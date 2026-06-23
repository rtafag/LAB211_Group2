package view;

import java.util.Scanner;

import controller.DispenseController;
import controller.LoginController;
import controller.PrescriptionController;
import controller.ReportController;
import controller.StockController;
import model.Prescription;
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
                handlePrescriptionAction(sc, user, prescriptionId);
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
        System.out.print("Enter medicine ID (e.g., M0001): ");
        String medicineId = sc.nextLine();
        System.out.print("Enter create date (yyyy-MM-dd), press Enter for today: ");
        String createdDate = sc.nextLine();

        try {
            model.Prescription created = prescriptionController.createPrescription(
                    patientName,
                    patientDob,
                    createdDate,
                    branchId,
                    medicineId);
            System.out.println("Created prescription successfully: " + created.getPrescriptionId());
        } catch (Exception e) {
            System.out.println("Error creating prescription: " + e.getMessage());
        }
    }

    private void handlePrescriptionAction(Scanner sc, User user, String prescriptionId) {
        Prescription prescription = prescriptionController.findById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found: " + prescriptionId);
            return;
        }

        System.out.println("--- Prescription Information ---");
        System.out.println("ID: " + prescription.getPrescriptionId());
        System.out.println("Patient Name: " + prescription.getPatientName());
        System.out.println("Patient DOB: " + prescription.getPatientDob());
        System.out.println("Create Date: " + prescription.getCreatedDate());
        System.out.println("Expiry Date: " + prescription.getExpiredDate());
        System.out.println("Status: " + prescription.getStatus());
        System.out.println("Branch ID: " + prescription.getBranchId());

        System.out.println("1. Confirm order");
        System.out.println("2. Edit information");
        System.out.println("3. Back");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                try {
                    String resultMessage = dispenseController.processDispense(prescriptionId, user.getId());
                    System.out.println(resultMessage);
                } catch (Exception e) {
                    System.out.println("Error dispensing: " + e.getMessage());
                }
                break;
            case "2":
                editPrescriptionInformation(sc, prescription);
                break;
            case "3":
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void editPrescriptionInformation(Scanner sc, Prescription current) {
        System.out.print("New patient name (Enter to keep current): ");
        String patientName = sc.nextLine();
        System.out.print("New patient DOB yyyy-MM-dd (Enter to keep current): ");
        String patientDob = sc.nextLine();
        System.out.print("New create date yyyy-MM-dd (Enter to keep current): ");
        String createdDate = sc.nextLine();
        System.out.print("New branch ID (Enter to keep current): ");
        String branchId = sc.nextLine();
        System.out.print("New medicine ID (Enter to keep current): ");
        String medicineId = sc.nextLine();

        try {
            Prescription updated = prescriptionController.updatePrescriptionInfo(
                    current.getPrescriptionId(),
                    patientName,
                    patientDob,
                    createdDate,
                    branchId,
                    medicineId);
            System.out.println("Updated prescription successfully: " + updated.getPrescriptionId());
        } catch (Exception e) {
            System.out.println("Error updating prescription: " + e.getMessage());
        }
    }

    private void managerMenu(User user, Scanner sc) {
        System.out.println("MANAGER MENU: stock management, reports");
        System.out.println("1. View stock reports by branch");
        System.out.println("2. View low-stock alerts by branch (< 60)");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        switch (choice) {
            case "1":
                System.out.print("Enter branch ID (e.g., B001): ");
                String branchIdForReport = sc.nextLine();
                stockController.getStockByBranch(branchIdForReport);
                break;
            case "2":
                System.out.print("Enter branch ID (e.g., B001): ");
                String branchIdForLowStock = sc.nextLine();
                stockController.getLowStockAlertByBranch(branchIdForLowStock, 60);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
