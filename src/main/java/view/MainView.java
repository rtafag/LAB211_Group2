package view;

import controller.*;
import java.util.Scanner;
import model.User;

public class MainView {

    private final LoginController loginController;
    private final DispenseController dispenseController;
    private final StockController stockController;
    private final ReportController reportController;

    public MainView(LoginController l, DispenseController d, StockController s, ReportController r) {
        this.loginController = l;
        this.dispenseController = d;
        this.stockController = s;
        this.reportController = r;
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter ID: ");
        String id = sc.nextLine();
        System.out.print("Enter password: ");
        String pw = sc.nextLine();

        User user = loginController.login(id, pw);

        switch (user.getRole()) {
            case "PATIENT":
                patientMenu(user);
                break;
            case "PHARMACIST":
                pharmacistMenu(user, sc);
                break;
            case "MANAGER":
                managerMenu(user, sc);
                break;
            default:
                System.out.println("Invalid access rights.");
        }
    }

    private void patientMenu(User user) {
        System.out.println("PATIENT MENU: prescription viewing function not implemented yet.");
        System.out.println("Please contact the pharmacy for details.");
    }

    private void pharmacistMenu(User user, Scanner sc) {
        System.out.println("PHARMACIST MENU: dispense medicines, view stock");
        System.out.println("1. Dispense by prescription");
        System.out.println("2. View low-stock alerts");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter prescription ID: ");
                String prescriptionId = sc.nextLine();
                try {
                    dispenseController.processDispense(prescriptionId, user.getId());
                    System.out.println("Dispensed successfully for prescription " + prescriptionId);
                } catch (Exception e) {
                    System.out.println("Error dispensing: " + e.getMessage());
                }
                break;
            case "2":
                stockController.getLowStockAlert();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void managerMenu(User user, Scanner sc) {
        System.out.println("MANAGER MENU: stock management, reports");
        System.out.println("1. View stock reports");
        System.out.println("2. View low-stock alerts");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                reportController.printReport();
                break;
            case "2":
                stockController.getLowStockAlert();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
