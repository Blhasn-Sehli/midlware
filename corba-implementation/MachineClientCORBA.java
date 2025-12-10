import ProductionControl.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import java.util.Scanner;

/**
 * Client CORBA représentant UNE seule machine
 * Chaque machine est un client indépendant
 */
public class MachineClientCORBA {
    private MachineControl myMachineControl;
    private int myMachineId;
    private String myMachineName;
    private Scanner scanner;
    
    public MachineClientCORBA(int machineId, ORB orb) throws Exception {
        this.myMachineId = machineId;
        this.scanner = new Scanner(System.in);
        
        // Obtenir le serveur depuis le service de noms
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        
        String name = "ProductionServer";
        ProductionServer server = ProductionServerHelper.narrow(ncRef.resolve_str(name));
        
        // Obtenir le contrôle de MA machine
        myMachineControl = server.getMachineControl(machineId);
        
        if (myMachineControl == null) {
            throw new Exception("Impossible d'obtenir le controle de la machine " + machineId);
        }
        
        // Récupérer les infos de ma machine
        MachineInfo info = myMachineControl.getMyMachineInfo();
        myMachineName = info.name;
        
        System.out.println("========================================");
        System.out.println("  CLIENT MACHINE M" + machineId + " - CORBA");
        System.out.println("========================================");
        System.out.println("Connecte au serveur CORBA");
        System.out.println("Gestion de: " + myMachineName + " (Type: " + machineTypeToString(info.type) + ")");
    }
    
    public void run() {
        boolean running = true;
        
        while (running) {
            displayMenu();
            System.out.print("Votre choix: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        viewMyMachine();
                        break;
                    case 2:
                        startMyMachine();
                        break;
                    case 3:
                        stopMyMachine();
                        break;
                    case 4:
                        reportFailure();
                        break;
                    case 5:
                        maintenanceMode();
                        break;
                    case 6:
                        viewAllMachines();
                        break;
                    case 7:
                        viewProductionStatus();
                        break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("[ERREUR] Choix invalide!");
                }
                
                if (running) {
                    System.out.println("\nAppuyez sur Entree pour continuer...");
                    scanner.nextLine();
                }
            } catch (Exception e) {
                System.err.println("[ERREUR] " + e.getMessage());
                scanner.nextLine();
            }
        }
        
        System.out.println("Deconnexion de Machine M" + myMachineId);
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
    
    private static String stationStateToString(StationState state) {
        if (state == null) return "UNKNOWN";
        if (state.value() == StationState._WAITING_PARTS) return "WAITING_PARTS";
        if (state.value() == StationState._ASSEMBLING) return "ASSEMBLING";
        return "UNKNOWN";
    }
    
    private void displayMenu() {
        System.out.println("\n+======================================================+");
        System.out.println("|  MENU - MACHINE M" + myMachineId + " (" + myMachineName + ")");
        System.out.println("+======================================================+");
        System.out.println("|  1. Voir l'etat de MA machine                       |");
        System.out.println("|  2. DEMARRER ma machine                             |");
        System.out.println("|  3. ARRETER ma machine                              |");
        System.out.println("|  4. Signaler une PANNE                              |");
        System.out.println("|  5. Mode MAINTENANCE                                |");
        System.out.println("|  ------------------------------------------------   |");
        System.out.println("|  6. Voir toutes les machines (lecture seule)       |");
        System.out.println("|  7. Voir l'etat global de production               |");
        System.out.println("|  0. Quitter                                         |");
        System.out.println("+======================================================+");
    }
    
    private void viewMyMachine() {
        System.out.println("\n+======================================================+");
        System.out.println("|  ETAT DE MA MACHINE M" + myMachineId);
        System.out.println("+======================================================+");
        
        try {
            MachineInfo m = myMachineControl.getMyMachineInfo();
            
            System.out.println("\n  ID:              " + m.id);
            System.out.println("  Nom:             " + m.name);
            System.out.println("  Type:            " + machineTypeToString(m.type));
            System.out.println("  Etat:            " + getStateIcon(m.state) + " [" + machineStateToString(m.state) + "]");
            System.out.println("  Piece actuelle:  " + partTypeToString(m.currentPart));
            System.out.println("  Production:      " + m.productionCount + " pieces");
            System.out.println("  Piece produite:  " + m.currentPart);
            System.out.println("  Production:      " + m.productionCount + " pieces");
            System.out.println("");
            
            String status = "";
            switch (m.state.value()) {
                case MachineState._RUNNING:
                    status = "[OK] Machine en cours de production";
                    break;
                case MachineState._STOPPED:
                    status = "[--] Machine arretee - Utilisez option 2 pour demarrer";
                    break;
                case MachineState._FAILURE:
                    status = "[!!] Machine en PANNE - Reparation necessaire";
                    break;
                case MachineState._MAINTENANCE:
                    status = "[**] Machine en maintenance";
                    break;
            }
            System.out.println("  " + status);
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void startMyMachine() {
        System.out.println("\n=== DEMARRAGE DE MA MACHINE M" + myMachineId + " ===");
        
        try {
            OperationResult result = myMachineControl.startMachine();
            if (result.success) {
                System.out.println("[OK] " + result.message);
                System.out.println("[OK] Machine M" + myMachineId + " est maintenant EN MARCHE!");
            } else {
                System.out.println("[ERREUR] " + result.message);
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void stopMyMachine() {
        System.out.println("\n=== ARRET DE MA MACHINE M" + myMachineId + " ===");
        
        try {
            OperationResult result = myMachineControl.stopMachine();
            if (result.success) {
                System.out.println("[OK] " + result.message);
                System.out.println("[OK] Machine M" + myMachineId + " est maintenant ARRETEE");
            } else {
                System.out.println("[ERREUR] " + result.message);
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void reportFailure() {
        System.out.println("\n=== SIGNALER PANNE - MACHINE M" + myMachineId + " ===");
        System.out.print("Description de la panne: ");
        String description = scanner.nextLine();
        
        try {
            OperationResult result = myMachineControl.reportFailure(description);
            if (result.success) {
                System.out.println("[OK] Panne signalee au serveur");
                System.out.println("[OK] " + result.message);
            } else {
                System.out.println("[ERREUR] " + result.message);
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void maintenanceMode() {
        System.out.println("\n=== MODE MAINTENANCE - MACHINE M" + myMachineId + " ===");
        
        try {
            OperationResult result = myMachineControl.setMaintenanceMode();
            if (result.success) {
                System.out.println("[OK] " + result.message);
                System.out.println("[OK] Machine M" + myMachineId + " en mode MAINTENANCE");
            } else {
                System.out.println("[ERREUR] " + result.message);
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void viewAllMachines() {
        System.out.println("\n+======================================================+");
        System.out.println("|  TOUTES LES MACHINES (lecture seule)                |");
        System.out.println("+======================================================+");
        
        try {
            MachineInfo[] machines = myMachineControl.getAllMachines();
            
            System.out.println("\n+-----+---------------+----------+-------------+-----------+");
            System.out.println("| ID  | Nom           | Type     | Etat        | Prod      |");
            System.out.println("+-----+---------------+----------+-------------+-----------+");
            
            for (MachineInfo m : machines) {
                String status = getStateIcon(m.state);
                String marker = (m.id == myMachineId) ? " <-- MOI" : "";
                System.out.println(String.format("| %-3d | %-13s | %-8s | %s %-8s | %-9d |%s",
                    m.id, m.name, machineTypeToString(m.type), status, 
                    machineStateToString(m.state), m.productionCount, marker));
            }
            System.out.println("+-----+---------------+----------+-------------+-----------+");
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private void viewProductionStatus() {
        System.out.println("\n+======================================================+");
        System.out.println("|  ETAT GLOBAL DE PRODUCTION (lecture seule)          |");
        System.out.println("+======================================================+");
        
        try {
            ProductionData data = myMachineControl.getProductionData();
            
            long running = 0, stopped = 0, failed = 0;
            for (MachineInfo m : data.machines) {
                switch (m.state.value()) {
                    case MachineState._RUNNING: running++; break;
                    case MachineState._STOPPED: stopped++; break;
                    case MachineState._FAILURE: failed++; break;
                }
            }
            
            System.out.println("\n>>> RESUME:");
            System.out.println("  - Machines actives: " + running + "/" + data.machines.length);
            System.out.println("  - Machines arretees: " + stopped);
            System.out.println("  - Machines en panne: " + failed);
            System.out.println("  - Produits assembles: " + data.assemblyStation.assembledProducts);
            System.out.println("  - Etat assemblage: " + stationStateToString(data.assemblyStation.state));
            
            System.out.println("\n>>> ZONES DE STOCKAGE:");
            for (StorageZoneInfo zone : data.assemblyStation.storageZones) {
                boolean isEmpty = zone.currentQuantity == 0;
                boolean isFull = zone.currentQuantity >= zone.maxCapacity;
                boolean isLow = zone.currentQuantity < 5 && !isEmpty;
                
                String status = isEmpty ? "[VIDE]" : isFull ? "[PLEIN]" : isLow ? "[BAS]" : "[OK]";
                double pct = (zone.currentQuantity * 100.0) / zone.maxCapacity;
                System.out.println(String.format("  %s %-15s %2d/%-2d (%.0f%%)",
                    status, partTypeToString(zone.partType), zone.currentQuantity, zone.maxCapacity, pct));
            }
        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
        }
    }
    
    private String getStateIcon(MachineState state) {
        switch (state.value()) {
            case MachineState._RUNNING: return "[RUN]";
            case MachineState._STOPPED: return "[STOP]";
            case MachineState._FAILURE: return "[FAIL]";
            case MachineState._MAINTENANCE: return "[MAINT]";
            default: return "[?]";
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("========================================");
            System.out.println("  CLIENT MACHINE - CORBA");
            System.out.println("========================================");
            System.out.println("\nUsage: java MachineClientCORBA <machine_id> -ORBInitialPort <port> -ORBInitialHost <host>");
            System.out.println("\nExemples:");
            System.out.println("  java -cp bin MachineClientCORBA 1 -ORBInitialPort 1050 -ORBInitialHost localhost");
            System.out.println("  java -cp bin MachineClientCORBA 2 -ORBInitialPort 1050 -ORBInitialHost localhost");
            System.out.println("  java -cp bin MachineClientCORBA 3 -ORBInitialPort 1050 -ORBInitialHost localhost");
            System.out.println("\nChaque client ne peut gerer QUE sa propre machine!");
            System.exit(1);
        }
        
        try {
            int machineId = Integer.parseInt(args[0]);
            if (machineId < 1 || machineId > 5) {
                System.err.println("[ERREUR] ID machine doit etre entre 1 et 5");
                System.exit(1);
            }
            
            // Initialiser ORB
            ORB orb = ORB.init(args, null);
            
            MachineClientCORBA client = new MachineClientCORBA(machineId, orb);
            client.run();
            
        } catch (NumberFormatException e) {
            System.err.println("[ERREUR] ID machine invalide: " + args[0]);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("[ERREUR] Impossible de se connecter au serveur: " + e.getMessage());
            System.err.println("\nAssurez-vous que:");
            System.err.println("  1. Le service de noms est demarre (tnameserv)");
            System.err.println("  2. Le serveur CORBA est demarre");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
