package repository;

import java.util.List;
import java.util.stream.Collectors;

import model.PrescriptionItem;

public class PrescriptionItemRepository extends CsvRepository<PrescriptionItem> {
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
}
