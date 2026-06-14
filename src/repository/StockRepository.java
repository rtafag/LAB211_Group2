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
        synchronized (STOCK_LOCK) {
            List<Stock> stocks = readAll(fileName);
            Stock stock = findStock(stocks, medicineId);
            stock.deduct(qtyBoxes);
            writeAll(fileName, stocks);
        }
    }

    public void deductWithOptimistic(String medicineId, int qtyBoxes) {
        for (int attempt = 0; attempt < 5; attempt++) {
            List<Stock> stocks = readAll(fileName);
            Stock stock = findStock(stocks, medicineId);
            int expectedVersion = stock.getVersion();
            stock.deduct(qtyBoxes);

            List<Stock> latest = readAll(fileName);
            Stock latestStock = findStock(latest, medicineId);
            if (latestStock.getVersion() == expectedVersion) {
                writeAll(fileName, stocks);
                return;
            }
        }
        throw new IllegalStateException("Concurrent stock update detected for medicine " + medicineId);
    }

    public void deductWithFileLock(String medicineId, int qtyBoxes) {
        Path path = Paths.get(fileName);
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
                FileChannel channel = file.getChannel()) {
            FileLock lock = channel.lock();
            try {
                List<Stock> stocks = readAll(fileName);
                Stock stock = findStock(stocks, medicineId);
                stock.deduct(qtyBoxes);
                writeAll(fileName, stocks);
            } finally {
                lock.release();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to lock stock file", e);
        }
    }

    private Stock findStock(List<Stock> stocks, String medicineId) {
        return stocks.stream()
                .filter(stock -> medicineId.equals(stock.getMedicineId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No stock found for medicine " + medicineId));
    }
}
