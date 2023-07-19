import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files


class ProcessSimulation {
    public static void main(String[] args) {
        ArrayList<String> processes = new ArrayList<>();

        try {
            File myObj = new File("./src/text.txt");
            Scanner myReader = new Scanner(myObj);
            myReader.nextLine(); //skip first line
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                processes.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (int i = 0; i < processes.size(); i++) {
            System.out.println(processes.get(i));
        }



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

    //PID
    ProcessState processState;
    int programCounter;
    int[] registers = new int[2]; //each instructions can use 2 registers to execute
    int clockTimeSinceStart;
    int ioDeviceAllocatedTo;

    public PCB() {
        this.processState = ProcessState.NEW;
        this.programCounter = 0; //PC starts at 0 when PCB is created
        this.registers[0] = (int)(Math.random() * 10); //Random registers up to 10
        this.registers[1] = (int)(Math.random() * 10); //Random registers up to 10
        this.clockTimeSinceStart = 0; //clock time starts start at 0
        //todo: keep track of which IOdevice it is allocated to while waiting?
        this.ioDeviceAllocatedTo = 0;
    }
}


class ProcessScheduler {
    Queue<Process> readyQueue = new LinkedList<>();
    //TODO: Wait queue for each OI device?
    Queue<Process> waitQueue1 = new LinkedList<>();
    Queue<Process> waitQueue2 = new LinkedList<>();

    //start by adding a process to the ready queue.
    public ProcessScheduler(Process process) {
        this.addProcessToReadyQueue(process);
    }

    public void addProcessToReadyQueue(Process process){
        process.pcb.processState = ProcessState.READY;
        readyQueue.add(process);
    }

    public void addProcessToWaitQueue(Process process, int IOdeviceRequested){
        if (IOdeviceRequested == 1){
            process.pcb.processState = ProcessState.WAITING;
            process.pcb.ioDeviceAllocatedTo = 1;
            waitQueue1.add(process);
        }
        else if (IOdeviceRequested == 2) {
            process.pcb.processState = ProcessState.WAITING;
            process.pcb.ioDeviceAllocatedTo = 2;
            waitQueue2.add(process);
        }
        else{
            System.out.println("Not valid IO device");
        }
    }

    //TODO: add context switch , each process can only use 2 instructions at a time.
    //TODO: Add timer of 2 per process
    public void execute() {
        while (!readyQueue.isEmpty()) {
            Process currentProcess = readyQueue.poll();
            currentProcess.pcb.processState = ProcessState.RUNNING;

            //TODO: Add context switching during the execution of the current process

//            if(currentProcess.pcb.clockTimeSinceStart % 2 != 0){
//            }
            //execute current process
            for (int i = 0; i < currentProcess.nbrOfInstructions; i++) {
                currentProcess.pcb.programCounter++;
                currentProcess.pcb.clockTimeSinceStart++;

                //if ioRequest, add it to the correct waitqueue
                if (currentProcess.ioRequests.contains(i)) {
                    int device = currentProcess.ioDevices.remove(0);
                    currentProcess.pcb.processState = ProcessState.WAITING;
                    addProcessToWaitQueue(currentProcess,device); //adding process to wait queue
                    break;
                }

                //Last instruction, so set it as terminated.
                if (i == currentProcess.nbrOfInstructions - 1) {
                    currentProcess.pcb.processState = ProcessState.TERMINATED;
                }
            }

            //Check in the waitqueue, and add to ready queue.
            for (int i = 0; i < waitQueue1.size(); i++) {
                Process waitingProcess = waitQueue1.peek();
                if (waitingProcess != null && waitingProcess.pcb.processState.equals(ProcessState.WAITING)) {
                    //TODO: Take 5 time units to run. So take 5 instructions. Is this the right way of doing it, just increase clocktime by 5?
                    waitingProcess.pcb.clockTimeSinceStart = waitingProcess.pcb.clockTimeSinceStart + 5;
                    waitingProcess.pcb.processState = ProcessState.READY;
                    waitingProcess.pcb.ioDeviceAllocatedTo = 0;
                    readyQueue.add(waitQueue1.poll());
                    //TODO: I can add only 1 to the ready queue at a time from the waitqueue right?
                    break;
                }
            }
            for (int i = 0; i < waitQueue2.size(); i++) {
                Process waitingProcess = waitQueue2.peek();

                if (waitingProcess != null && waitingProcess.pcb.processState.equals(ProcessState.WAITING)) {
                    //TODO: Take 5 time units to run. So take 5 instructions. Is this the right way of doing it, just increase clocktime by 5?
                    waitingProcess.pcb.clockTimeSinceStart = waitingProcess.pcb.clockTimeSinceStart + 5;
                    waitingProcess.pcb.processState = ProcessState.READY;
                    waitingProcess.pcb.ioDeviceAllocatedTo = 0;
                    readyQueue.add(waitQueue2.poll());
                    //TODO: I can add only 1 to the ready queue at a time from the waitqueue right?
                    break;
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

/*additional notes

While one process is waiting, the 2nd process can execute.

PCB -> class, print the object, every variable gets printed
which process do we proritize to go back to ready queue

non-preemtive scheduling.

is clock x the IO request.
 */