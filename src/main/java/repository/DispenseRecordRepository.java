package repository;

import java.util.List;

import model.DispenseRecord;

public class DispenseRecordRepository extends CsvRepository<DispenseRecord> {
    private final String fileName;

    public DispenseRecordRepository() {
        this("data/dispense_records.csv");
    }

    public DispenseRecordRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public DispenseRecord parseLine(String line) {
        return DispenseRecord.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(DispenseRecord entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "dispense_record_id,prescription_id,pharmacist_id,dispensed_time,branch_id";
    }

    public void save(DispenseRecord record) {
        List<DispenseRecord> records = readAll(fileName);
        records.add(record);
        writeAll(fileName, records);
    }
}
