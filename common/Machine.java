package common;

import java.io.Serializable;

/**
 * Classe représentant une machine de production
 */
public class Machine implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MachineState {
        STOPPED,      // Arrêtée
        RUNNING,      // En marche
        MAINTENANCE,  // En maintenance
        FAILURE       // En panne
    }
    
    public enum MachineType {
        TYPE_A,  // Pour produire la partie A
        TYPE_B,  // Pour produire la partie B
        TYPE_C,  // Pour produire la partie C
        TYPE_D   // Pour produire la partie D
    }
    
    private int id;
    private String name;
    private MachineType type;
    private MachineState state;
    private int productionCount;
    private String currentPart;
    
    public Machine() {
        this.state = MachineState.STOPPED;
        this.productionCount = 0;
    }
    
    public Machine(int id, String name, MachineType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.state = MachineState.STOPPED;
        this.productionCount = 0;
        this.currentPart = "Part_" + type.name();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public MachineType getType() {
        return type;
    }
    
    public void setType(MachineType type) {
        this.type = type;
    }
    
    public MachineState getState() {
        return state;
    }
    
    public void setState(MachineState state) {
        this.state = state;
    }
    
    public int getProductionCount() {
        return productionCount;
    }
    
    public void setProductionCount(int productionCount) {
        this.productionCount = productionCount;
    }
    
    public String getCurrentPart() {
        return currentPart;
    }
    
    public void setCurrentPart(String currentPart) {
        this.currentPart = currentPart;
    }
    
    public void incrementProduction() {
        this.productionCount++;
    }
    
    @Override
    public String toString() {
        return String.format("Machine[id=%d, name=%s, type=%s, state=%s, production=%d]",
                id, name, type, state, productionCount);
    }
}
