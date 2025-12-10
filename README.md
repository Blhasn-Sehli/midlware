# Système de Contrôle de Production - Architecture Multi-Client

## 📁 Structure

```
projet-client-serveur/
├── common/                          # Classes partagées (Socket uniquement)
│   ├── Machine.java
│   ├── AssemblyStation.java
│   ├── StorageZone.java
│   ├── Alert.java
│   ├── ControlCommand.java
│   └── ProductionMessage.java
│
├── socket-implementation/           # Implémentation Socket
│   ├── ProductionControlServer.java # Serveur Socket
│   └── MachineClient.java          # Client Socket
│
├── corba-implementation/            # Implémentation CORBA
│   ├── ProductionControl.idl       # Interface IDL
│   ├── ProductionControlServerCORBA.java  # Serveur CORBA
│   └── MachineClientCORBA.java     # Client CORBA
│
├── bin/                            # Fichiers compilés
│
├── Scripts Socket:
│   ├── compile-socket.bat
│   ├── run-socket-server.bat
│   └── run-machine.bat
│
├── Scripts CORBA:
│   ├── compile-corba.bat
│   ├── run-nameservice.bat
│   ├── run-corba-server.bat
│   └── run-corba-machine.bat
│
└── Documentation/
    ├── DEMARRAGE-RAPIDE.txt  (Socket)
    ├── DEMARRAGE-CORBA.txt   (CORBA)
    └── GUIDE-CORBA.txt       (Guide détaillé CORBA)
```

## 🚀 Démarrage Rapide

### OPTION 1: Socket (Plus simple)

#### 1. Compiler
```bash
compile-socket.bat
```

#### 2. Démarrer le serveur
```bash
run-socket-server.bat
```

#### 3. Démarrer les machines (terminaux séparés)
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

### OPTION 2: CORBA (Standard industriel)

#### 1. Compiler
```bash
compile-corba.bat
```

#### 2. Démarrer le service de noms
```bash
run-nameservice.bat
```

#### 3. Démarrer le serveur CORBA
```bash
run-corba-server.bat
```

#### 4. Démarrer les machines (terminaux séparés)
```bash
# Terminal 1 - Machine M1
run-corba-machine.bat 1

# Terminal 2 - Machine M3
run-corba-machine.bat 3

# Terminal 3 - Machine M4
run-corba-machine.bat 4

# Terminal 4 - Machine M5
run-corba-machine.bat 5

# Terminal 5 (optionnel) - Machine M2 (backup M1)
run-corba-machine.bat 2
```

## 💡 Concept

**Chaque machine = 1 client indépendant = 1 terminal séparé**

- Un client contrôle UNIQUEMENT sa propre machine
- L'assemblage démarre automatiquement quand toutes les machines sont actives
- M2 remplace automatiquement M1 en cas de panne

**Deux implémentations avec la MÊME logique métier:**
- **Socket**: Communication TCP/IP directe (plus simple)
- **CORBA**: Middleware ORB avec IDL (standard industriel)

## 📋 Types de Machines

- **M1, M2**: TYPE_A (backup)
- **M3**: TYPE_B
- **M4**: TYPE_C
- **M5**: TYPE_D

## 📚 Documentation

**Socket:**
- **DEMARRAGE-RAPIDE.txt** pour démarrage express
- **COMMANDES.txt** pour aide-mémoire

**CORBA:**
- **DEMARRAGE-CORBA.txt** pour démarrage express
- **GUIDE-CORBA.txt** pour guide complet
