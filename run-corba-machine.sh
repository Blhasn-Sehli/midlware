#!/bin/bash

echo "========================================"
echo "  CLIENT MACHINE CORBA"
echo "========================================"
echo ""

if [ -z "$1" ]; then
    echo "Usage: ./run-corba-machine.sh <machine_id>"
    echo ""
    echo "Exemples:"
    echo "  ./run-corba-machine.sh 1    <- Machine M1"
    echo "  ./run-corba-machine.sh 2    <- Machine M2 (backup)"
    echo "  ./run-corba-machine.sh 3    <- Machine M3"
    echo "  ./run-corba-machine.sh 4    <- Machine M4"
    echo "  ./run-corba-machine.sh 5    <- Machine M5"
    echo ""
    exit 1
fi

echo "Demarrage Machine M$1 (CORBA)..."
echo ""
java -cp bin MachineClientCORBA $1 -ORBInitialPort 1050 -ORBInitialHost localhost
