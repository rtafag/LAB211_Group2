
import controller.DispenseController;
import controller.LoginController;
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
                        prescriptionController);
                view.showMenu();
            } else if ("2".equals(option)) {
                SimulatorController simulator = new SimulatorController();
                java.util.List<view.SimulatorView.SimulationResult> results = new java.util.ArrayList<>();
                simulator.runAllSimulations(results);
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
