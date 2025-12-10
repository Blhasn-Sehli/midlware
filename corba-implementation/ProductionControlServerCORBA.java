import ProductionControl.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Serveur CORBA de contrôle de production
 * Architecture multi-client: chaque machine = 1 client
 */
public class ProductionControlServerCORBA {
    
    // Base de données partagée
    private static Map<Integer, MachineData> machines = new ConcurrentHashMap<>();
    private static AssemblyData assemblyStation = new AssemblyData();
    private static final java.lang.Object machinesLock = new java.lang.Object();
    private static final java.lang.Object assemblyLock = new java.lang.Object();
    
    // Simulateur
    private static Thread productionSimulatorThread = null;
    private static volatile boolean simulatorRunning = false;
    
    // Classe pour stocker les données d'une machine
    static class MachineData {
        int id;
        String name;
        MachineType type;
        MachineState state;
        PartType currentPart;
        int productionCount;
        
        MachineData(int id, String name, MachineType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.state = MachineState.STOPPED;
            this.currentPart = typeToPartType(type);
            this.productionCount = 0;
        }
        
        PartType typeToPartType(MachineType type) {
            switch (type.value()) {
                case MachineType._TYPE_A: return PartType.PART_TYPE_A;
                case MachineType._TYPE_B: return PartType.PART_TYPE_B;
                case MachineType._TYPE_C: return PartType.PART_TYPE_C;
                case MachineType._TYPE_D: return PartType.PART_TYPE_D;
                default: return PartType.PART_TYPE_A;
            }
        }
    }
    
    // Classe pour stocker les données d'assemblage
    static class AssemblyData {
        StationState state = StationState.IDLE;
        int assembledProducts = 0;
        Map<PartType, StorageData> storageZones = new HashMap<>();
        
        AssemblyData() {
            storageZones.put(PartType.PART_TYPE_A, new StorageData(PartType.PART_TYPE_A, 20));
            storageZones.put(PartType.PART_TYPE_B, new StorageData(PartType.PART_TYPE_B, 20));
            storageZones.put(PartType.PART_TYPE_C, new StorageData(PartType.PART_TYPE_C, 20));
            storageZones.put(PartType.PART_TYPE_D, new StorageData(PartType.PART_TYPE_D, 20));
        }
    }
    
    static class StorageData {
        PartType partType;
        int currentQuantity = 0;
        int maxCapacity;
        
        StorageData(PartType type, int capacity) {
            this.partType = type;
            this.maxCapacity = capacity;
        }
    }
    
    /**
     * Implémentation du contrôle d'une machine
     */
    static class MachineControlImpl extends MachineControlPOA {
        private int myMachineId;
        
        public MachineControlImpl(int machineId) {
            this.myMachineId = machineId;
        }
        
        @Override
        public MachineInfo getMyMachineInfo() {
            MachineData data = machines.get(myMachineId);
            if (data == null) return null;
            
            return new MachineInfo(
                data.id, data.name, data.type, data.state,
                data.currentPart, data.productionCount
            );
        }
        
        @Override
        public OperationResult startMachine() {
            synchronized (machinesLock) {
                MachineData machine = machines.get(myMachineId);
                if (machine == null) {
                    return new OperationResult(false, "Machine introuvable");
                }
                
                if (machine.state == MachineState.RUNNING) {
                    return new OperationResult(false, "Machine deja en marche");
                }
                
                if (machine.state == MachineState.FAILURE) {
                    return new OperationResult(false, "Machine en panne - Reparation necessaire");
                }
                
                machine.state = MachineState.RUNNING;
                System.out.println("✓ Commande: Démarrage " + machine.name);
                
                checkAndStartAssembly();
                
                return new OperationResult(true, "Machine " + machine.name + " demarree");
            }
        }
        
        @Override
        public OperationResult stopMachine() {
            synchronized (machinesLock) {
                MachineData machine = machines.get(myMachineId);
                if (machine == null) {
                    return new OperationResult(false, "Machine introuvable");
                }
                
                machine.state = MachineState.STOPPED;
                System.out.println("✓ Commande: Arrêt " + machine.name);
                
                checkAndStopAssembly();
                
                return new OperationResult(true, "Machine " + machine.name + " arretee");
            }
        }
        
        @Override
        public OperationResult reportFailure(String description) {
            synchronized (machinesLock) {
                MachineData machine = machines.get(myMachineId);
                if (machine == null) {
                    return new OperationResult(false, "Machine introuvable");
                }
                
                System.out.println("✓ ALERTE PANNE: " + machine.name + " - " + description);
                
                machine.state = MachineState.FAILURE;
                
                // Remplacement automatique M1 → M2
                if (myMachineId == 1) {
                    MachineData m2 = machines.get(2);
                    if (m2 != null && m2.state == MachineState.STOPPED) {
                        m2.state = MachineState.RUNNING;
                        System.out.println(">>> [AUTO] Machine-M2 demarre automatiquement (remplacement M1)");
                    }
                }
                
                checkAndStopAssembly();
                
                return new OperationResult(true, "Panne signalee sur " + machine.name);
            }
        }
        
        @Override
        public OperationResult setMaintenanceMode() {
            synchronized (machinesLock) {
                MachineData machine = machines.get(myMachineId);
                if (machine == null) {
                    return new OperationResult(false, "Machine introuvable");
                }
                
                machine.state = MachineState.MAINTENANCE;
                System.out.println("✓ Commande: Maintenance " + machine.name);
                
                checkAndStopAssembly();
                
                return new OperationResult(true, "Machine " + machine.name + " en maintenance");
            }
        }
        
        @Override
        public MachineInfo[] getAllMachines() {
            List<MachineInfo> list = new ArrayList<>();
            for (MachineData data : machines.values()) {
                list.add(new MachineInfo(
                    data.id, data.name, data.type, data.state,
                    data.currentPart, data.productionCount
                ));
            }
            return list.toArray(new MachineInfo[0]);
        }
        
        @Override
        public ProductionData getProductionData() {
            MachineInfo[] machineArray = getAllMachines();
            
            List<StorageZoneInfo> zones = new ArrayList<>();
            for (StorageData sd : assemblyStation.storageZones.values()) {
                zones.add(new StorageZoneInfo(sd.partType, sd.currentQuantity, sd.maxCapacity));
            }
            
            AssemblyStationInfo stationInfo = new AssemblyStationInfo(
                assemblyStation.state,
                assemblyStation.assembledProducts,
                zones.toArray(new StorageZoneInfo[0])
            );
            
            return new ProductionData(machineArray, stationInfo);
        }
    }
    
    /**
     * Implémentation du serveur de production
     */
    static class ProductionServerImpl extends ProductionServerPOA {
        private Map<Integer, MachineControl> machineControls = new ConcurrentHashMap<>();
        private ORB orb;
        private POA rootPOA;
        
        public ProductionServerImpl(ORB orb, POA rootPOA) {
            this.orb = orb;
            this.rootPOA = rootPOA;
        }
        
        @Override
        public MachineControl getMachineControl(int machineId) {
            if (machineId < 1 || machineId > 5) {
                return null;
            }
            
            // Créer un nouveau contrôleur pour cette machine si nécessaire
            if (!machineControls.containsKey(machineId)) {
                try {
                    MachineControlImpl controlImpl = new MachineControlImpl(machineId);
                    org.omg.CORBA.Object ref = rootPOA.servant_to_reference(controlImpl);
                    MachineControl control = MachineControlHelper.narrow(ref);
                    machineControls.put(machineId, control);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            
            return machineControls.get(machineId);
        }
        
        @Override
        public ProductionData getProductionData() {
            List<MachineInfo> list = new ArrayList<>();
            for (MachineData data : machines.values()) {
                list.add(new MachineInfo(
                    data.id, data.name, data.type, data.state,
                    data.currentPart, data.productionCount
                ));
            }
            
            List<StorageZoneInfo> zones = new ArrayList<>();
            for (StorageData sd : assemblyStation.storageZones.values()) {
                zones.add(new StorageZoneInfo(sd.partType, sd.currentQuantity, sd.maxCapacity));
            }
            
            AssemblyStationInfo stationInfo = new AssemblyStationInfo(
                assemblyStation.state,
                assemblyStation.assembledProducts,
                zones.toArray(new StorageZoneInfo[0])
            );
            
            return new ProductionData(list.toArray(new MachineInfo[0]), stationInfo);
        }
    }
    
    /**
     * Vérifier et démarrer l'assemblage si toutes les machines sont prêtes
     */
    private static void checkAndStartAssembly() {
        synchronized (assemblyLock) {
            boolean hasTypeA = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_A && m.state == MachineState.RUNNING);
            boolean hasTypeB = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_B && m.state == MachineState.RUNNING);
            boolean hasTypeC = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_C && m.state == MachineState.RUNNING);
            boolean hasTypeD = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_D && m.state == MachineState.RUNNING);
            
            if (hasTypeA && hasTypeB && hasTypeC && hasTypeD) {
                if (assemblyStation.state != StationState.ASSEMBLING) {
                    assemblyStation.state = StationState.ASSEMBLING;
                    System.out.println("\n>>> [AUTO] Station d'assemblage DEMARRE - Toutes les machines necessaires sont en marche!\n");
                    startProductionSimulatorIfNeeded();
                }
            }
        }
    }
    
    /**
     * Vérifier et arrêter l'assemblage si des machines manquent
     */
    private static void checkAndStopAssembly() {
        synchronized (assemblyLock) {
            boolean hasTypeA = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_A && m.state == MachineState.RUNNING);
            boolean hasTypeB = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_B && m.state == MachineState.RUNNING);
            boolean hasTypeC = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_C && m.state == MachineState.RUNNING);
            boolean hasTypeD = machines.values().stream()
                .anyMatch(m -> m.type == MachineType.TYPE_D && m.state == MachineState.RUNNING);
            
            if (!hasTypeA || !hasTypeB || !hasTypeC || !hasTypeD) {
                if (assemblyStation.state == StationState.ASSEMBLING) {
                    assemblyStation.state = StationState.WAITING_PARTS;
                    System.out.println("\n>>> [AUTO] Station d'assemblage EN ATTENTE - Il manque des machines en marche!\n");
                }
            }
        }
    }
    
    /**
     * Démarrer le simulateur de production si nécessaire
     */
    private static void startProductionSimulatorIfNeeded() {
        if (!simulatorRunning) {
            simulatorRunning = true;
            productionSimulatorThread = new Thread(new ProductionSimulator());
            productionSimulatorThread.setDaemon(true);
            productionSimulatorThread.start();
            System.out.println(">>> [AUTO] Simulateur de production DEMARRE");
        }
    }
    
    // Méthodes utilitaires pour convertir les enums CORBA en String lisible
    private static String machineTypeToString(MachineType type) {
        if (type == null) return "UNKNOWN";
        if (type.value() == MachineType._TYPE_A) return "TYPE_A";
        if (type.value() == MachineType._TYPE_B) return "TYPE_B";
        if (type.value() == MachineType._TYPE_C) return "TYPE_C";
        if (type.value() == MachineType._TYPE_D) return "TYPE_D";
        return "UNKNOWN";
    }
    
    private static String machineStateToString(MachineState state) {
        if (state == null) return "UNKNOWN";
        if (state.value() == MachineState._STOPPED) return "STOPPED";
        if (state.value() == MachineState._RUNNING) return "RUNNING";
        if (state.value() == MachineState._FAILURE) return "FAILURE";
        if (state.value() == MachineState._MAINTENANCE) return "MAINTENANCE";
        return "UNKNOWN";
    }
    
    private static String partTypeToString(PartType type) {
        if (type == null) return "UNKNOWN";
        if (type.value() == PartType._PART_TYPE_A) return "PART_A";
        if (type.value() == PartType._PART_TYPE_B) return "PART_B";
        if (type.value() == PartType._PART_TYPE_C) return "PART_C";
        if (type.value() == PartType._PART_TYPE_D) return "PART_D";
        return "UNKNOWN";
    }
    
    /**
     * Simulateur de production
     */
    static class ProductionSimulator implements Runnable {
        private Random random = new Random();
        
        @Override
        public void run() {
            try {
                Thread.sleep(5000); // Attendre le démarrage
                
                while (true) {
                    Thread.sleep(3000 + random.nextInt(4000)); // 3-7 secondes
                    
                    // Production de pièces - TOUJOURS actif pour machines RUNNING
                    synchronized (machinesLock) {
                        for (MachineData machine : machines.values()) {
                            if (machine.state == MachineState.RUNNING) {
                                StorageData zone = assemblyStation.storageZones.get(machine.currentPart);
                                if (zone != null && zone.currentQuantity < zone.maxCapacity) {
                                    zone.currentQuantity++;
                                    machine.productionCount++;
                                    System.out.println("  [Production] " + machine.name + " -> " + 
                                        partTypeToString(machine.currentPart) + " (" + zone.currentQuantity + "/" + zone.maxCapacity + ")");
                                }
                            }
                        }
                    }
                    
                    // Assemblage - SEULEMENT si état ASSEMBLING
                    synchronized (assemblyLock) {
                        if (assemblyStation.state == StationState.ASSEMBLING) {
                            boolean canAssemble = true;
                            for (StorageData zone : assemblyStation.storageZones.values()) {
                                if (zone.currentQuantity < 1) {
                                    canAssemble = false;
                                    break;
                                }
                            }
                            
                            if (canAssemble) {
                                for (StorageData zone : assemblyStation.storageZones.values()) {
                                    zone.currentQuantity--;
                                }
                                assemblyStation.assembledProducts++;
                                System.out.println("  [Assemblage] Produit fini assemble! Total: " + 
                                    assemblyStation.assembledProducts);
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Simulateur arrete");
            }
        }
    }
    
    /**
     * Initialiser la chaîne de production
     */
    private static void initializeProductionLine() {
        machines.put(1, new MachineData(1, "Machine-M1", MachineType.TYPE_A));
        machines.put(2, new MachineData(2, "Machine-M2", MachineType.TYPE_A));
        machines.put(3, new MachineData(3, "Machine-M3", MachineType.TYPE_B));
        machines.put(4, new MachineData(4, "Machine-M4", MachineType.TYPE_C));
        machines.put(5, new MachineData(5, "Machine-M5", MachineType.TYPE_D));
        
        System.out.println("✓ Chaîne de production initialisée:");
        for (MachineData m : machines.values()) {
            System.out.println("  - " + m.name + " (" + machineTypeToString(m.type) + ") - État: " + machineStateToString(m.state));
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("====================================================");
            System.out.println("  Serveur de Controle de Production - CORBA       ");
            System.out.println("====================================================");
            System.out.println();
            
            initializeProductionLine();
            
            // Initialiser ORB
            ORB orb = ORB.init(args, null);
            
            // Obtenir le POA et l'activer
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            
            // Créer le servant
            ProductionServerImpl serverImpl = new ProductionServerImpl(orb, rootPOA);
            
            // Obtenir la référence de l'objet
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(serverImpl);
            ProductionServer serverRef = ProductionServerHelper.narrow(ref);
            
            // Enregistrer dans le service de noms
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            
            String name = "ProductionServer";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, serverRef);
            
            System.out.println("\nServeur CORBA prêt et en attente de connexions...");
            System.out.println("Nom dans NameService: " + name);
            System.out.println();
            
            // Attendre les invocations
            orb.run();
            
        } catch (Exception e) {
            System.err.println("Erreur serveur: " + e);
            e.printStackTrace();
        }
    }
}
