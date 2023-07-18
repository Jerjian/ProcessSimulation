//mport java.util.*;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//class Process {
//    int id;
//    int instructions;
//    Queue<Integer> io_requests = new LinkedList<>();
//    Queue<Integer> io_devices = new LinkedList<>();
//    int[] registers = new int[2];
//    String state = "Ready";
//
//    public Process(int id, int instructions, Queue<Integer> io_requests, Queue<Integer> io_devices) {
//        this.id = id;
//        this.instructions = instructions;
//        this.io_requests = io_requests;
//        this.io_devices = io_devices;
//    }
//
//    public void execute() {
//        if (instructions > 0) {
//            instructions--;
//            registers[0] = new Random().nextInt(100) + 1;
//            registers[1] = new Random().nextInt(100) + 1;
//            if (!io_requests.isEmpty() && instructions == io_requests.peek()) {
//                state = "Waiting";
//                io_requests.remove();
//            }
//        } else if (instructions == 0) {
//            state = "Terminated";
//        }
//    }
//
//    public void displayPCB() {
//        System.out.println("Process ID: " + id);
//        System.out.println("State: " + state);
//        System.out.println("Registers: " + Arrays.toString(registers));
//        System.out.println("IO Requests: " + io_requests);
//        System.out.println("IO Devices: " + io_devices);
//        System.out.println();
//    }
//}
//
//class OS_Simulator {
//    Queue<Process> ready_queue = new LinkedList<>();
//    Map<Integer, Queue<Process>> wait_queue = new HashMap<>();
//    Process current_process = null;
//
//    public OS_Simulator() {
//        wait_queue.put(1, new LinkedList<>());
//        wait_queue.put(2, new LinkedList<>());
//    }
//
//    public void loadProcess(String file) {
//        try {
//            Scanner sc = new Scanner(new File(file));
//            if (sc.hasNextLine()) {
//                sc.nextLine(); // Skip the header line
//            }
//
//            List<int[]> processList = new ArrayList<>(); // Multi-dimensional array to store process data
//
//            while (sc.hasNextLine()) {
//                String line = sc.nextLine().trim();
//                if (line.isEmpty()) {
//                    continue; // Skip empty lines
//                }
//
//                // Use regular expression pattern to extract fields
//                String pattern = "(\\d+),\\s*(\\d+),\\s*\\[(.*?)],\\s*\\[(.*?)\\]";
//                Pattern regex = Pattern.compile(pattern);
//                Matcher matcher = regex.matcher(line);
//
//                if (matcher.matches()) {
//                    int id = Integer.parseInt(matcher.group(1).trim());
//                    int instructions = Integer.parseInt(matcher.group(2).trim());
//                    String ioRequestsStr = matcher.group(3).trim();
//                    String ioDevicesStr = matcher.group(4).trim();
//
//                    Queue<Integer> io_requests = parseQueue(ioRequestsStr);
//                    Queue<Integer> io_devices = parseQueue(ioDevicesStr);
//
//                    int[] process = { id, instructions };
//                    processList.add(process);
//
//                    Process p = new Process(id, instructions, io_requests, io_devices);
//                    ready_queue.add(p);
//                }
//            }
//            sc.close();
//
//            // Sort the processList based on process ID
//            Collections.sort(processList, (a, b) -> Integer.compare(a[0], b[0]));
//
//            // Clear the ready_queue and load processes based on the sorted processList
//            ready_queue.clear();
//            for (int[] process : processList) {
//                int id = process[0];
//
//                // Find the process with the matching ID and add it to the ready_queue
//                for (Process p : ready_queue) {
//                    if (p.id == id) {
//                        ready_queue.add(p);
//                        break;
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Queue<Integer> parseQueue(String str) {
//        Queue<Integer> queue = new LinkedList<>();
//        if (!str.isEmpty()) {
//            str = str.substring(1, str.length() - 1); // Remove brackets
//            String[] elements = str.split("\\s*,\\s*");
//            for (String element : elements) {
//                queue.add(Integer.parseInt(element));
//            }
//        }
//        return queue;
//    }
//
//    public void run() {
//        System.out.println("Starting the run method.");
//
//        // Load the first process if available
//        if (!ready_queue.isEmpty()) {
//            current_process = ready_queue.poll();
//            System.out.println("Starting execution of process: " + current_process.id);
//        }
//
//        while (current_process != null || !ready_queue.isEmpty() || !wait_queue.get(1).isEmpty() || !wait_queue.get(2).isEmpty()) {
//            System.out.println("Inside the run loop.");
//
//            System.out.println("Current process: " + (current_process != null ? current_process.id : "None"));
//
//            if (current_process != null) {
//                if (current_process.state.equals("Ready")) {
//                    current_process.execute();
//                    if (current_process.state.equals("Waiting")) {
//                        wait_queue.get(current_process.io_devices.poll()).add(current_process);
//                        current_process = null;
//                    } else if (current_process.state.equals("Terminated")) {
//                        current_process = null;
//                    }
//                }
//
//                System.out.println("Executing process: " + (current_process != null ? current_process.id : "None"));
//                System.out.println("Ready queue: " + ready_queue);
//                System.out.println("Waiting queue IO1: " + wait_queue.get(1));
//                System.out.println("Waiting queue IO2: " + wait_queue.get(2));
//
//                // Display PCB content for each process
//                for (Process process : ready_queue) {
//                    process.displayPCB();
//                }
//                for (Queue<Process> ioQueue : wait_queue.values()) {
//                    for (Process process : ioQueue) {
//                        process.displayPCB();
//                    }
//                }
//            }
//
//            // Delay IO requests
//            for (int key : wait_queue.keySet()) {
//                Queue<Process> ioQueue = wait_queue.get(key);
//                if (!ioQueue.isEmpty()) {
//                    Process process = ioQueue.peek();
//                    process.instructions--;
//                    if (!process.io_requests.isEmpty() && process.instructions == process.io_requests.peek()) {
//                        process.io_requests.poll();
//                        if (process.io_requests.isEmpty()) {
//                            process.state = "Ready";
//                            ready_queue.add(ioQueue.poll());
//                        }
//                    }
//                }
//            }
//        }
//
//        System.out.println("Exiting the run method.");
//    }
//
//
//}
//
//public class A1 {
//    public static void main(String[] args) {
//        OS_Simulator os = new OS_Simulator();
//
//        String filePath = "info.txt";
//        os.loadProcess(filePath);
//
//        os.run();
//    }
//}