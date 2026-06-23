package repository;

import java.util.List;
import java.util.stream.Collectors;

import model.PrescriptionItem;

public class PrescriptionItemRepository extends CsvRepository<PrescriptionItem> {

    private static final Object PRESCRIPTION_ITEM_LOCK = new Object();

    private final String fileName;

    public PrescriptionItemRepository() {
        this("data/prescription_items.csv");
    }

    public PrescriptionItemRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public PrescriptionItem parseLine(String line) {
        return PrescriptionItem.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(PrescriptionItem entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "prescription_item_id,prescription_id,medicine_id,quantity";
    }

    public List<PrescriptionItem> findByPrescriptionId(String prescriptionId) {
        return readAll(fileName).stream()
                .filter(item -> prescriptionId.equals(item.getPrescriptionId()))
                .collect(Collectors.toList());
    }

    public void save(PrescriptionItem item) {
        synchronized (PRESCRIPTION_ITEM_LOCK) {
            List<PrescriptionItem> items = readAll(fileName);
            items.add(item);
            writeAll(fileName, items);
        }
    }

    public String generateNextPrescriptionItemId() {
        synchronized (PRESCRIPTION_ITEM_LOCK) {
            int maxId = readAll(fileName).stream()
                    .map(PrescriptionItem::getPrescriptionItemId)
                    .filter(id -> id != null && id.startsWith("PI"))
                    .mapToInt(id -> {
                        try {
                            return Integer.parseInt(id.substring(2));
                        } catch (NumberFormatException ex) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            return String.format("PI%05d", maxId + 1);
        }
    }

    public void updateFirstMedicineIdByPrescriptionId(String prescriptionId, String medicineId) {
        synchronized (PRESCRIPTION_ITEM_LOCK) {
            List<PrescriptionItem> items = readAll(fileName);
            for (int i = 0; i < items.size(); i++) {
                PrescriptionItem current = items.get(i);
                if (!prescriptionId.equals(current.getPrescriptionId())) {
                    continue;
                }
                PrescriptionItem updated = new PrescriptionItem(
                        current.getPrescriptionItemId(),
                        current.getPrescriptionId(),
                        medicineId,
                        current.getQuantity());
                items.set(i, updated);
                writeAll(fileName, items);
                return;
            }
            throw new IllegalArgumentException("No prescription items found for prescription " + prescriptionId);
        }
    }
}
