package view;

import java.util.Scanner;

import controller.DispenseController;
import controller.LoginController;
import controller.MedicineController;
import controller.PrescriptionController;
import controller.ReportController;
import controller.StockController;
import model.Medicine;
import model.Prescription;
import model.User;

public class MainView {

    private final LoginController loginController;
    private final DispenseController dispenseController;
    private final StockController stockController;
    private final ReportController reportController;
    private final PrescriptionController prescriptionController;
    private final MedicineController medicineController;

    public MainView(LoginController l, DispenseController d, StockController s, ReportController r,
            PrescriptionController p, MedicineController m) {
        this.loginController = l;
        this.dispenseController = d;
        this.stockController = s;
        this.reportController = r;
        this.prescriptionController = p;
        this.medicineController = m;
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
            case "STAFF":
                pharmacistMenu(user, sc);
                break;
            case "ADMIN":
                new view.AdminView(new controller.AdminController(), user).showMenu(sc);
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
        System.out.print("Enter quantity: ");
        String quantityInput = sc.nextLine();
        System.out.print("Enter create date (yyyy-MM-dd), press Enter for today: ");
        String createdDate = sc.nextLine();

        try {
            int quantity = Integer.parseInt(quantityInput);
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            model.Prescription created = prescriptionController.createPrescription(
                    patientName,
                    patientDob,
                    createdDate,
                    branchId,
                    medicineId,
                    quantity);
            System.out.println("Created prescription successfully: " + created.getPrescriptionId());
        } catch (NumberFormatException e) {
            System.out.println("Error creating prescription: Quantity must be a valid integer");
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
            System.out.println("1. View stock reports");
            System.out.println("2. View low-stock alerts");
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
            System.out.println("1. View medicine information");
            System.out.println("2. Add medicine");
            System.out.println("3. Edit medicine");
            System.out.println("4. Delete medicine");
            System.out.println("5. Add medicine to batchlot");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    viewMedicineInfoFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "2":
                    addMedicineFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "3":
                    editMedicineFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "4":
                    deleteMedicineFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "5":
                    addMedicineToStockFlow(sc);
                    pauseForEnter(sc);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void viewMedicineInfoFlow(Scanner sc) {
        System.out.print("Enter medicine ID (e.g., M0001): ");
        String medicineId = sc.nextLine();

        try {
            Medicine medicine = medicineController.findById(medicineId);

            if (medicine == null) {
                System.out.println("Medicine not found: " + medicineId);
                return;
            }

            System.out.println("--- Medicine Information ---");
            System.out.println("ID: " + medicine.getMedicineId());
            System.out.println("Name: " + medicine.getMedicineName());
            System.out.println("Unit: " + medicine.getUnit());
            System.out.println("Units per box: " + medicine.getUnitsPerBox());
            System.out.println("Price: " + medicine.getPrice());
        } catch (Exception e) {
            System.out.println("Error viewing medicine: " + e.getMessage());
        }
    }

    private void addMedicineFlow(Scanner sc) {
        System.out.print("Enter medicine name: ");
        String medicineName = sc.nextLine();
        System.out.print("Enter unit (e.g., box): ");
        String unit = sc.nextLine();
        System.out.print("Enter units per box: ");
        String unitsPerBoxInput = sc.nextLine();
        System.out.print("Enter price: ");
        String priceInput = sc.nextLine();

        try {
            Medicine medicine = medicineController.addMedicine(medicineName, unit, unitsPerBoxInput, priceInput);
            System.out.println("Medicine added successfully: " + medicine.getMedicineId());
        } catch (Exception e) {
            System.out.println("Error adding medicine: " + e.getMessage());
        }
    }

    private void editMedicineFlow(Scanner sc) {
        System.out.print("Enter medicine ID to edit (e.g., M0001): ");
        String medicineId = sc.nextLine();

        try {
            Medicine medicine = medicineController.findById(medicineId);

            if (medicine == null) {
                System.out.println("Medicine not found: " + medicineId);
                return;
            }

            System.out.print("New medicine name (Enter to keep current [" + medicine.getMedicineName() + "]): ");
            String newName = sc.nextLine();
            System.out.print("New unit (Enter to keep current [" + medicine.getUnit() + "]): ");
            String newUnit = sc.nextLine();
            System.out.print("New units per box (Enter to keep current [" + medicine.getUnitsPerBox() + "]): ");
            String newUnitsPerBoxInput = sc.nextLine();
            System.out.print("New price (Enter to keep current [" + medicine.getPrice() + "]): ");
            String newPriceInput = sc.nextLine();

            Medicine updatedMedicine = medicineController.updateMedicine(
                    medicineId,
                    newName,
                    newUnit,
                    newUnitsPerBoxInput,
                    newPriceInput);
            System.out.println("Medicine updated successfully: " + updatedMedicine.getMedicineId());
        } catch (Exception e) {
            System.out.println("Error editing medicine: " + e.getMessage());
        }
    }

    private void deleteMedicineFlow(Scanner sc) {
        System.out.print("Enter medicine ID to delete (e.g., M0001): ");
        String medicineId = sc.nextLine();

        try {
            Medicine medicine = medicineController.findById(medicineId);

            if (medicine == null) {
                System.out.println("Medicine not found: " + medicineId);
                return;
            }

            System.out.println("Medicine to delete: " + medicine.getMedicineName() + " (" + medicine.getMedicineId() + ")");
            System.out.print("Type YES to confirm deletion: ");
            String confirm = sc.nextLine();
            if (!"YES".equalsIgnoreCase(confirm.trim())) {
                System.out.println("Delete cancelled.");
                return;
            }

            medicineController.deleteMedicine(medicineId);
            System.out.println("Medicine deleted successfully: " + medicineId);
        } catch (Exception e) {
            System.out.println("Error deleting medicine: " + e.getMessage());
        }
    }

    private void addMedicineToStockFlow(Scanner sc) {
        System.out.print("Enter medicine ID (e.g., M0001): ");
        String medicineId = sc.nextLine().trim();
        System.out.print("Enter branch ID (e.g., B001): ");
        String branchId = sc.nextLine().trim();
        System.out.print("Enter quantity to add (boxes): ");
        String quantityInput = sc.nextLine().trim();
        System.out.print("Enter expiry date (yyyy-MM-dd): ");
        String expiryDate = sc.nextLine().trim();

        try {
            int quantity = Integer.parseInt(quantityInput);
            stockController.addMedicineToStock(branchId, medicineId, quantity, expiryDate);
            System.out.println("Medicine successfully added to stock and batch lots.");
        } catch (NumberFormatException e) {
            System.out.println("Error: Quantity must be a valid integer.");
        } catch (Exception e) {
            System.out.println("Error adding medicine to stock: " + e.getMessage());
        }
    }
}
