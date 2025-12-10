# Système de Contrôle de Production - Architecture Multi-Client

## 📁 Structure

```
projet-client-serveur/
├── common/                          # Classes partagées
│   ├── Machine.java
│   ├── AssemblyStation.java
│   ├── StorageZone.java
│   ├── Alert.java
│   ├── ControlCommand.java
│   └── ProductionMessage.java
│
├── socket-implementation/           # Implémentation Socket
│   ├── ProductionControlServer.java # Serveur de production
│   └── MachineClient.java          # Client machine (distribué)
│
├── bin/                            # Fichiers compilés
│
├── compile-socket.bat              # Script de compilation
├── run-socket-server.bat           # Lancer le serveur
├── run-machine.bat                 # Lancer une machine
└── GUIDE-SIMPLE.txt                # Guide détaillé
```

## 🚀 Démarrage Rapide

### 1. Compiler
```bash
compile-socket.bat
```

### 2. Démarrer le serveur
```bash
run-socket-server.bat
```

### 3. Démarrer les machines (terminaux séparés)
```bash
# Terminal 1 - Machine M1
run-machine.bat 1

# Terminal 2 - Machine M3
run-machine.bat 3

# Terminal 3 - Machine M4
run-machine.bat 4

# Terminal 4 - Machine M5
run-machine.bat 5

# Terminal 5 (optionnel) - Machine M2 (backup M1)
run-machine.bat 2
```

## 💡 Concept

**Chaque machine = 1 client indépendant = 1 terminal séparé**

- Un client contrôle UNIQUEMENT sa propre machine
- L'assemblage démarre automatiquement quand toutes les machines sont actives
- M2 remplace automatiquement M1 en cas de panne

## 📋 Types de Machines

- **M1, M2**: TYPE_A (backup)
- **M3**: TYPE_B
- **M4**: TYPE_C
- **M5**: TYPE_D

## 📚 Documentation

Consultez **GUIDE-SIMPLE.txt** pour des instructions détaillées.
