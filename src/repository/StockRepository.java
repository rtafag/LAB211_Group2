package repository;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import model.Stock;

public class StockRepository extends CsvRepository<Stock> {

    private static final Object STOCK_LOCK = new Object();

    private final String fileName;

    public StockRepository() {
        this("data/stocks.csv");
    }

    public StockRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Stock parseLine(String line) {
        return Stock.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Stock entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "stock_id,branch_id,medicine_id,quantity_boxes,version";
    }

    public void deductWithSync(String medicineId, int qtyBoxes) {
        deductWithSync(null, medicineId, qtyBoxes);
    }

    public void deductWithSync(String branchId, String medicineId, int qtyBoxes) {
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            Stock stock = findStock(stocks, branchId, medicineId);
            stock.deduct(qtyBoxes);
            writeAll(fileName, stocks);
        }
    }

    public void deductWithOptimistic(String medicineId, int qtyBoxes) {
        for (int attempt = 0; attempt < 5; attempt++) {
            List<Stock> stocks = readAll(fileName);
            Stock stock = findStock(stocks, null, medicineId);
            int expectedVersion = stock.getVersion();
            stock.deduct(qtyBoxes);

            List<Stock> latest = readAll(fileName);
            Stock latestStock = findStock(latest, null, medicineId);
            if (latestStock.getVersion() == expectedVersion) {
                writeAll(fileName, stocks);
                return;
            }
        }
        throw new IllegalStateException("Concurrent stock update detected for medicine " + medicineId);
    }

    public void deductWithFileLock(String medicineId, int qtyBoxes) {
        Path path = Paths.get(fileName);
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw"); FileChannel channel = file.getChannel()) {
            FileLock lock = channel.lock();
            try {
                List<Stock> stocks = readAll(fileName);
                Stock stock = findStock(stocks, null, medicineId);
                stock.deduct(qtyBoxes);
                writeAll(fileName, stocks);
            } finally {
                lock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to lock stock file", e);
        }
    }

    public boolean hasSufficientStock(String branchId, String medicineId, int requiredQty) {
        if (requiredQty <= 0) {
            return false;
        }

        return readAll(fileName).stream()
                .filter(stock -> medicineId.equals(stock.getMedicineId()))
                .filter(stock -> branchId == null || branchId.equals(stock.getBranchId()))
                .mapToInt(Stock::getQuantity)
                .sum() >= requiredQty;
    }

    public Stock findByStockId(String stockId) {
        if (stockId == null || stockId.isBlank()) {
            return null;
        }
        return readAll(fileName).stream()
                .filter(stock -> stockId.equals(stock.getStockId()))
                .findFirst()
                .orElse(null);
    }

    public void upsertStock(String branchId, String medicineId, int qtyBoxes) {
        if (qtyBoxes <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            for (int i = 0; i < stocks.size(); i++) {
                Stock current = stocks.get(i);
                if (!branchId.equals(current.getBranchId()) || !medicineId.equals(current.getMedicineId())) {
                    continue;
                }
                Stock updated = new Stock(
                        current.getStockId(),
                        current.getBranchId(),
                        current.getMedicineId(),
                        current.getQuantity() + qtyBoxes,
                        current.getVersion() + 1);
                stocks.set(i, updated);
                writeAll(fileName, stocks);
                return;
            }

            stocks.add(new Stock(generateNextStockId(stocks), branchId, medicineId, qtyBoxes, 1));
            writeAll(fileName, stocks);
        }
    }

    public void updateStock(String stockId, String newBranchId, String newMedicineId, int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            for (int i = 0; i < stocks.size(); i++) {
                Stock current = stocks.get(i);
                if (!stockId.equals(current.getStockId())) {
                    continue;
                }

                Stock updated = new Stock(
                        current.getStockId(),
                        newBranchId,
                        newMedicineId,
                        newQuantity,
                        current.getVersion() + 1);
                stocks.set(i, updated);
                writeAll(fileName, stocks);
                return;
            }
            throw new IllegalArgumentException("Stock not found: " + stockId);
        }
    }

    public void deleteByStockId(String stockId) {
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            boolean removed = stocks.removeIf(stock -> stockId.equals(stock.getStockId()));
            if (!removed) {
                throw new IllegalArgumentException("Stock not found: " + stockId);
            }
            writeAll(fileName, stocks);
        }
    }

    private String generateNextStockId(List<Stock> stocks) {
        int maxId = stocks.stream()
                .map(Stock::getStockId)
                .filter(id -> id != null && id.startsWith("S"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(1));
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("S%05d", maxId + 1);
    }

    private Stock findStock(List<Stock> stocks, String medicineId) {
        return findStock(stocks, null, medicineId);
    }

    private Stock findStock(List<Stock> stocks, String branchId, String medicineId) {
        return stocks.stream()
                .filter(stock -> medicineId.equals(stock.getMedicineId()))
                .filter(stock -> branchId == null || branchId.equals(stock.getBranchId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No stock found for medicine " + medicineId
                + (branchId != null ? " in branch " + branchId : "")));
    }

    public void deleteByMedicineId(String medicineId) {
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            boolean removed = stocks.removeIf(stock -> medicineId.equals(stock.getMedicineId()));
            if (removed) {
                writeAll(fileName, stocks);
            }
        }
    }
}
