package socket.client;

import common.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Client représentant UNE seule machine
 * Chaque machine est un client indépendant
 */
public class MachineClient {
    private static final String SERVER = "localhost";
    private static final int PORT = 9000;
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;
    
    // ID de la machine gérée par ce client
    private int myMachineId;
    private String myMachineName;
    
    public MachineClient(int machineId) throws IOException {
        this.myMachineId = machineId;
        this.socket = new Socket(SERVER, PORT);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.scanner = new Scanner(System.in);
        
        System.out.println("========================================");
        System.out.println("  CLIENT MACHINE M" + machineId);
        System.out.println("========================================");
        System.out.println("Connecte au serveur " + SERVER + ":" + PORT);
        
        // Récupérer les infos de ma machine
        try {
            getMachineInfo();
        } catch (ClassNotFoundException e) {
            System.err.println("[AVERTISSEMENT] Impossible de recuperer les infos: " + e.getMessage());
            myMachineName = "Machine-M" + machineId;
        }
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
        
        close();
        System.out.println("Deconnexion de Machine M" + myMachineId);
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
    
    private void getMachineInfo() throws IOException, ClassNotFoundException {
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.PRODUCTION_DATA, null);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getPayload();
            @SuppressWarnings("unchecked")
            List<Machine> machines = (List<Machine>) data.get("machines");
            
            for (Machine m : machines) {
                if (m.getId() == myMachineId) {
                    myMachineName = m.getName();
                    System.out.println("Gestion de: " + myMachineName + " (Type: " + m.getType() + ")");
                    break;
                }
            }
        }
    }
    
    private void viewMyMachine() throws IOException, ClassNotFoundException {
        System.out.println("\n+======================================================+");
        System.out.println("|  ETAT DE MA MACHINE M" + myMachineId);
        System.out.println("+======================================================+");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.PRODUCTION_DATA, null);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getPayload();
            @SuppressWarnings("unchecked")
            List<Machine> machines = (List<Machine>) data.get("machines");
            
            for (Machine m : machines) {
                if (m.getId() == myMachineId) {
                    System.out.println("\n  ID:              " + m.getId());
                    System.out.println("  Nom:             " + m.getName());
                    System.out.println("  Type:            " + m.getType());
                    System.out.println("  Etat:            " + getStateIcon(m.getState()) + " " + m.getState());
                    System.out.println("  Piece produite:  " + m.getCurrentPart());
                    System.out.println("  Production:      " + m.getProductionCount() + " pieces");
                    System.out.println("");
                    
                    String status = "";
                    switch (m.getState()) {
                        case RUNNING:
                            status = "[OK] Machine en cours de production";
                            break;
                        case STOPPED:
                            status = "[--] Machine arretee - Utilisez option 2 pour demarrer";
                            break;
                        case FAILURE:
                            status = "[!!] Machine en PANNE - Reparation necessaire";
                            break;
                        case MAINTENANCE:
                            status = "[**] Machine en maintenance";
                            break;
                    }
                    System.out.println("  " + status);
                    break;
                }
            }
        }
    }
    
    private void startMyMachine() throws IOException, ClassNotFoundException {
        System.out.println("\n=== DEMARRAGE DE MA MACHINE M" + myMachineId + " ===");
        
        ControlCommand command = new ControlCommand(
            ControlCommand.CommandType.START_MACHINE, myMachineId, "Demarrage par client");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.COMMAND, command);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            System.out.println("[OK] " + response.getResponseMessage());
            System.out.println("[OK] Machine M" + myMachineId + " est maintenant EN MARCHE!");
        } else {
            System.out.println("[ERREUR] " + response.getResponseMessage());
        }
    }
    
    private void stopMyMachine() throws IOException, ClassNotFoundException {
        System.out.println("\n=== ARRET DE MA MACHINE M" + myMachineId + " ===");
        
        ControlCommand command = new ControlCommand(
            ControlCommand.CommandType.STOP_MACHINE, myMachineId, "Arret par client");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.COMMAND, command);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            System.out.println("[OK] " + response.getResponseMessage());
            System.out.println("[OK] Machine M" + myMachineId + " est maintenant ARRETEE");
        } else {
            System.out.println("[ERREUR] " + response.getResponseMessage());
        }
    }
    
    private void reportFailure() throws IOException, ClassNotFoundException {
        System.out.println("\n=== SIGNALER PANNE - MACHINE M" + myMachineId + " ===");
        System.out.print("Description de la panne: ");
        String description = scanner.nextLine();
        
        Alert alert = new Alert(
            Alert.AlertType.MACHINE_FAILURE,
            myMachineId,
            myMachineName,
            description
        );
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.ALERT, alert);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            System.out.println("[OK] Panne signalee au serveur");
            System.out.println("[OK] " + response.getResponseMessage());
        } else {
            System.out.println("[ERREUR] " + response.getResponseMessage());
        }
    }
    
    private void maintenanceMode() throws IOException, ClassNotFoundException {
        System.out.println("\n=== MODE MAINTENANCE - MACHINE M" + myMachineId + " ===");
        
        ControlCommand command = new ControlCommand(
            ControlCommand.CommandType.MAINTENANCE_MODE, myMachineId, "Maintenance");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.COMMAND, command);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            System.out.println("[OK] " + response.getResponseMessage());
            System.out.println("[OK] Machine M" + myMachineId + " en mode MAINTENANCE");
        } else {
            System.out.println("[ERREUR] " + response.getResponseMessage());
        }
    }
    
    private void viewAllMachines() throws IOException, ClassNotFoundException {
        System.out.println("\n+======================================================+");
        System.out.println("|  TOUTES LES MACHINES (lecture seule)                |");
        System.out.println("+======================================================+");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.PRODUCTION_DATA, null);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getPayload();
            @SuppressWarnings("unchecked")
            List<Machine> machines = (List<Machine>) data.get("machines");
            
            System.out.println("\n+-----+---------------+--------+-----------+-----------+");
            System.out.println("| ID  | Nom           | Type   | Etat      | Prod      |");
            System.out.println("+-----+---------------+--------+-----------+-----------+");
            
            for (Machine m : machines) {
                String status = getStateIcon(m.getState());
                String marker = (m.getId() == myMachineId) ? " <-- MOI" : "";
                System.out.println(String.format("| %-3d | %-13s | %-6s | %s %-6s | %-9d |%s",
                    m.getId(), m.getName(), m.getType(), status, m.getState(), 
                    m.getProductionCount(), marker));
            }
            System.out.println("+-----+---------------+--------+-----------+-----------+");
        }
    }
    
    private void viewProductionStatus() throws IOException, ClassNotFoundException {
        System.out.println("\n+======================================================+");
        System.out.println("|  ETAT GLOBAL DE PRODUCTION (lecture seule)          |");
        System.out.println("+======================================================+");
        
        ProductionMessage request = new ProductionMessage(
            ProductionMessage.MessageType.PRODUCTION_DATA, null);
        out.writeObject(request);
        out.flush();
        
        ProductionMessage response = (ProductionMessage) in.readObject();
        if (response.isSuccess()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.getPayload();
            @SuppressWarnings("unchecked")
            List<Machine> machines = (List<Machine>) data.get("machines");
            AssemblyStation station = (AssemblyStation) data.get("assemblyStation");
            
            long running = machines.stream().filter(m -> m.getState() == Machine.MachineState.RUNNING).count();
            long stopped = machines.stream().filter(m -> m.getState() == Machine.MachineState.STOPPED).count();
            long failed = machines.stream().filter(m -> m.getState() == Machine.MachineState.FAILURE).count();
            
            System.out.println("\n>>> RESUME:");
            System.out.println("  - Machines actives: " + running + "/" + machines.size());
            System.out.println("  - Machines arretees: " + stopped);
            System.out.println("  - Machines en panne: " + failed);
            System.out.println("  - Produits assembles: " + station.getAssembledProducts());
            System.out.println("  - Etat assemblage: " + station.getState());
            
            System.out.println("\n>>> ZONES DE STOCKAGE:");
            for (StorageZone zone : station.getStorageZones().values()) {
                String status = zone.isEmpty() ? "[VIDE]" : zone.isFull() ? "[PLEIN]" : zone.isLow() ? "[BAS]" : "[OK]";
                double pct = (zone.getCurrentQuantity() * 100.0) / zone.getMaxCapacity();
                System.out.println(String.format("  %s %-15s %2d/%-2d (%.0f%%)",
                    status, zone.getPartType(), zone.getCurrentQuantity(), 
                    zone.getMaxCapacity(), pct));
            }
        }
    }
    
    private String getStateIcon(Machine.MachineState state) {
        switch (state) {
            case RUNNING: return "[RUN]";
            case STOPPED: return "[STOP]";
            case FAILURE: return "[FAIL]";
            case MAINTENANCE: return "[MAINT]";
            default: return "[?]";
        }
    }
    
    private ProductionMessage sendMessage(ProductionMessage message) throws IOException, ClassNotFoundException {
        out.writeObject(message);
        out.flush();
        return (ProductionMessage) in.readObject();
    }
    
    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            if (scanner != null) scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("========================================");
            System.out.println("  CLIENT MACHINE - Demarrage");
            System.out.println("========================================");
            System.out.println("\nUsage: java socket.client.MachineClient <machine_id>");
            System.out.println("\nExemples:");
            System.out.println("  java -cp bin socket.client.MachineClient 1   <- Gerer Machine M1");
            System.out.println("  java -cp bin socket.client.MachineClient 2   <- Gerer Machine M2");
            System.out.println("  java -cp bin socket.client.MachineClient 3   <- Gerer Machine M3");
            System.out.println("  java -cp bin socket.client.MachineClient 4   <- Gerer Machine M4");
            System.out.println("  java -cp bin socket.client.MachineClient 5   <- Gerer Machine M5");
            System.out.println("\nChaque client ne peut gerer QUE sa propre machine!");
            System.exit(1);
        }
        
        try {
            int machineId = Integer.parseInt(args[0]);
            if (machineId < 1 || machineId > 5) {
                System.err.println("[ERREUR] ID machine doit etre entre 1 et 5");
                System.exit(1);
            }
            
            MachineClient client = new MachineClient(machineId);
            client.run();
        } catch (NumberFormatException e) {
            System.err.println("[ERREUR] ID machine invalide: " + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("[ERREUR] Impossible de se connecter au serveur: " + e.getMessage());
            System.err.println("\nAssurez-vous que le serveur est demarre:");
            System.err.println("  java -cp bin socket.server.ProductionControlServer");
            System.exit(1);
        }
    }
}
