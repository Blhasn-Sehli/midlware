package common;

import java.io.Serializable;

/**
 * Classe pour les messages échangés dans le système de production
 */
public class ProductionMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        ALERT,              // Alerte/Anomalie
        COMMAND,            // Commande de contrôle
        STATUS_UPDATE,      // Mise à jour de statut
        PRODUCTION_DATA,    // Données de production
        ACK                 // Acquittement
    }
    
    private MessageType type;
    private Object payload;  // Alert, ControlCommand, Machine, AssemblyStation, etc.
    private boolean success;
    private String responseMessage;
    
    public ProductionMessage() {}
    
    public ProductionMessage(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.success = true;
    }
    
    public ProductionMessage(MessageType type, Object payload, boolean success, String responseMessage) {
        this.type = type;
        this.payload = payload;
        this.success = success;
        this.responseMessage = responseMessage;
    }
    
    // Getters and Setters
    public MessageType getType() {
        return type;
    }
    
    public void setType(MessageType type) {
        this.type = type;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getResponseMessage() {
        return responseMessage;
    }
    
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }
    
    @Override
    public String toString() {
        return String.format("ProductionMessage[type=%s, success=%s, msg=%s]",
                type, success, responseMessage);
    }
}
