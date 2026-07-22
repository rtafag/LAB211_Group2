package controller;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import model.BatchLot;
import model.Branch;
import model.Medicine;
import model.Stock;
import repository.BatchLotRepository;
import repository.BranchRepository;
import repository.MedicineRepository;
import repository.StockRepository;

public class StockController {

    private final StockRepository stockRepo;
    private final MedicineRepository medicineRepo;
    private final BranchRepository branchRepo;
    private final BatchLotRepository lotRepo;

    public StockController(StockRepository repo) {
        this.stockRepo = repo;
        this.medicineRepo = new MedicineRepository();
        this.branchRepo = new BranchRepository();
        this.lotRepo = new BatchLotRepository();
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

    public void addMedicineToStock(String branchId, String medicineId, int quantity, String expiryDateStr) {
        if (branchRepo.findById(branchId) == null) {
            throw new IllegalArgumentException("Branch ID '" + branchId + "' does not exist.");
        }
        boolean medicineExists = medicineRepo.findAll().stream()
                .anyMatch(m -> m.getMedicineId().equals(medicineId));
        if (!medicineExists) {
            throw new IllegalArgumentException("Medicine ID '" + medicineId + "' does not exist.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive integer.");
        }
        java.time.LocalDate expiryDate;
        try {
            expiryDate = java.time.LocalDate.parse(expiryDateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.");
        }
        if (expiryDate.isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date cannot be in the past.");
        }

        stockRepo.upsertStock(branchId, medicineId, quantity);

        List<BatchLot> lots = lotRepo.readAll("data/batch_lots.csv");
        BatchLot existingLot = lots.stream()
                .filter(l -> l.getMedicineId().equals(medicineId) 
                           && l.getBranchId().equals(branchId) 
                           && l.getExpiryDate().equals(expiryDate))
                .findFirst().orElse(null);

        if (existingLot != null) {
            BatchLot updatedLot = new BatchLot(
                    existingLot.getBatchLotId(),
                    existingLot.getMedicineId(),
                    existingLot.getBranchId(),
                    existingLot.getQuantity() + quantity,
                    existingLot.getManufactureDate(),
                    existingLot.getExpiryDate(),
                    existingLot.getVersion() + 1
            );
            lots.replaceAll(l -> l.getBatchLotId().equals(existingLot.getBatchLotId()) ? updatedLot : l);
        } else {
            String nextLotId = generateNextBatchLotId(lots);
            BatchLot newLot = new BatchLot(
                    nextLotId,
                    medicineId,
                    branchId,
                    quantity,
                    java.time.LocalDate.now(),
                    expiryDate,
                    1
            );
            lots.add(newLot);
        }
        lotRepo.writeAll("data/batch_lots.csv", lots);
    }

    private String generateNextBatchLotId(List<BatchLot> lots) {
        int maxId = lots.stream()
                .map(BatchLot::getBatchLotId)
                .filter(id -> id != null && id.startsWith("BL"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(2));
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("BL%05d", maxId + 1);
    }
}
