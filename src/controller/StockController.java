package controller;

import java.util.List;
import model.Stock;
import repository.StockRepository;

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
                .forEach(s -> System.out.println("Warning: " + s.getMedicineId() + " is running low"));
    }
}
