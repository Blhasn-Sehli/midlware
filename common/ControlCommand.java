package common;

import java.io.Serializable;

/**
 * Classe représentant une commande de contrôle envoyée par le contrôleur
 */
public class ControlCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum CommandType {
        START_MACHINE,       // Démarrer une machine
        STOP_MACHINE,        // Arrêter une machine
        REPLACE_MACHINE,     // Remplacer une machine par une autre
        START_ASSEMBLY,      // Démarrer l'assemblage
        STOP_ASSEMBLY,       // Arrêter l'assemblage
        GET_STATUS,          // Obtenir le statut
        MAINTENANCE_MODE,    // Mode maintenance
        EMERGENCY_STOP       // Arrêt d'urgence
    }
    
    private CommandType type;
    private int targetId;      // ID de la cible (machine ou station)
    private int replacementId;  // ID de remplacement (pour REPLACE_MACHINE)
    private String parameters;
    private String message;
    
    public ControlCommand() {}
    
    public ControlCommand(CommandType type, int targetId, String message) {
        this.type = type;
        this.targetId = targetId;
        this.message = message;
    }
    
    public ControlCommand(CommandType type, int targetId, int replacementId, String message) {
        this.type = type;
        this.targetId = targetId;
        this.replacementId = replacementId;
        this.message = message;
    }
    
    // Getters and Setters
    public CommandType getType() {
        return type;
    }
    
    public void setType(CommandType type) {
        this.type = type;
    }
    
    public int getTargetId() {
        return targetId;
    }
    
    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
    
    public int getReplacementId() {
        return replacementId;
    }
    
    public void setReplacementId(int replacementId) {
        this.replacementId = replacementId;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    @Override
    public String toString() {
        return String.format("ControlCommand[type=%s, target=%d, replacement=%d, msg=%s]",
                type, targetId, replacementId, message);
    }
}
