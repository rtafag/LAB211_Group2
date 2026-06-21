package repository;

import java.util.List;
import model.User;

public class UserRepository extends CsvRepository<User> {
    private final String fileName;

    public UserRepository() {
        this("data/pharmacists.csv");
    }

    public UserRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public User parseLine(String line) {
        return User.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(User entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "user_id,password,role";
    }

    public User findById(String id) {
        List<User> users = readAll(fileName);
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
