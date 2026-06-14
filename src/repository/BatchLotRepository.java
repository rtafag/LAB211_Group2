package repository;

import java.util.Comparator;
import java.util.List;

import model.BatchLot;

public class BatchLotRepository extends CsvRepository<BatchLot> {
    private final String fileName;

    public BatchLotRepository() {
        this("data/batch_lots.csv");
    }

    public BatchLotRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public BatchLot parseLine(String line) {
        return BatchLot.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(BatchLot entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "batch_lot_id,medicine_id,branch_id,quantity_boxes,manufacture_date,expiry_date,version";
    }

    public BatchLot findBestLot(String medicineId) {
        return readAll(fileName).stream()
                .filter(lot -> medicineId.equals(lot.getMedicineId()))
                .filter(lot -> lot.getQuantity() > 0)
                .filter(lot -> !lot.isExpired())
                .min(Comparator.comparing(BatchLot::getExpiryDate)
                        .thenComparing(BatchLot::getBatchLotId))
                .orElse(null);
    }

    public void consumeFromLot(String lotId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        List<BatchLot> lots = readAll(fileName);
        for (int i = 0; i < lots.size(); i++) {
            BatchLot current = lots.get(i);
            if (!lotId.equals(current.getBatchLotId())) {
                continue;
            }
            if (current.isExpired()) {
                throw new IllegalStateException("Cannot consume from expired batch lot " + lotId);
            }
            if (current.getQuantity() < qty) {
                throw new IllegalStateException("Insufficient quantity in batch lot " + lotId);
            }

            BatchLot updated = new BatchLot(
                    current.getBatchLotId(),
                    current.getMedicineId(),
                    current.getBranchId(),
                    current.getQuantity() - qty,
                    current.getManufactureDate(),
                    current.getExpiryDate(),
                    current.getVersion() + 1);
            lots.set(i, updated);
            writeAll(fileName, lots);
            return;
        }

        throw new IllegalArgumentException("Batch lot not found: " + lotId);
    }
}
