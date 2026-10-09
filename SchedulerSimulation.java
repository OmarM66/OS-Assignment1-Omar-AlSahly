import java.util.LinkedList;
import java.util.Queue;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

// ANSI Color Codes for enhanced terminal output
class Colors {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String CYAN = "\u001B[36m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String BLUE = "\u001B[34m";
    public static final String RED = "\u001B[31m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String WHITE = "\u001B[37m";
    public static final String BRIGHT_WHITE = "\u001B[97m";
    public static final String BRIGHT_CYAN = "\u001B[96m";
    public static final String BRIGHT_YELLOW = "\u001B[93m";
    public static final String BRIGHT_GREEN = "\u001B[92m";
}

// Class representing a process that implements Runnable to be run by a thread
class Process implements Runnable {
    private String name; // Name of the process
    private int burstTime; // Total time the process requires to complete (in milliseconds)
    private int timeQuantum; // Time slice (time quantum) allowed per CPU access (in milliseconds)
    private int remainingTime; // Time left for the process to finish its execution

    // Feature 1: Store a random process priority from 1 to 10 (10 is highest)
    private int priority = 1 + new Random().nextInt(10);

    // Feature 3: Track ready-queue entry time and accumulated waiting time
    private long readyQueueEntryTime;
    private long waitingTime = 0;

    // Constructor to initialize the process with name, burst time, and time quantum
    public Process(String name, int burstTime, int timeQuantum) {
        this.name = name;
        this.burstTime = burstTime;
        this.timeQuantum = timeQuantum;
        this.remainingTime = burstTime; // Initially, remaining time is equal to the burst time

        // Feature 3: Record the initial time the process enters the ready queue
        this.readyQueueEntryTime = System.currentTimeMillis();
    }

    // This method will be called when the thread for this process is started
    @Override
    public void run() {
        // Simulate running for either the time quantum or remaining time, whichever is
        // smaller
        int runTime = Math.min(timeQuantum, remainingTime);

        // Show quantum execution starting
        String quantumBar = createProgressBar(0, 15);
        System.out.println(Colors.BRIGHT_GREEN + "  ▶ " + Colors.BOLD + Colors.CYAN + name +
                Colors.RESET + Colors.GREEN + " executing quantum" + Colors.RESET +
                " [" + runTime + "ms] ");

        try {
            // Simulate quantum execution with progress updates
            int steps = 5;
            int stepTime = runTime / steps;

            for (int i = 1; i <= steps; i++) {
                Thread.sleep(stepTime);
                int quantumProgress = (i * 100) / steps;
                quantumBar = createProgressBar(quantumProgress, 15);

                // Clear line and show updated progress
                System.out.print("\r  " + Colors.YELLOW + "⚡" + Colors.RESET +
                        " Quantum progress: " + quantumBar);
            }

            System.out.println();

        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "\n  ✗ " + name + " was interrupted." + Colors.RESET);
        }

        remainingTime -= runTime;
        int overallProgress = (int) (((double) (burstTime - remainingTime) / burstTime) * 100);
        String overallProgressBar = createProgressBar(overallProgress, 20);

        System.out.println(Colors.YELLOW + "  ⏸ " + Colors.CYAN + name + Colors.RESET +
                " completed quantum " + Colors.BRIGHT_YELLOW + runTime + "ms" + Colors.RESET +
                " │ Overall progress: " + overallProgressBar);
        System.out.println(Colors.MAGENTA + "     Remaining time: " + remainingTime + "ms" + Colors.RESET);

        // If the process still has remaining time, it yields CPU for the next process
        if (remainingTime > 0) {
            System.out.println(Colors.BLUE + "  ↻ " + Colors.CYAN + name + Colors.RESET +
                    " yields CPU for context switch" + Colors.RESET);
        } else {
            // If no time is left, the process has finished its execution
            System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                    Colors.RESET + Colors.BRIGHT_GREEN + " finished execution!" +
                    Colors.RESET);
        }

        System.out.println();
    }

    // Helper method to create a visual progress bar
    private String createProgressBar(int progress, int width) {
        int filled = (progress * width) / 100;
        StringBuilder bar = new StringBuilder("[");

        for (int i = 0; i < width; i++) {
            if (i < filled) {
                bar.append(Colors.GREEN + "█" + Colors.RESET);
            } else {
                bar.append(Colors.WHITE + "░" + Colors.RESET);
            }
        }

        bar.append("] ").append(progress).append("%");
        return bar.toString();
    }

    // Method to run the last process to completion, ignoring the time quantum
    public void runToCompletion() {
        try {
            // Run for the remaining time without splitting into smaller time slices
            System.out.println(Colors.BRIGHT_CYAN + "  ⚡ " + Colors.BOLD + Colors.CYAN + name +
                    Colors.RESET + Colors.BRIGHT_CYAN +
                    " is the last process, running to completion" +
                    Colors.RESET + " [" + remainingTime + "ms]");

            Thread.sleep(remainingTime);
            remainingTime = 0;

            System.out.println(Colors.BRIGHT_GREEN + "  ✓ " + Colors.BOLD + Colors.CYAN + name +
                    Colors.RESET + Colors.BRIGHT_GREEN +
                    " finished execution!" + Colors.RESET);
            System.out.println();

        } catch (InterruptedException e) {
            System.out.println(Colors.RED + "  ✗ " + name + " was interrupted." + Colors.RESET);
        }
    }

    // Getter methods for process name, burst time, and remaining time
    public String getName() {
        return name;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    // Feature 1: Return the process priority for display in the ready queue
    public int getPriority() {
        return priority;
    }

    // Feature 3: Reset the timestamp whenever the process enters the ready queue
    public void markReady() {
        readyQueueEntryTime = System.currentTimeMillis();
    }

    // Feature 3: Add elapsed ready-queue time to the accumulated waiting time
    public void recordWaitingTime() {
        waitingTime += System.currentTimeMillis() - readyQueueEntryTime;
    }

    // Feature 3: Return accumulated waiting time in milliseconds
    public long getWaitingTime() {
        return waitingTime;
    }

    // Feature 3: Turnaround time follows the assignment definition: waiting + burst
    public long getTurnaroundTime() {
        return waitingTime + burstTime;
    }

    // Check if the process has finished (i.e., no remaining time)
    public boolean isFinished() {
        return remainingTime <= 0;
    }
}

public class SchedulerSimulation {

    // Feature 2: Count every time a process thread starts running
    private static int contextSwitchCount = 0;

    public static void main(String[] args) {
        // IMPORTANT: Put your student ID here to seed the random number generator
        int studentID = 444050133; // CHANGE THIS TO YOUR ACTUAL STUDENT ID

        Random random = new Random(studentID);

        // Define the time quantum in milliseconds
        // Choose a random number between 2000 and 5000 ms
        int timeQuantum = 2000 + random.nextInt(4) * 1000;

        // Generate random number of processes between 10 and 20
        int numProcesses = 10 + random.nextInt(11);

        // Queue to manage processes in a First-In-First-Out (FIFO) order
        Queue<Thread> processQueue = new LinkedList<>();

        // Map to associate each thread with its respective process object
        Map<Thread, Process> processMap = new HashMap<>();

        // Feature 3: Keep each process for the final statistics table
        Queue<Process> allProcesses = new LinkedList<>();

        // Print simulation header
        System.out.println("\n" + Colors.BOLD + Colors.BRIGHT_CYAN +
                "╔═══════════════════════════════════════════════════════════════════════════════════════╗" +
                Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                Colors.BG_BLUE + Colors.BRIGHT_WHITE + Colors.BOLD +
                "                          CPU SCHEDULER SIMULATION                                " +
                Colors.RESET + Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                "╠═══════════════════════════════════════════════════════════════════════════════════════╣" +
                Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                Colors.YELLOW + "  ⚙ Processes:     " + Colors.RESET + Colors.BRIGHT_YELLOW +
                String.format("%-65s", numProcesses) +
                Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                Colors.YELLOW + "  ⏱ Time Quantum:  " + Colors.RESET + Colors.BRIGHT_YELLOW +
                String.format("%-65s", timeQuantum + "ms") +
                Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET +
                Colors.YELLOW + "  🔑 Student ID:    " + Colors.RESET + Colors.BRIGHT_YELLOW +
                String.format("%-65s", studentID) +
                Colors.BOLD + Colors.BRIGHT_CYAN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_CYAN +
                "╚═══════════════════════════════════════════════════════════════════════════════════════╝" +
                Colors.RESET + "\n");

        // Create the processes
        for (int i = 1; i <= numProcesses; i++) {
            // Random burst time for each process
            int burstTime = timeQuantum / 2 + random.nextInt(2 * timeQuantum + 1);

            // Create a new process
            Process process = new Process("P" + i, burstTime, timeQuantum);

            // Feature 3: Store the process once for the final statistics table
            allProcesses.add(process);

            // Add the process to the ready queue and the map
            addProcessToQueue(process, processQueue, processMap);
        }

        // Start of the scheduler simulation
        System.out.println(Colors.BOLD + Colors.GREEN +
                "╔════════════════════════════════════════════════════════════════════════════════╗" +
                Colors.RESET);
        System.out.println(Colors.BOLD + Colors.GREEN + "║" + Colors.RESET +
                Colors.BG_GREEN + Colors.WHITE + Colors.BOLD +
                "                        ▶  SCHEDULER STARTING  ◀                               " +
                Colors.RESET + Colors.BOLD + Colors.GREEN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.GREEN +
                "╚════════════════════════════════════════════════════════════════════════════════╝" +
                Colors.RESET + "\n");

        // Loop to manage the scheduling of processes
        while (!processQueue.isEmpty()) {
            // Get the next thread from the queue (FIFO)
            Thread currentThread = processQueue.poll();

            // Print the current process queue
            System.out.println(Colors.BOLD + Colors.MAGENTA +
                    "┌─ Ready Queue " + "─".repeat(65) + Colors.RESET);
            System.out.print(Colors.MAGENTA + "│ " + Colors.RESET +
                    Colors.BRIGHT_WHITE + "[" + Colors.RESET);

            int queueCount = 0;

            for (Thread thread : processQueue) {
                Process process = processMap.get(thread);

                if (queueCount > 0) {
                    System.out.print(Colors.WHITE + " → " + Colors.RESET);
                }

                System.out.print(Colors.BRIGHT_CYAN + process.getName() + Colors.RESET);
                queueCount++;
            }

            if (queueCount == 0) {
                System.out.print(Colors.YELLOW + "empty" + Colors.RESET);
            }

            System.out.println(Colors.BRIGHT_WHITE + "]" + Colors.RESET);
            System.out.println(Colors.BOLD + Colors.MAGENTA +
                    "└" + "─".repeat(79) + Colors.RESET + "\n");

            // Feature 2: Count this process start as a context switch
            contextSwitchCount++;

            // Feature 3: Record how long this process waited before receiving CPU time
            Process currentProcess = processMap.get(currentThread);
            currentProcess.recordWaitingTime();

            // Start the thread, which will run the process for one time quantum
            currentThread.start();

            try {
                // Wait for the thread to finish its time quantum
                currentThread.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }

            // Retrieve the process associated with the thread
            Process process = processMap.get(currentThread);

            // Check if the process is not finished
            if (!process.isFinished()) {
                // If the process still has remaining time, check if there are more processes
                if (!processQueue.isEmpty()) {
                    // Re-enqueue the process for another round
                    addProcessToQueue(process, processQueue, processMap);
                } else {
                    // If this is the last process in the queue, run it to completion
                    System.out.println(Colors.BRIGHT_YELLOW + "  ⚠ " +
                            Colors.CYAN + process.getName() +
                            Colors.RESET + Colors.YELLOW +
                            " is the last process → running to completion" +
                            Colors.RESET);

                    process.runToCompletion();
                }
            }
        }

        // End of the scheduler simulation
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN +
                "╔════════════════════════════════════════════════════════════════════════════════╗" +
                Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN + "║" + Colors.RESET +
                Colors.BG_GREEN + Colors.WHITE + Colors.BOLD +
                "                     ✓  ALL PROCESSES COMPLETED  ✓                            " +
                Colors.RESET + Colors.BOLD + Colors.BRIGHT_GREEN + "║" + Colors.RESET);
        System.out.println(Colors.BOLD + Colors.BRIGHT_GREEN +
                "╚════════════════════════════════════════════════════════════════════════════════╝" +
                Colors.RESET + "\n");

        // Feature 2: Display the total number of context switches
        System.out.println(Colors.BOLD + Colors.BRIGHT_YELLOW +
                "Total Context Switches: " + contextSwitchCount +
                Colors.RESET);

        // Feature 3: Display waiting and turnaround time for every process
        System.out.println("\nFINAL PROCESS STATISTICS");
        System.out.printf("%-15s %-15s %-15s %-20s%n",
                "Process Name", "Burst Time", "Waiting Time", "Turnaround Time");

        for (Process completedProcess : allProcesses) {
            System.out.printf("%-15s %-15d %-15d %-20d%n",
                    completedProcess.getName(),
                    completedProcess.getBurstTime(),
                    completedProcess.getWaitingTime(),
                    completedProcess.getTurnaroundTime());
        }
    }

    // Method to add a process to the queue and map, while printing a ready message
    public static void addProcessToQueue(Process process,
            Queue<Thread> processQueue,
            Map<Thread, Process> processMap) {

        // Create a new thread to run the process
        Thread thread = new Thread(process);

        // Feature 3: Record the time this process enters the ready queue
        process.markReady();

        // Add the thread to the ready queue
        processQueue.add(thread);

        // Map the thread to the process
        processMap.put(thread, process);

        // Print a message indicating the process has entered the ready queue
        System.out.println(Colors.BLUE + "  ➕ " + Colors.BOLD + Colors.CYAN +
                process.getName() +
                Colors.RESET + Colors.BLUE + " added to ready queue" +
                Colors.RESET + " │ Burst time: " +
                Colors.YELLOW + process.getBurstTime() + "ms" +
                Colors.RESET + " │ Priority: " +
                Colors.BRIGHT_YELLOW + process.getPriority() +
                Colors.RESET);
    }
}