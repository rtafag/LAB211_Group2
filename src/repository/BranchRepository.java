package repository;

import java.util.List;

import model.Branch;

public class BranchRepository extends CsvRepository<Branch> {

    private static final Object LOCK = new Object();
    private final String fileName;

    public BranchRepository() {
        this("data/branches.csv");
    }

    public BranchRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Branch parseLine(String line) {
        return Branch.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Branch entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "branch_id,branch_name,address,phone";
    }

    public List<Branch> findAll() {
        return readAll(fileName);
    }

    public Branch findById(String id) {
        return findAll().stream()
                .filter(b -> b.getBranchId().equals(id))
                .findFirst().orElse(null);
    }

    public void save(Branch branch) {
        synchronized (LOCK) {
            List<Branch> list = findAll();
            list.add(branch);
            writeAll(fileName, list);
        }
    }

    public void update(Branch updated) {
        synchronized (LOCK) {
            List<Branch> list = findAll();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getBranchId().equals(updated.getBranchId())) {
                    list.set(i, updated);
                    writeAll(fileName, list);
                    return;
                }
            }
            throw new IllegalArgumentException("Branch not found: " + updated.getBranchId());
        }
    }

    public void delete(String id) {
        synchronized (LOCK) {
            List<Branch> list = findAll();
            boolean removed = list.removeIf(b -> b.getBranchId().equals(id));
            if (!removed) {
                throw new IllegalArgumentException("Branch not found: " + id);
            }
            writeAll(fileName, list);
        }
    }

    public String generateNextId() {
        int maxId = findAll().stream()
                .map(Branch::getBranchId)
                .filter(id -> id != null && id.startsWith("B"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(1));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max().orElse(0);
        return String.format("B%03d", maxId + 1);
    }
}
