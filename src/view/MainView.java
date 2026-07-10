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
            System.out.println("1. View medicine information");
            System.out.println("2. Add medicine");
            System.out.println("3. Edit medicine");
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
            repository.MedicineRepository medicineRepo = new repository.MedicineRepository();
            java.util.List<model.Medicine> medicines = medicineRepo.findAll();
            model.Medicine medicine = medicines.stream()
                    .filter(m -> m.getMedicineId().equals(medicineId))
                    .findFirst()
                    .orElse(null);

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
            repository.MedicineRepository medicineRepo = new repository.MedicineRepository();
            String medicineId = medicineRepo.generateNextMedicineId();
            int unitsPerBox = Integer.parseInt(unitsPerBoxInput);
            double price = Double.parseDouble(priceInput);

            model.Medicine medicine = new model.Medicine(medicineId, medicineName, unit, unitsPerBox, price);
            medicineRepo.save(medicine);
            System.out.println("Medicine added successfully: " + medicineId);
        } catch (Exception e) {
            System.out.println("Error adding medicine: " + e.getMessage());
        }
    }

    private void editMedicineFlow(Scanner sc) {
        System.out.print("Enter medicine ID to edit (e.g., M0001): ");
        String medicineId = sc.nextLine();

        try {
            repository.MedicineRepository medicineRepo = new repository.MedicineRepository();
            java.util.List<model.Medicine> medicines = medicineRepo.findAll();
            model.Medicine medicine = medicines.stream()
                    .filter(m -> m.getMedicineId().equals(medicineId))
                    .findFirst()
                    .orElse(null);

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

            String updatedName = newName.isBlank() ? medicine.getMedicineName() : newName;
            String updatedUnit = newUnit.isBlank() ? medicine.getUnit() : newUnit;
            int updatedUnitsPerBox = newUnitsPerBoxInput.isBlank() ? medicine.getUnitsPerBox() : Integer.parseInt(newUnitsPerBoxInput);
            double updatedPrice = newPriceInput.isBlank() ? medicine.getPrice() : Double.parseDouble(newPriceInput);

            model.Medicine updatedMedicine = new model.Medicine(medicineId, updatedName, updatedUnit, updatedUnitsPerBox, updatedPrice);
            medicines.replaceAll(m -> m.getMedicineId().equals(medicineId) ? updatedMedicine : m);
            medicineRepo.writeAll("data/medicines.csv", medicines);
            System.out.println("Medicine updated successfully: " + medicineId);
        } catch (Exception e) {
            System.out.println("Error editing medicine: " + e.getMessage());
        }
    }
}
