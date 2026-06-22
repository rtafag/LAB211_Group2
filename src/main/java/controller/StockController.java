package controller;

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
        getLowStockAlertByBranch(null, 5);
    }

    public void getLowStockAlertByBranch(String branchId, int threshold) {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        Map<String, String> medicineNameById = medicineRepo.findAll().stream()
                .collect(Collectors.toMap(Medicine::getMedicineId, Medicine::getMedicineName, (a, b) -> a));
        Map<String, Branch> branchById = branchRepo.findAll().stream()
                .collect(Collectors.toMap(Branch::getBranchId, Function.identity(), (a, b) -> a));

        System.out.println("===== LOW STOCK ALERT (< " + threshold + ") =====");
        boolean hasData = false;
        for (Stock stock : stocks) {
            if (branchId != null && !branchId.equals(stock.getBranchId())) {
                continue;
            }
            if (stock.getQuantity() >= threshold) {
                continue;
            }

            hasData = true;
            String medicineName = medicineNameById.getOrDefault(stock.getMedicineId(), stock.getMedicineId());
            Branch branch = branchById.get(stock.getBranchId());
            String branchName = branch != null ? branch.getBranchName() : stock.getBranchId();

            System.out.println("Branch: " + branchName
                    + " | Medicine: " + medicineName
                    + " | Quantity: " + stock.getQuantity());
        }

        if (!hasData) {
            if (branchId == null) {
                System.out.println("No low-stock records found.");
            } else {
                System.out.println("No low-stock records found for branch " + branchId);
            }
        }
    }
}
