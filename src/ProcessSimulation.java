import java.io.*;
import java.util.*;


class ProcessSimulation {
    public static void main(String[] args) throws FileNotFoundException {
        ArrayList<String> processesDataString = new ArrayList<>();
        ArrayList<Process> processes = new ArrayList<>();

        //Read file
        File file;
        FileOutputStream fos = null;
        PrintStream fileOut = null;
        try {
            file = new File("output.txt");
            fos = new FileOutputStream(file);
            fileOut = new PrintStream(fos);
            System.setOut(fileOut);


            File myObj = new File("./src/text.txt");
            Scanner myReader = new Scanner(myObj);
            myReader.nextLine(); //skip first line
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                processesDataString.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (int i = 0; i < processesDataString.size(); i++) {
            processes.add(parseProcessDataString(processesDataString.get(i)));
        }


        ProcessScheduler ps = new ProcessScheduler(processes);
        ps.execute();
        try{
            fileOut.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Process parseProcessDataString(String s){

        String[] parts = s.split(", ");
        int PID = Integer.parseInt(parts[0]); //good
        int nbrOfInstructions = Integer.parseInt(parts[1]); //good

        ArrayList<Integer> ioRequests = new ArrayList<>();
        ArrayList<Integer> ioDevices = new ArrayList<>();

        String part2 = parts[2].replace("[", "").replace("]", ",");
        String[] part2Strings = part2.split(", ");

        for (int i = 0; i < part2Strings.length; i++) {
            String[] numbersString = part2Strings[i].split(",");
            for (int j = 0; j < numbersString.length; j++) {
                if(numbersString[j] == "") continue;
                int currentNum = Integer.parseInt(numbersString[j]);
                if (i==0){
                    ioRequests.add(currentNum);
                }else{
                    ioDevices.add(currentNum);
                }
            }
        }
        Process process = new Process(PID, nbrOfInstructions,  ioRequests,  ioDevices);
        return  process;
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

    @Override
    public String toString() {
        return "Process{" +
                "PID=" + PID +
                ", nbrOfInstructions=" + nbrOfInstructions +
                ", pcb=" + pcb +
                ", ioRequests=" + ioRequests +
                ", ioDevices=" + ioDevices +
                '}';
    }
}

class PCB{

    //PID
    ProcessState processState;
    int programCounter;
    int[] registers = new int[2]; //each instructions can use 2 registers to execute
    int ioDeviceAllocatedTo;
    int clockTimeSinceIORequest;

    public PCB() {
        this.processState = ProcessState.NEW;
        this.programCounter = 0; //PC starts at 0 when PCB is created
        this.registers[0] = (int)(Math.random() * 100); //Random registers up to 10
        this.registers[1] = (int)(Math.random() * 100); //Random registers up to 10
        this.ioDeviceAllocatedTo = 0; //tracks while io devicde it is allocated to
        this.clockTimeSinceIORequest = 0; //when process gets an IOrequest, we need to track clock time up to 5
    }

    @Override
    public String toString() {
        return "PCB{" +
                "processState=" + processState +
                ", programCounter=" + programCounter +
                ", registers=" + Arrays.toString(registers) +
                ", ioDeviceAllocatedTo=" + ioDeviceAllocatedTo +
                ", clockTimeSinceIORequest=" + clockTimeSinceIORequest +
                '}';
    }
}


class ProcessScheduler {
    Queue<Process> readyQueue = new LinkedList<>();
    Queue<Process> waitQueue1 = new LinkedList<>();
    Queue<Process> waitQueue2 = new LinkedList<>();
    ArrayList<Process> processes = new ArrayList<>();

    //start by adding a process to the ready queue.
    public ProcessScheduler(ArrayList<Process> processes) {
        this.processes = processes;
        for (Process process : processes) {
            this.addProcessToReadyQueue(process);
        }


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

    //Check the first element of the  waitqueue, add +1 to clockTimeSinceIORequest
     public static void updateWaitQueueTime(Queue<Process> waitQueue){
         Process waitingProcess = waitQueue.peek();

         if (waitingProcess != null
                 && waitingProcess.pcb.processState.equals(ProcessState.WAITING)
                 && waitingProcess.pcb.clockTimeSinceIORequest < 5) {
             waitingProcess.pcb.clockTimeSinceIORequest++;
         }
     }

     public static void addWaitingProcessToReadyQueueOrTerminated(Queue<Process> waitingQueue, Queue<Process> readyQueue){
        Process waitingProcess = waitingQueue.peek();
         if (waitingProcess != null
                 && waitingProcess.pcb.processState.equals(ProcessState.WAITING)
                 && waitingProcess.pcb.clockTimeSinceIORequest == 5) {

             if (waitingProcess.nbrOfInstructions == waitingProcess.pcb.programCounter){
                 waitingProcess.pcb.processState = ProcessState.TERMINATED;
                 waitingProcess.pcb.ioDeviceAllocatedTo = 0;
                 waitingProcess.pcb.clockTimeSinceIORequest = 0;
                 waitingQueue.poll();

             }else{
                 waitingProcess.pcb.processState = ProcessState.READY;
                 waitingProcess.pcb.ioDeviceAllocatedTo = 0;
                 waitingProcess.pcb.clockTimeSinceIORequest = 0;
                 waitingQueue.poll(); //todo: find a more elegant way to do this
                 readyQueue.add(waitingProcess);
             }

         }
     }


    //TODO: add context switch , each process can only use 2 instructions at a time.
    //TODO: Add timer of 2 per process
    public void execute() {
        while (!readyQueue.isEmpty() || !waitQueue1.isEmpty() || !waitQueue2.isEmpty()) {

            Process currentProcess = readyQueue.poll();
            if (currentProcess == null){
                //todo: waitqueu1 or waitqueue2 are NOT empty, run them until readyqueue
                updateWaitQueueTime(waitQueue1);
                updateWaitQueueTime(waitQueue2);
                printInfo();
                addWaitingProcessToReadyQueueOrTerminated(waitQueue1, readyQueue);
                addWaitingProcessToReadyQueueOrTerminated(waitQueue2, readyQueue);
            }else{
                currentProcess.pcb.processState = ProcessState.RUNNING;

                //TODO: Add context switching during the execution of the current process

                //each loop is one instruction
                for (int i = 0; i < 2; i++) {
                    currentProcess.pcb.programCounter++;
                    updateWaitQueueTime(waitQueue1);
                    updateWaitQueueTime(waitQueue2);

                    //if ioRequest, add it to the correct waitqueue
                    if (currentProcess.ioRequests.contains(currentProcess.pcb.programCounter)) {
                        printInfo(currentProcess);
                        int device = currentProcess.ioDevices.get(currentProcess.ioRequests.indexOf(currentProcess.pcb.programCounter));
                        addProcessToWaitQueue(currentProcess, device); //adding process to wait queue
                        break;
                    }

                    //Last instruction, so set it as terminated.
                    if (currentProcess.pcb.programCounter == currentProcess.nbrOfInstructions) {
                        printInfo(currentProcess);
                        currentProcess.pcb.processState = ProcessState.TERMINATED;
                        break;
                    }

                    printInfo(currentProcess);
                    //Check in the waitQueue if there's process ready to be added to ready queue.
                    addWaitingProcessToReadyQueueOrTerminated(waitQueue1, readyQueue);
                    addWaitingProcessToReadyQueueOrTerminated(waitQueue2, readyQueue);
                }


                if (currentProcess.pcb.processState.equals(ProcessState.RUNNING)){
                    currentProcess.pcb.processState = ProcessState.READY;
                    readyQueue.add(currentProcess);
                }
            }
        }
        //print all terminated stuff
        printInfo();

    }


    //Print all info
    public void printInfo(Process currentProcess){
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Running");
        System.out.println("\t"+currentProcess);
        System.out.println("Ready Queue");

        for (Process process : readyQueue) {
            System.out.println("\t"+process);
        }
        System.out.println("Wait Queue 1");
        for (int i = 0; i < waitQueue1.size(); i++) {
            for (Process process : waitQueue1) {
                System.out.println("\t"+process);
            }
        }
        System.out.println("Wait Queue 2");
        for (int i = 0; i < waitQueue2.size(); i++) {
            for (Process process : waitQueue2) {
                System.out.println("\t"+process);
            }
        }
        System.out.println("Terminated");
        for (Process process : processes) {
            if (process.pcb.processState.equals(ProcessState.TERMINATED)){
                System.out.println("\t" + process);
            }
        }

    }
    public void printInfo(){
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Running");
        System.out.println("Ready Queue");

        for (Process process : readyQueue) {
            System.out.println("\t"+process);
        }
        System.out.println("Wait Queue 1");
        for (int i = 0; i < waitQueue1.size(); i++) {
            for (Process process : waitQueue1) {
                System.out.println("\t"+process);
            }
        }
        System.out.println("Wait Queue 2");
        for (int i = 0; i < waitQueue2.size(); i++) {
            for (Process process : waitQueue2) {
                System.out.println("\t"+process);
            }
        }
        System.out.println("Terminated");
        for (Process process : processes) {
            if (process.pcb.processState.equals(ProcessState.TERMINATED)){
                System.out.println("\t" + process);
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