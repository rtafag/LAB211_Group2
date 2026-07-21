
import controller.DispenseController;
import controller.LoginController;
import controller.MedicineController;
import controller.PrescriptionController;
import controller.ReportController;
import controller.SimulatorController;
import controller.StockController;
import repository.BatchLotRepository;
import repository.DispenseRecordRepository;
import repository.PrescriptionItemRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;
import repository.UserRepository;
import view.MainView;

public class Main {

    public static void main(String[] args) {
        UserRepository userRepo = new UserRepository();
        PrescriptionRepository presRepo = new PrescriptionRepository();
        StockRepository stockRepo = new StockRepository();
        BatchLotRepository lotRepo = new BatchLotRepository();
        PrescriptionItemRepository itemRepo = new PrescriptionItemRepository();
        DispenseRecordRepository recordRepo = new DispenseRecordRepository();

        LoginController loginController = new LoginController(userRepo);
        DispenseController dispenseController = new DispenseController(presRepo, stockRepo, lotRepo, itemRepo,
                recordRepo);
        StockController stockController = new StockController(stockRepo);
        ReportController reportController = new ReportController(stockRepo, presRepo, lotRepo);
        PrescriptionController prescriptionController = new PrescriptionController(presRepo, itemRepo);
        MedicineController medicineController = new MedicineController();

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            System.out.println("\n=================================================");
            System.out.println("      FPT PHARMACY CHAIN SYSTEM - WEEK 8");
            System.out.println("=================================================");
            System.out.println("1. Run Main Pharmacy Application (Login)");
            System.out.println("2. Run Concurrency Simulator Tool");
            System.out.println("0. Exit");
            System.out.print("Please select an option (0-2): ");
            String option = scanner.nextLine().trim();
            if ("1".equals(option)) {
                MainView view = new MainView(
                        loginController,
                        dispenseController,
                        stockController,
                        reportController,
                        prescriptionController,
                        medicineController);
                view.showMenu();
            } else if ("2".equals(option)) {
                System.out.println("\n--- CONCURRENCY SIMULATION CONFIGURATION ---");
                System.out.print("Enter number of threads (Press ENTER for default 200): ");
                String threadsInput = scanner.nextLine().trim();
                int threads = 200;
                if (!threadsInput.isEmpty()) {
                    try {
                        threads = Integer.parseInt(threadsInput);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number. Using default 200 threads.");
                    }
                }

                System.out.println("Select concurrency mode:");
                System.out.println("1. ALL Modes (NO_LOCK, SYNC, OPTIMISTIC, FILE_LOCK - Default)");
                System.out.println("2. NO_LOCK");
                System.out.println("3. SYNC");
                System.out.println("4. OPTIMISTIC");
                System.out.println("5. FILE_LOCK");
                System.out.print("Please select mode (1-5, Press ENTER for 1): ");
                String modeChoice = scanner.nextLine().trim();
                if (modeChoice.isEmpty()) {
                    modeChoice = "1";
                }

                SimulatorController simulator = new SimulatorController();
                java.util.List<view.SimulatorView.SimulationResult> results = new java.util.ArrayList<>();
                simulator.runCustomSimulation(modeChoice, threads, results);
                view.SimulatorView.printResultsTable(results);
            } else if ("0".equals(option)) {
                System.out.println("Exiting application. Goodbye!");
                break;
            } else {
                System.out.println("Invalid option, please try again.");
            }
        }
    }
}
