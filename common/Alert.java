package common;

import java.io.Serializable;
import java.util.Date;

/**
 * Classe représentant une alerte/anomalie dans le système
 */
public class Alert implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum AlertType {
        MACHINE_FAILURE,        // Panne machine
        STORAGE_FULL,          // Zone de stockage pleine
        STORAGE_EMPTY,         // Zone de stockage vide
        STORAGE_LOW,           // Zone de stockage basse
        PRODUCTION_CHANGE,     // Changement de production
        MAINTENANCE_REQUIRED   // Maintenance requise
    }
    
    public enum AlertPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
    
    private int id;
    private AlertType type;
    private AlertPriority priority;
    private int sourceId;  // ID de la machine ou station
    private String sourceName;
    private String message;
    private Date timestamp;
    private boolean resolved;
    
    public Alert() {
        this.timestamp = new Date();
        this.resolved = false;
    }
    
    public Alert(AlertType type, int sourceId, String sourceName, String message) {
        this.type = type;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.message = message;
        this.timestamp = new Date();
        this.resolved = false;
        this.priority = determinePriority(type);
    }
    
    private AlertPriority determinePriority(AlertType type) {
        switch (type) {
            case MACHINE_FAILURE:
                return AlertPriority.CRITICAL;
            case STORAGE_EMPTY:
                return AlertPriority.HIGH;
            case STORAGE_FULL:
                return AlertPriority.HIGH;
            case STORAGE_LOW:
                return AlertPriority.MEDIUM;
            case MAINTENANCE_REQUIRED:
                return AlertPriority.MEDIUM;
            case PRODUCTION_CHANGE:
                return AlertPriority.LOW;
            default:
                return AlertPriority.LOW;
        }
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public AlertType getType() {
        return type;
    }
    
    public void setType(AlertType type) {
        this.type = type;
    }
    
    public AlertPriority getPriority() {
        return priority;
    }
    
    public void setPriority(AlertPriority priority) {
        this.priority = priority;
    }
    
    public int getSourceId() {
        return sourceId;
    }
    
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isResolved() {
        return resolved;
    }
    
    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
    
    @Override
    public String toString() {
        return String.format("[%s] Alert[type=%s, priority=%s, from=%s, msg=%s, resolved=%s]",
                timestamp, type, priority, sourceName, message, resolved);
    }
}
