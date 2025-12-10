package common;

import java.io.Serializable;

/**
 * Classe représentant une zone de stockage pour une partie spécifique
 */
public class StorageZone implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String partType;
    private int currentQuantity;
    private int maxCapacity;
    private int minThreshold;
    
    public StorageZone() {
        this.currentQuantity = 0;
        this.minThreshold = 5;
    }
    
    public StorageZone(String partType, int maxCapacity) {
        this.partType = partType;
        this.maxCapacity = maxCapacity;
        this.currentQuantity = 0;
        this.minThreshold = (int)(maxCapacity * 0.2); // 20% du max
    }
    
    public boolean isEmpty() {
        return currentQuantity == 0;
    }
    
    public boolean isFull() {
        return currentQuantity >= maxCapacity;
    }
    
    public boolean isLow() {
        return currentQuantity <= minThreshold;
    }
    
    public boolean addPart() {
        if (currentQuantity < maxCapacity) {
            currentQuantity++;
            return true;
        }
        return false;
    }
    
    public boolean removePart() {
        if (currentQuantity > 0) {
            currentQuantity--;
            return true;
        }
        return false;
    }
    
    // Getters and Setters
    public String getPartType() {
        return partType;
    }
    
    public void setPartType(String partType) {
        this.partType = partType;
    }
    
    public int getCurrentQuantity() {
        return currentQuantity;
    }
    
    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }
    
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public int getMinThreshold() {
        return minThreshold;
    }
    
    public void setMinThreshold(int minThreshold) {
        this.minThreshold = minThreshold;
    }
    
    @Override
    public String toString() {
        return String.format("StorageZone[part=%s, qty=%d/%d, status=%s]",
                partType, currentQuantity, maxCapacity, 
                isEmpty() ? "EMPTY" : isFull() ? "FULL" : isLow() ? "LOW" : "OK");
    }
}
