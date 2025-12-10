#!/bin/bash

echo "========================================"
echo "  LANCEMENT CLIENT MACHINE"
echo "========================================"
echo ""

if [ -z "$1" ]; then
    echo "Usage: ./run-machine.sh <machine_id>"
    echo ""
    echo "Exemples:"
    echo "  ./run-machine.sh 1    <- Machine M1"
    echo "  ./run-machine.sh 2    <- Machine M2 (backup)"
    echo "  ./run-machine.sh 3    <- Machine M3"
    echo "  ./run-machine.sh 4    <- Machine M4"
    echo "  ./run-machine.sh 5    <- Machine M5"
    echo ""
    exit 1
fi

echo "Demarrage Machine M$1..."
echo ""
java -cp bin socket.client.MachineClient $1
