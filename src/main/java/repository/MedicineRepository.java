package repository;

import java.util.List;

import model.Medicine;

public class MedicineRepository extends CsvRepository<Medicine> {

    private final String fileName;

    public MedicineRepository() {
        this("data/medicines.csv");
    }

    public MedicineRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Medicine parseLine(String line) {
        return Medicine.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Medicine entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "medicine_id,medicine_name,unit,units_per_box,description,manufacturer";
    }

    public List<Medicine> findAll() {
        return readAll(fileName);
    }
}
