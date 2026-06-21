package view;

import java.util.Scanner;

import controller.DispenseController;
import controller.LoginController;
import controller.PrescriptionController;
import controller.ReportController;
import controller.StockController;
import model.User;

public class MainView {

    private final LoginController loginController;
    private final DispenseController dispenseController;
    private final StockController stockController;
    private final ReportController reportController;
    private final PrescriptionController prescriptionController;

    public MainView(LoginController l, DispenseController d, StockController s, ReportController r,
            PrescriptionController p) {
        this.loginController = l;
        this.dispenseController = d;
        this.stockController = s;
        this.reportController = r;
        this.prescriptionController = p;
    }

    public void showMenu() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter phone number: ");
        String phoneNumber = sc.nextLine();
        System.out.print("Enter password: ");
        String pw = sc.nextLine();

        User user = loginController.login(phoneNumber, pw);

        switch (user.getRole()) {
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

    private void pharmacistMenu(User user, Scanner sc) {
        System.out.println("PHARMACIST MENU: dispense medicines, view stock");
        System.out.println("1. Create prescription");
        System.out.println("2. Dispense by prescription");
        System.out.println("3. View low-stock alerts");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                createPrescription(sc);
                break;
            case "2":
                System.out.print("Enter prescription ID: ");
                String prescriptionId = sc.nextLine();
                try {
                    dispenseController.processDispense(prescriptionId, user.getId());
                    System.out.println("Dispensed successfully for prescription " + prescriptionId);
                } catch (Exception e) {
                    System.out.println("Error dispensing: " + e.getMessage());
                }
                break;
            case "3":
                stockController.getLowStockAlert();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void createPrescription(Scanner sc) {
        System.out.print("Enter patient name: ");
        String patientName = sc.nextLine();
        System.out.print("Enter patient DOB (yyyy-MM-dd): ");
        String patientDob = sc.nextLine();
        System.out.print("Enter branch ID (e.g., B001): ");
        String branchId = sc.nextLine();
        System.out.print("Enter create date (yyyy-MM-dd), press Enter for today: ");
        String createdDate = sc.nextLine();

        try {
            model.Prescription created = prescriptionController.createPrescription(
                    patientName,
                    patientDob,
                    createdDate,
                    branchId);
            System.out.println("Created prescription successfully: " + created.getPrescriptionId());
        } catch (Exception e) {
            System.out.println("Error creating prescription: " + e.getMessage());
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
