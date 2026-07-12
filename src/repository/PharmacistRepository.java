package repository;

import java.util.List;

import model.Pharmacist;

public class PharmacistRepository extends CsvRepository<Pharmacist> {

    private static final Object LOCK = new Object();
    private final String fileName;

    public PharmacistRepository() {
        this("data/pharmacists.csv");
    }

    public PharmacistRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Pharmacist parseLine(String line) {
        return Pharmacist.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Pharmacist entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "pharmacist_id,pharmacist_name,branch_id,phone_number,password,role";
    }

    public List<Pharmacist> findAll() {
        return readAll(fileName);
    }

    public void save(Pharmacist pharmacist) {
        synchronized (LOCK) {
            List<Pharmacist> list = findAll();
            list.add(pharmacist);
            writeAll(fileName, list);
        }
    }

    public void update(Pharmacist updated) {
        synchronized (LOCK) {
            List<Pharmacist> list = findAll();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getPharmacistId().equals(updated.getPharmacistId())) {
                    list.set(i, updated);
                    writeAll(fileName, list);
                    return;
                }
            }
            throw new IllegalArgumentException("Pharmacist not found: " + updated.getPharmacistId());
        }
    }

    public void delete(String id) {
        synchronized (LOCK) {
            List<Pharmacist> list = findAll();
            boolean removed = list.removeIf(p -> p.getPharmacistId().equals(id));
            if (!removed) {
                throw new IllegalArgumentException("Pharmacist not found: " + id);
            }
            writeAll(fileName, list);
        }
    }

    public String generateNextId() {
        int maxId = findAll().stream()
                .map(Pharmacist::getPharmacistId)
                .filter(id -> id != null && id.startsWith("P"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(1));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max().orElse(0);
        return String.format("P%03d", maxId + 1);
    }
}
