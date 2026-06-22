package repository;

import java.util.List;

import model.Branch;

public class BranchRepository extends CsvRepository<Branch> {

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
}
