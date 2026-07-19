package controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import model.Branch;
import model.Medicine;
import model.Stock;
import repository.BranchRepository;
import repository.MedicineRepository;
import repository.StockRepository;

public class StockController {

    private final StockRepository stockRepo;
    private final MedicineRepository medicineRepo;
    private final BranchRepository branchRepo;

    public StockController(StockRepository repo) {
        this.stockRepo = repo;
        this.medicineRepo = new MedicineRepository();
        this.branchRepo = new BranchRepository();
    }

    public void getStockByBranch(String branchId) {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        Map<String, String> medicineNameById = medicineRepo.findAll().stream()
                .collect(Collectors.toMap(Medicine::getMedicineId, Medicine::getMedicineName, (a, b) -> a));
        Map<String, Branch> branchById = branchRepo.findAll().stream()
                .collect(Collectors.toMap(Branch::getBranchId, Function.identity(), (a, b) -> a));

        Branch branch = branchById.get(branchId);
        String branchName = branch != null ? branch.getBranchName() : "Unknown Branch";

        System.out.println("===== STOCK BY BRANCH " + branchId + " =====");
        boolean hasData = false;
        for (Stock stock : stocks) {
            if (!branchId.equals(stock.getBranchId())) {
                continue;
            }
            hasData = true;
            String medicineName = medicineNameById.getOrDefault(stock.getMedicineId(), stock.getMedicineId());
            System.out.println("Branch: " + branchName
                    + " | Medicine: " + medicineName
                    + " | Quantity: " + stock.getQuantity());
        }

        if (!hasData) {
            System.out.println("No stock records found for branch " + branchId);
        }
    }

    public void getLowStockAlert() {
        System.out.println("Please use branch-specific low stock view.");
    }

    public void getLowStockAlertByBranch(String branchId, int threshold) {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        Map<String, String> medicineNameById = medicineRepo.findAll().stream()
                .collect(Collectors.toMap(Medicine::getMedicineId, Medicine::getMedicineName, (a, b) -> a));
        Map<String, Branch> branchById = branchRepo.findAll().stream()
                .collect(Collectors.toMap(Branch::getBranchId, Function.identity(), (a, b) -> a));

        if (branchId != null) {
            Branch branch = branchById.get(branchId);
            String branchName = branch != null ? branch.getBranchName() : branchId;

            List<Stock> lowestStocks = stocks.stream()
                    .filter(stock -> branchId.equals(stock.getBranchId()))
                    .sorted(Comparator.comparingInt(Stock::getQuantity))
                    .limit(20)
                    .collect(Collectors.toList());

            System.out.println("=====LOW STOCK ALERT" + branchId + " =====");
            if (lowestStocks.isEmpty()) {
                System.out.println("No stock records found for branch " + branchId);
                return;
            }

            for (Stock stock : lowestStocks) {
                String medicineName = medicineNameById.getOrDefault(stock.getMedicineId(), stock.getMedicineId());
                System.out.println("Branch: " + branchName
                        + " | Medicine: " + medicineName
                        + " | Quantity: " + stock.getQuantity());
            }
            return;
        }

        System.out.println("Branch ID is required for this view.");
    }

    public void addMedicineToStock(String medicineName, String branchId, int quantity) {
        if (medicineName == null || medicineName.isBlank()) {
            throw new IllegalArgumentException("Medicine name is required");
        }
        if (branchId == null || branchId.isBlank()) {
            throw new IllegalArgumentException("Branch ID is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        validateBranchExists(branchId);

        Medicine medicine = medicineRepo.findFirstByName(medicineName);
        if (medicine == null) {
            medicine = new Medicine(
                    medicineRepo.generateNextMedicineId(),
                    medicineName.trim(),
                    "box",
                    1,
                    100000.0);
            medicineRepo.save(medicine);
        }

        stockRepo.upsertStock(branchId.trim(), medicine.getMedicineId(), quantity);
    }

    public void editMedicineInStock(String stockId, String newMedicineName, String newBranchId, Integer newQuantity) {
        if (stockId == null || stockId.isBlank()) {
            throw new IllegalArgumentException("Stock ID is required");
        }

        Stock current = stockRepo.findByStockId(stockId.trim());
        if (current == null) {
            throw new IllegalArgumentException("Stock not found: " + stockId);
        }

        String branchIdToUse = (newBranchId == null || newBranchId.isBlank())
                ? current.getBranchId()
                : newBranchId.trim();
        validateBranchExists(branchIdToUse);

        String medicineIdToUse = current.getMedicineId();
        if (newMedicineName != null && !newMedicineName.isBlank()) {
            Medicine medicine = medicineRepo.findFirstByName(newMedicineName);
            if (medicine == null) {
                medicine = new Medicine(
                        medicineRepo.generateNextMedicineId(),
                        newMedicineName.trim(),
                        "box",
                        1,
                        100000.0);
                medicineRepo.save(medicine);
            }
            medicineIdToUse = medicine.getMedicineId();
        }

        int quantityToUse = newQuantity == null ? current.getQuantity() : newQuantity;
        stockRepo.updateStock(current.getStockId(), branchIdToUse, medicineIdToUse, quantityToUse);
    }

    public void deleteMedicineFromStock(String stockId) {
        if (stockId == null || stockId.isBlank()) {
            throw new IllegalArgumentException("Stock ID is required");
        }
        stockRepo.deleteByStockId(stockId.trim());
    }

    private void validateBranchExists(String branchId) {
        boolean exists = branchRepo.findAll().stream()
                .anyMatch(branch -> branchId.equals(branch.getBranchId()));
        if (!exists) {
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
    }
}
