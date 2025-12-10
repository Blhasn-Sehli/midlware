package socket.server;

import common.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Serveur de contrôle multi-thread pour la chaîne de production
 * Implémentation Socket avec synchronisation entre threads
 */
public class ProductionControlServer {
    private static final int PORT = 9000;
    
    // Base de données partagée (accès synchronisé)
    private static Map<Integer, Machine> machines = new ConcurrentHashMap<>();
    private static AssemblyStation assemblyStation;
    private static List<Alert> alerts = Collections.synchronizedList(new ArrayList<>());
    private static int alertIdCounter = 1;
    
    // Verrous pour la synchronisation
    private static final Object machinesLock = new Object();
    private static final Object assemblyLock = new Object();
    
    // Simulateur de production
    private static Thread productionSimulatorThread = null;
    private static volatile boolean simulatorRunning = false;
    
    public static void main(String[] args) {
        initializeProductionLine();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("╔════════════════════════════════════════════════════╗");
            System.out.println("║  Serveur de Contrôle de Production - SOCKET       ║");
            System.out.println("╚════════════════════════════════════════════════════╝");
            System.out.println("Port: " + PORT);
            System.out.println("État: EN LIGNE");
            System.out.println("\nEn attente de connexions...\n");
            
            // Thread pour simuler la production automatique
            // DESACTIVE PAR DEFAUT - Décommentez pour activer la simulation automatique
            // new Thread(new ProductionSimulator()).start();
            System.out.println("[INFO] Simulateur automatique DESACTIVE - Controle manuel uniquement");
            System.out.println("[INFO] Utilisez le client pour demarrer les machines (option 4)\n");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("✓ Nouvelle connexion: " + clientSocket.getInetAddress());
                
                // Gérer chaque client dans un thread séparé
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialisation de la chaîne de production
     */
    private static void initializeProductionLine() {
        // Machines de production (M1 et M2 pour la même tâche)
        machines.put(1, new Machine(1, "Machine-M1", Machine.MachineType.TYPE_A));
        machines.put(2, new Machine(2, "Machine-M2", Machine.MachineType.TYPE_A)); // Backup pour M1
        machines.put(3, new Machine(3, "Machine-M3", Machine.MachineType.TYPE_B));
        machines.put(4, new Machine(4, "Machine-M4", Machine.MachineType.TYPE_C));
        machines.put(5, new Machine(5, "Machine-M5", Machine.MachineType.TYPE_D));
        
        // Station d'assemblage
        assemblyStation = new AssemblyStation(100, "Station-Assemblage-Principale");
        
        // TOUTES LES MACHINES DÉMARRENT EN STOPPED
        // Vous pouvez les démarrer manuellement avec le client (option 4)
        // OU décommenter les lignes ci-dessous pour démarrage automatique:
        
        // machines.get(1).setState(Machine.MachineState.RUNNING);
        // machines.get(3).setState(Machine.MachineState.RUNNING);
        // machines.get(4).setState(Machine.MachineState.RUNNING);
        // machines.get(5).setState(Machine.MachineState.RUNNING);
        // assemblyStation.setState(AssemblyStation.StationState.ASSEMBLING);
        
        // Par défaut: tout est arrêté, contrôle manuel
        assemblyStation.setState(AssemblyStation.StationState.IDLE);
        
        System.out.println("✓ Chaîne de production initialisée:");
        for (Machine m : machines.values()) {
            System.out.println("  - " + m);
        }
        System.out.println("  - " + assemblyStation);
    }
    
    /**
     * Gestionnaire de connexion client (Thread)
     */
    static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                
                while (true) {
                    ProductionMessage message = (ProductionMessage) in.readObject();
                    ProductionMessage response = processMessage(message);
                    out.writeObject(response);
                    out.flush();
                }
            } catch (EOFException e) {
                System.out.println("✗ Client déconnecté: " + socket.getInetAddress());
            } catch (Exception e) {
                System.err.println("Erreur client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * Traitement des messages avec synchronisation
         */
        private ProductionMessage processMessage(ProductionMessage message) {
            try {
                switch (message.getType()) {
                    case ALERT:
                        return handleAlert((Alert) message.getPayload());
                        
                    case COMMAND:
                        return handleCommand((ControlCommand) message.getPayload());
                        
                    case STATUS_UPDATE:
                        return handleStatusUpdate(message.getPayload());
                        
                    case PRODUCTION_DATA:
                        return getProductionData();
                        
                    default:
                        return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                                null, false, "Type de message inconnu");
                }
            } catch (Exception e) {
                return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                        null, false, "Erreur: " + e.getMessage());
            }
        }
        
        /**
         * Gestion des alertes avec synchronisation
         */
        private ProductionMessage handleAlert(Alert alert) {
            synchronized (alerts) {
                alert.setId(alertIdCounter++);
                alerts.add(alert);
            }
            
            System.out.println("\n⚠ ALERTE REÇUE: " + alert);
            
            // Résolution automatique selon le type d'alerte
            ProductionMessage resolution = resolveAlert(alert);
            
            return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                    resolution.getPayload(), true, 
                    "Alerte traitée: " + resolution.getResponseMessage());
        }
        
        /**
         * Résolution automatique des alertes
         */
        private ProductionMessage resolveAlert(Alert alert) {
            switch (alert.getType()) {
                case MACHINE_FAILURE:
                    return handleMachineFailure(alert);
                    
                case STORAGE_FULL:
                    return handleStorageFull(alert);
                    
                case STORAGE_EMPTY:
                case STORAGE_LOW:
                    return handleStorageLow(alert);
                    
                default:
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, true, "Alerte enregistrée");
            }
        }
        
        /**
         * Gestion panne machine: Remplacement automatique
         * Exemple: M1 tombe en panne → M2 la remplace
         */
        private ProductionMessage handleMachineFailure(Alert alert) {
            synchronized (machinesLock) {
                int failedId = alert.getSourceId();
                Machine failedMachine = machines.get(failedId);
                
                if (failedMachine == null) {
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, false, "Machine inconnue");
                }
                
                // Arrêter la machine en panne ET METTRE À JOUR dans la map
                failedMachine.setState(Machine.MachineState.FAILURE);
                machines.put(failedId, failedMachine); // IMPORTANT: Mise à jour explicite
                System.out.println("  → Arrêt de " + failedMachine.getName() + " [État: FAILURE]");
                
                // Chercher une machine de remplacement du même type
                Machine replacement = findReplacementMachine(failedMachine.getType(), failedId);
                
                if (replacement != null) {
                    replacement.setState(Machine.MachineState.RUNNING);
                    machines.put(replacement.getId(), replacement); // IMPORTANT: Mise à jour
                    System.out.println("  → Démarrage de " + replacement.getName() + " (remplacement) [État: RUNNING]");
                    
                    alert.setResolved(true);
                    
                    // Vérifier si on peut démarrer l'assemblage
                    checkAndStartAssembly();
                    
                    ControlCommand cmd = new ControlCommand(
                            ControlCommand.CommandType.REPLACE_MACHINE,
                            failedId, replacement.getId(),
                            "Machine remplacée avec succès");
                    
                    return new ProductionMessage(ProductionMessage.MessageType.COMMAND, 
                            cmd, true, 
                            failedMachine.getName() + " remplacée par " + replacement.getName());
                } else {
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, false, "Aucune machine de remplacement disponible");
                }
            }
        }
        
        /**
         * Trouver une machine de remplacement
         */
        private Machine findReplacementMachine(Machine.MachineType type, int excludeId) {
            for (Machine m : machines.values()) {
                if (m.getType() == type && 
                    m.getId() != excludeId && 
                    m.getState() == Machine.MachineState.STOPPED) {
                    return m;
                }
            }
            return null;
        }
        
        /**
         * Gestion zone de stockage pleine: Arrêt machine correspondante
         */
        private ProductionMessage handleStorageFull(Alert alert) {
            synchronized (machinesLock) {
                synchronized (assemblyLock) {
                    String partType = alert.getMessage().split(":")[0];
                    
                    // Arrêter les machines produisant cette pièce
                    for (Machine m : machines.values()) {
                        if (m.getCurrentPart().equals(partType) && 
                            m.getState() == Machine.MachineState.RUNNING) {
                            m.setState(Machine.MachineState.STOPPED);
                            machines.put(m.getId(), m); // Mettre à jour dans la map
                            System.out.println("  → Arrêt de " + m.getName() + " (stockage plein)");
                        }
                    }
                    
                    alert.setResolved(true);
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, true, "Production arrêtée pour " + partType);
                }
            }
        }
        
        /**
         * Gestion zone de stockage vide/basse: Démarrage machines
         */
        private ProductionMessage handleStorageLow(Alert alert) {
            synchronized (machinesLock) {
                String partType = alert.getMessage().split(":")[0];
                
                // Démarrer les machines produisant cette pièce
                boolean started = false;
                for (Machine m : machines.values()) {
                    if (m.getCurrentPart().equals(partType) && 
                        m.getState() == Machine.MachineState.STOPPED) {
                        m.setState(Machine.MachineState.RUNNING);
                        machines.put(m.getId(), m); // Mettre à jour dans la map
                        System.out.println("  → Démarrage de " + m.getName() + " (stockage bas)");
                        started = true;
                    }
                }
                
                if (started) {
                    alert.setResolved(true);
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, true, "Production relancée pour " + partType);
                } else {
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, false, "Aucune machine disponible");
                }
            }
        }
        
        /**
         * Gestion des commandes de contrôle
         */
        private ProductionMessage handleCommand(ControlCommand command) {
            synchronized (machinesLock) {
                Machine target = machines.get(command.getTargetId());
                
                if (target == null) {
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, false, "Machine non trouvée");
                }
                
                switch (command.getType()) {
                    case START_MACHINE:
                        target.setState(Machine.MachineState.RUNNING);
                        machines.put(target.getId(), target); // Mise à jour
                        System.out.println("✓ Commande: Démarrage " + target.getName());
                        
                        // Vérifier si on peut démarrer l'assemblage
                        checkAndStartAssembly();
                        break;
                        
                    case STOP_MACHINE:
                        target.setState(Machine.MachineState.STOPPED);
                        machines.put(target.getId(), target); // Mise à jour
                        System.out.println("✓ Commande: Arrêt " + target.getName());
                        
                        // Vérifier si on doit arrêter l'assemblage
                        checkAndStopAssembly();
                        break;
                        
                    case MAINTENANCE_MODE:
                        target.setState(Machine.MachineState.MAINTENANCE);
                        machines.put(target.getId(), target); // Mise à jour
                        System.out.println("✓ Commande: Maintenance " + target.getName());
                        break;
                        
                    default:
                        return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                                null, false, "Commande non supportée");
                }
                
                return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                        target, true, "Commande exécutée");
            }
        }
        
        /**
         * Démarrer le simulateur de production si pas déjà lancé
         */
        private static void startProductionSimulatorIfNeeded() {
            synchronized (assemblyLock) {
                if (!simulatorRunning) {
                    simulatorRunning = true;
                    productionSimulatorThread = new Thread(new ProductionSimulator());
                    productionSimulatorThread.start();
                    System.out.println(">>> [AUTO] Simulateur de production DEMARRE\n");
                }
            }
        }
        
        /**
         * Vérifier si on peut démarrer l'assemblage
         * L'assemblage démarre si au moins une machine de chaque type est en marche
         */
        private void checkAndStartAssembly() {
            synchronized (machinesLock) {
                synchronized (assemblyLock) {
                    // Vérifier qu'on a au moins une machine en marche pour chaque type
                    boolean hasTypeA = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_A && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeB = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_B && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeC = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_C && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeD = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_D && m.getState() == Machine.MachineState.RUNNING);
                    
                    if (hasTypeA && hasTypeB && hasTypeC && hasTypeD) {
                        if (assemblyStation.getState() != AssemblyStation.StationState.ASSEMBLING) {
                            assemblyStation.setState(AssemblyStation.StationState.ASSEMBLING);
                            System.out.println("\n>>> [AUTO] Station d'assemblage DEMARRE - Toutes les machines necessaires sont en marche!");
                            
                            // Démarrer le simulateur de production si pas déjà lancé
                            startProductionSimulatorIfNeeded();
                        }
                    }
                }
            }
        }
        
        /**
         * Vérifier si on doit arrêter l'assemblage
         */
        private void checkAndStopAssembly() {
            synchronized (machinesLock) {
                synchronized (assemblyLock) {
                    // Vérifier qu'on a au moins une machine en marche pour chaque type
                    boolean hasTypeA = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_A && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeB = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_B && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeC = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_C && m.getState() == Machine.MachineState.RUNNING);
                    boolean hasTypeD = machines.values().stream()
                        .anyMatch(m -> m.getType() == Machine.MachineType.TYPE_D && m.getState() == Machine.MachineState.RUNNING);
                    
                    if (!hasTypeA || !hasTypeB || !hasTypeC || !hasTypeD) {
                        if (assemblyStation.getState() == AssemblyStation.StationState.ASSEMBLING) {
                            assemblyStation.setState(AssemblyStation.StationState.WAITING_PARTS);
                            System.out.println("\n>>> [AUTO] Station d'assemblage EN ATTENTE - Il manque des machines en marche!");
                        }
                    }
                }
            }
        }
        
        /**
         * Mise à jour de statut
         */
        private ProductionMessage handleStatusUpdate(Object payload) {
            if (payload instanceof Machine) {
                synchronized (machinesLock) {
                    Machine updated = (Machine) payload;
                    machines.put(updated.getId(), updated);
                    System.out.println("✓ Mise à jour: " + updated.getName() + " [État: " + updated.getState() + "]");
                    return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                            null, true, "Statut machine mis à jour");
                }
            }
            return new ProductionMessage(ProductionMessage.MessageType.ACK, 
                    null, false, "Type de mise à jour non supporté");
        }
        
        /**
         * Obtenir les données de production
         * IMPORTANT: Crée de nouvelles instances pour éviter problèmes de sérialisation
         */
        private ProductionMessage getProductionData() {
            Map<String, Object> data = new HashMap<>();
            
            synchronized (machinesLock) {
                // Créer une liste fraîche avec les derniers états
                List<Machine> machineList = new ArrayList<>();
                for (Machine m : machines.values()) {
                    // Créer une nouvelle instance pour la sérialisation
                    Machine copy = new Machine(m.getId(), m.getName(), m.getType());
                    copy.setState(m.getState());
                    copy.setProductionCount(m.getProductionCount());
                    copy.setCurrentPart(m.getCurrentPart());
                    machineList.add(copy);
                }
                data.put("machines", machineList);
            }
            
            synchronized (assemblyLock) {
                // Créer une copie fraîche de la station d'assemblage avec les vraies valeurs
                AssemblyStation stationCopy = new AssemblyStation(assemblyStation.getId(), assemblyStation.getName());
                stationCopy.setState(assemblyStation.getState());
                stationCopy.setAssembledProducts(assemblyStation.getAssembledProducts());
                
                // Copier les zones de stockage avec leurs quantités actuelles
                for (Map.Entry<String, StorageZone> entry : assemblyStation.getStorageZones().entrySet()) {
                    StorageZone originalZone = entry.getValue();
                    StorageZone zoneCopy = new StorageZone(originalZone.getPartType(), originalZone.getMaxCapacity());
                    zoneCopy.setCurrentQuantity(originalZone.getCurrentQuantity());
                    stationCopy.getStorageZones().put(entry.getKey(), zoneCopy);
                }
                
                data.put("assemblyStation", stationCopy);
            }
            
            synchronized (alerts) {
                data.put("alerts", new ArrayList<>(alerts));
            }
            
            return new ProductionMessage(ProductionMessage.MessageType.PRODUCTION_DATA, 
                    data, true, "Données récupérées");
        }
    }
    
    /**
     * Simulateur de production (Thread)
     */
    static class ProductionSimulator implements Runnable {
        private Random random = new Random();
        
        @Override
        public void run() {
            try {
                Thread.sleep(5000); // Attendre le démarrage
                
                while (true) {
                    Thread.sleep(3000 + random.nextInt(4000)); // 3-7 secondes
                    
                    // Simuler production et dépôt de pièces
                    synchronized (machinesLock) {
                        for (Machine m : machines.values()) {
                            if (m.getState() == Machine.MachineState.RUNNING) {
                                m.incrementProduction();
                                machines.put(m.getId(), m); // Mise à jour dans la map
                                
                                synchronized (assemblyLock) {
                                    StorageZone zone = assemblyStation.getStorageZone(m.getCurrentPart());
                                    if (zone != null && !zone.isFull()) {
                                        boolean added = zone.addPart();
                                        if (added) {
                                            System.out.println("  [Production] " + m.getName() + " → " + zone.getPartType() + " (" + zone.getCurrentQuantity() + "/" + zone.getMaxCapacity() + ")");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Simuler assemblage
                    synchronized (assemblyLock) {
                        if (assemblyStation.getState() == AssemblyStation.StationState.ASSEMBLING) {
                            boolean canAssemble = true;
                            for (StorageZone zone : assemblyStation.getStorageZones().values()) {
                                if (zone.getCurrentQuantity() == 0) {
                                    canAssemble = false;
                                    break;
                                }
                            }
                            
                            if (canAssemble) {
                                for (StorageZone zone : assemblyStation.getStorageZones().values()) {
                                    zone.removePart();
                                }
                                assemblyStation.incrementAssembled();
                                System.out.println("  [Assemblage] Produit fini assemblé! Total: " + assemblyStation.getAssembledProducts());
                            }
                        }
                    }
                    
                    // // Simuler panne aléatoire (1% de chance)
                    // if (random.nextInt(100) < 1) {
                    //     simulateFailure();
                    // }
                }
            } catch (InterruptedException e) {
                System.out.println("Simulateur arrêté");
            }
        }
        
    //     private void simulateFailure() {
    //         synchronized (machinesLock) {
    //             List<Machine> runningMachines = new ArrayList<>();
    //             for (Machine m : machines.values()) {
    //                 if (m.getState() == Machine.MachineState.RUNNING) {
    //                     runningMachines.add(m);
    //                 }
    //             }
                
    //             if (!runningMachines.isEmpty()) {
    //                 Machine failed = runningMachines.get(random.nextInt(runningMachines.size()));
    //                 System.out.println("\n⚠ SIMULATION: Panne détectée sur " + failed.getName());
    //             }
    //         }
    //     }
    // }
}
