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
        if (user == null) {
            System.out.println("Login failed.");
            return;
        }

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
        while (true) {
            System.out.println("PHARMACIST MENU:");
            System.out.println("1. Create prescription");
            System.out.println("2. View prescription");
            System.out.println("3. View stock, batch lot, report");
            System.out.println("4. Medicine management");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    createPrescription(sc);
                    pauseForEnter(sc);
                    break;
                case "2":
                    System.out.print("Enter prescription ID: ");
                    String prescriptionId = sc.nextLine();
                    handlePrescriptionAction(sc, user, prescriptionId);
                    break;
                case "3":
                    managerMenu(user, sc);
                    break;
                case "4":
                    medicinesManageMenu(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
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

        while (true) {
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
                    pauseForEnter(sc);
                    break;
                case "2":
                    editPrescriptionInformation(sc, prescription);
                    prescription = prescriptionController.findById(prescriptionId);
                    if (prescription == null) {
                        System.out.println("Prescription not found: " + prescriptionId);
                        return;
                    }
                    pauseForEnter(sc);
                    break;
                case "3":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
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

    private void pauseForEnter(Scanner sc) {
        System.out.print("\nPress Enter to return to menu...");
        sc.nextLine();
    }

    private void managerMenu(User user, Scanner sc) {
        while (true) {
            System.out.println("MANAGER MENU: stock management, reports");
            System.out.println("1. View stock reports by branch");
            System.out.println("2. View low-stock alerts by branch (< 60)");
            System.out.println("3. View near expiry warning");
            System.out.println("4. Sale report");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter branch ID (e.g., B001): ");
                    String branchIdForReport = sc.nextLine();
                    stockController.getStockByBranch(branchIdForReport);
                    pauseForEnter(sc);
                    break;
                case "2":
                    System.out.print("Enter branch ID (e.g., B001): ");
                    String branchIdForLowStock = sc.nextLine();
                    stockController.getLowStockAlertByBranch(branchIdForLowStock, 60);
                    pauseForEnter(sc);
                    break;
                case "3":
                    System.out.print("Enter branch ID (e.g., B001): ");
                    String branchIdForNearExpiry = sc.nextLine();
                    reportController.printNearExpiryWarning(branchIdForNearExpiry);
                    pauseForEnter(sc);
                    break;
                case "4":
                    System.out.print("Enter branch ID (e.g., B001): ");
                    String branchIdForSaleReport = sc.nextLine();
                    reportController.printSaleReport(branchIdForSaleReport);
                    pauseForEnter(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void medicinesManageMenu(Scanner sc) {
        while (true) {
            System.out.println("MEDICINE MANAGEMENT:");
            System.out.println("1. Add medicine to stock");
            System.out.println("2. Edit medicine in stock");
            System.out.println("3. Delete medicine from stock");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    addMedicineToStockFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "2":
                    editMedicineInStockFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "3":
                    deleteMedicineFromStockFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void addMedicineToStockFlow(Scanner sc) {
        System.out.print("Enter medicine name: ");
        String medicineName = sc.nextLine();
        System.out.print("Enter description: ");
        String description = sc.nextLine();
        System.out.print("Enter manufacturer: ");
        String manufacturer = sc.nextLine();
        System.out.print("Enter branch ID (e.g., B001): ");
        String branchId = sc.nextLine();
        System.out.print("Enter quantity: ");
        String quantityInput = sc.nextLine();

        try {
            int quantity = Integer.parseInt(quantityInput);
            stockController.addMedicineToStock(medicineName, description, manufacturer, branchId, quantity);
            System.out.println("Medicine saved to stock successfully.");
        } catch (Exception e) {
            System.out.println("Error adding medicine to stock: " + e.getMessage());
        }
    }

    private void editMedicineInStockFlow(Scanner sc) {
        System.out.print("Enter stock ID to edit (e.g., S00001): ");
        String stockId = sc.nextLine();
        System.out.print("New medicine name (Enter to keep current): ");
        String newMedicineName = sc.nextLine();
        System.out.print("New description (Enter to keep current): ");
        String newDescription = sc.nextLine();
        System.out.print("New manufacturer (Enter to keep current): ");
        String newManufacturer = sc.nextLine();
        System.out.print("New branch ID (Enter to keep current): ");
        String newBranchId = sc.nextLine();
        System.out.print("New quantity (Enter to keep current): ");
        String newQuantityInput = sc.nextLine();

        try {
            Integer newQuantity = null;
            if (newQuantityInput != null && !newQuantityInput.isBlank()) {
                newQuantity = Integer.parseInt(newQuantityInput);
            }
            stockController.editMedicineInStock(
                    stockId,
                    newMedicineName,
                    newDescription,
                    newManufacturer,
                    newBranchId,
                    newQuantity);
            System.out.println("Medicine stock updated successfully.");
        } catch (Exception e) {
            System.out.println("Error editing medicine stock: " + e.getMessage());
        }
    }

    private void deleteMedicineFromStockFlow(Scanner sc) {
        System.out.print("Enter stock ID to delete (e.g., S00001): ");
        String stockId = sc.nextLine();

        try {
            stockController.deleteMedicineFromStock(stockId);
            System.out.println("Medicine stock deleted successfully.");
        } catch (Exception e) {
            System.out.println("Error deleting medicine stock: " + e.getMessage());
        }
    }
}
