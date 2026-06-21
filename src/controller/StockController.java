package controller;

import repository.StockRepository;
import model.Stock;
import java.util.List;

public class StockController {
    private final StockRepository stockRepo;

    public StockController(StockRepository repo) {
        this.stockRepo = repo;
    }

    public void getStockByBranch(String branchId) {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        stocks.stream()
                .filter(s -> s.getBranchId().equals(branchId))
                .forEach(s -> System.out.println(s.toCsvLine()));
    }

    public void getLowStockAlert() {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        stocks.stream()
                .filter(s -> s.getQuantity() < 5)
                .forEach(s -> System.out.println("Cảnh báo: " + s.getMedicineId() + " sắp hết hàng"));
    }
}
