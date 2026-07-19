package view;

import java.util.List;

public class SimulatorView {
    
    public static class SimulationResult {
        public final String mode;
        public final int threads;
        public final long timeMs;
        public final int totalReq;
        public final int successReq;
        public final int invIncon;
        public final int doubleDisp;
        public final int expiredDisp;

        public SimulationResult(String mode, int threads, long timeMs, int totalReq, int successReq,
                                int invIncon, int doubleDisp, int expiredDisp) {
            this.mode = mode;
            this.threads = threads;
            this.timeMs = timeMs;
            this.totalReq = totalReq;
            this.successReq = successReq;
            this.invIncon = invIncon;
            this.doubleDisp = doubleDisp;
            this.expiredDisp = expiredDisp;
        }
    }

    public static void printResultsTable(List<SimulationResult> results) {
        System.out.println("\n====================================================== SIMULATION RESULTS ======================================================");
        System.out.println("+------------+---------+-----------+------------+--------------+----------------------+----------------------+----------------------+");
        System.out.println("|    Mode    | Threads | Time (ms) | Total Req. | Success Req. | Inv. Inconsistency   | Double Dispensing    | Expired Dispensing   |");
        System.out.println("+------------+---------+-----------+------------+--------------+----------------------+----------------------+----------------------+");
        for (SimulationResult r : results) {
            System.out.printf("| %-10s | %-7d | %-9d | %-10d | %-12d | %-20d | %-20d | %-20d |\n",
                    r.mode, r.threads, r.timeMs, r.totalReq, r.successReq, r.invIncon, r.doubleDisp, r.expiredDisp);
        }
        System.out.println("+------------+---------+-----------+------------+--------------+----------------------+----------------------+----------------------+");
        System.out.println("================================================================================================================================\n");
    }
}
