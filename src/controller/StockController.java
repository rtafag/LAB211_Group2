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
}
