import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

class ProcessSimulation {
    public static void main(String[] args) {

    }
}

class Process{
    int PID;
    int nbrOfInstructions;
    PCB pcb;
    ArrayList<Integer> ioRequests = new ArrayList<>();
    ArrayList<Integer> ioDevices = new ArrayList<>();


    public Process(int PID, int nbrOfInstructions, ArrayList<Integer> ioRequests, ArrayList<Integer> ioDevices) {
        this.PID = PID;
        this.nbrOfInstructions = nbrOfInstructions;
        this.pcb = new PCB();
        this.ioRequests = ioRequests;
        this.ioDevices = ioDevices;
    }

}

class PCB{
    ProcessState processState;
    int programCounter;
    int[] registers = new int[2]; //each instructions can use 2 registers to execute
    int clockTimeSinceStart;
//    String[] listOfOpenFiles; //MAYBE REMOVE????!!!?!??!?!?!??!?!?!

    public PCB() {
        this.processState = ProcessState.NEW;
        this.programCounter = 0; //PC starts at 0 when PCB is created
        this.registers[0] = (int)(Math.random() * 10); //Random registers up to 10
        this.registers[1] = (int)(Math.random() * 10); //Random registers up to 10
        this.clockTimeSinceStart = 0; //clock time starts start at 0
    }
}


class ProcessScheduler {
    Queue<Process> readyQueue = new LinkedList<>();
    Queue<Process> waitQueue = new LinkedList<>();


    public ProcessScheduler(Process process) {
        this.addProcessToReadyQueue(process); //start by adding a process to the ready queue.
    }

    public void addProcessToReadyQueue(Process process){
        process.pcb.processState = ProcessState.READY;
        readyQueue.add(process);
    }
    public void addProcessToWaitQueue(Process process){
        process.pcb.processState = ProcessState.WAITING;
        waitQueue.add(process);
    }

    //TODO: add context switch , each process can only use 2 instructions at a time.
    public void execute() {
        while (!readyQueue.isEmpty()) {
            Process currentProcess = readyQueue.poll();
            currentProcess.pcb.processState = ProcessState.RUNNING;

            for (int i = 0; i < currentProcess.nbrOfInstructions; i++) {
                currentProcess.pcb.programCounter++;
                currentProcess.pcb.clockTimeSinceStart++;

                if (currentProcess.ioRequests.contains(i)) {
                    int device = currentProcess.ioDevices.remove(0);
                    waitQueue.get(device).add(currentProcess);
                    currentProcess.pcb.processState = ProcessState.WAITING;
                    break;
                }

                if (i == currentProcess.nbrOfInstructions - 1) {
                    currentProcess.pcb.processState = ProcessState.TERMINATED;
                }
            }

            for (Queue<Process> queue : waitQueue.values()) {
                Process waitingProcess = queue.peek();

                if (waitingProcess != null && waitingProcess.pcb.processState.equals(ProcessState.WAITING)) {
                    waitingProcess.pcb.processState = ProcessState.READY;
                    readyQueue.add(queue.poll());
                }
            }
        }
    }
}
enum ProcessState {
    NEW,
    RUNNING,
    WAITING,
    READY,
    TERMINATED
}
