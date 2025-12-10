package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant la station d'assemblage
 */
public class AssemblyStation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum StationState {
        IDLE,           // Inactive
        ASSEMBLING,     // En cours d'assemblage
        WAITING_PARTS   // En attente de pièces
    }
    
    private int id;
    private String name;
    private StationState state;
    private Map<String, StorageZone> storageZones;
    private int assembledProducts;
    
    public AssemblyStation() {
        this.storageZones = new HashMap<>();
        this.state = StationState.IDLE;
        this.assembledProducts = 0;
    }
    
    public AssemblyStation(int id, String name) {
        this.id = id;
        this.name = name;
        this.state = StationState.IDLE;
        this.storageZones = new HashMap<>();
        this.assembledProducts = 0;
        initializeStorageZones();
    }
    
    private void initializeStorageZones() {
        storageZones.put("Part_TYPE_A", new StorageZone("Part_TYPE_A", 20));
        storageZones.put("Part_TYPE_B", new StorageZone("Part_TYPE_B", 20));
        storageZones.put("Part_TYPE_C", new StorageZone("Part_TYPE_C", 20));
        storageZones.put("Part_TYPE_D", new StorageZone("Part_TYPE_D", 20));
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
    
    public StationState getState() {
        return state;
    }
    
    public void setState(StationState state) {
        this.state = state;
    }
    
    public Map<String, StorageZone> getStorageZones() {
        return storageZones;
    }
    
    public void setStorageZones(Map<String, StorageZone> storageZones) {
        this.storageZones = storageZones;
    }
    
    public int getAssembledProducts() {
        return assembledProducts;
    }
    
    public void setAssembledProducts(int assembledProducts) {
        this.assembledProducts = assembledProducts;
    }
    
    public void incrementAssembled() {
        this.assembledProducts++;
    }
    
    public StorageZone getStorageZone(String partType) {
        return storageZones.get(partType);
    }
    
    @Override
    public String toString() {
        return String.format("AssemblyStation[id=%d, name=%s, state=%s, assembled=%d, zones=%d]",
                id, name, state, assembledProducts, storageZones.size());
    }
}
