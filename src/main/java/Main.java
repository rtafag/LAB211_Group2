import controller.*;
import repository.*;
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

        MainView view = new MainView(loginController, dispenseController, stockController, reportController);
        view.showMenu();
    }
}
