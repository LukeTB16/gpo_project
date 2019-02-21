
package criticalpath;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Luke
 */

public class CriticalPath {
    public static int maxCost;
    public static String format = "%1$-10s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-10s\n";

    public static void main(String[] args) {
        HashSet<Task> allTasks = new HashSet<Task>();
        Task end = new Task("End", 0);
        
 
        Task F = new Task("F",8, end);
        Task A = new Task("A", 2, end);
        Task X = new Task("X", 6, F, end);
        Task Q = new Task("Q", 4, A, X);
        Task start = new Task("Start", 0, Q);
        
        allTasks.add(end);
        allTasks.add(F);
        allTasks.add(A);
        allTasks.add(X);
        allTasks.add(Q);
        allTasks.add(start);
        Task[] result = criticalPath(allTasks);
        print(result);
    }

    public static class Task {
        // Costo task(durata)
        public int cost;
        // Costo percorso critico
        public int criticalCost;
        // Nome task
        public String name;
        // Early Start
        public int earlyStart;
        // Early Finish
        public int earlyFinish;
        // Latest Start
        public int latestStart;
        // Latest Finish
        public int latestFinish;
        // Task che hanno dipendenze da altre task
        public HashSet<Task> dependencies = new HashSet<Task>();

        public Task(String name, int cost, Task... dependencies) {
            this.name = name;
            this.cost = cost;
            for (Task t : dependencies) {
                this.dependencies.add(t);
            }
            this.earlyFinish = -1;
        }

        public void setLatest() {
            latestStart = maxCost - criticalCost;
            latestFinish = latestStart + cost;
        }

        public String[] toStringArray() {
            String criticalCond = earlyStart == latestStart ? "Si" : "No";
            String[] toString = { name, earlyStart + "", earlyFinish + "", latestStart + "", latestFinish + "",
                    latestStart - earlyStart + "", criticalCond };
            return toString;
        }

        public boolean isDependent(Task t) {
            // Se è una dipendenza diretta
            if (dependencies.contains(t)) {
                return true;
            }
            // Se è una dipendenza indiretta
            for (Task dep : dependencies) {
                if (dep.isDependent(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static Task[] criticalPath(Set<Task> tasks) {
        
        HashSet<Task> completed = new HashSet<Task>();
        
        HashSet<Task> remaining = new HashSet<Task>(tasks);

     
        while (!remaining.isEmpty()) {
            boolean progress = false;

            // Trova nuove task da 
            for (Iterator<Task> it = remaining.iterator(); it.hasNext();) {
                Task task = it.next();
                if (completed.containsAll(task.dependencies)) {
                    
                    int critical = 0;
                    for (Task t : task.dependencies) {
                        if (t.criticalCost > critical) {
                            critical = t.criticalCost;
                        }
                    }
                    task.criticalCost = critical + task.cost;
                    completed.add(task);
                    it.remove();
                    progress = true;
                }
            }
            if (!progress)
                throw new RuntimeException("Dipendenza ciclica, algoritmo fermato.");
        }

        // Ottengo il costo
        maxCost(tasks);
        HashSet<Task> initialNodes = initials(tasks);
        calculateEarly(initialNodes);

        // Ottengo le task
        Task[] ret = completed.toArray(new Task[0]);
        // Crea una lista prioritaria
        Arrays.sort(ret, new Comparator<Task>() {

            @Override
            public int compare(Task o1, Task o2) {
                return o1.name.compareTo(o2.name);
            }
        });

        return ret;
    }

    public static void calculateEarly(HashSet<Task> initials) {
        for (Task initial : initials) {
            initial.earlyStart = 0;
            initial.earlyFinish = initial.cost;
            setEarly(initial);
        }
    }

    public static void setEarly(Task initial) {
        int completionTime = initial.earlyFinish;
        for (Task t : initial.dependencies) {
            if (completionTime >= t.earlyStart) {
                t.earlyStart = completionTime;
                t.earlyFinish = completionTime + t.cost;
            }
            setEarly(t);
        }
    }

    public static HashSet<Task> initials(Set<Task> tasks) {
        HashSet<Task> remaining = new HashSet<Task>(tasks);
        for (Task t : tasks) {
            for (Task td : t.dependencies) {
                remaining.remove(td);
            }
        }

        for (Task t : remaining)
            System.out.print(t.name + " ");
        System.out.print("\n\n");
        return remaining;
    }

    public static void maxCost(Set<Task> tasks) {
        int max = -1;
        for (Task t : tasks) {
            if (t.criticalCost > max)
                max = t.criticalCost;
        }
        maxCost = max;
        System.out.println("Costo percorso critico: " + maxCost);
        for (Task t : tasks) {
            t.setLatest();
        }
    }

    public static void print(Task[] tasks) {
        System.out.format(format, "Task", "ES", "EF", "LS", "LF", "MDG", "Percorso critico?"); // MDG="margine di scorrimento"
        for (Task t : tasks)
            System.out.format(format, (Object[]) t.toStringArray());
    }
}