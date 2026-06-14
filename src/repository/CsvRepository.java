package repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public abstract class CsvRepository<T> {
    public abstract T parseLine(String line);

    public abstract String toCsvLine(T entity);

    public List<T> readAll(String fileName) {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<T> entities = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line == null ? "" : line.trim();
                if (trimmed.isEmpty() || isHeader(trimmed)) {
                    continue;
                }
                entities.add(parseLine(trimmed));
            }
            return entities;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV file: " + fileName, e);
        }
    }

    public void writeAll(String fileName, List<T> entities) {
        Path path = Paths.get(fileName);
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            List<String> lines = new ArrayList<>();
            String header = getHeader();
            if (header != null && !header.isBlank()) {
                lines.add(header);
            }
            for (T entity : entities) {
                lines.add(toCsvLine(entity));
            }
            Files.write(path, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file: " + fileName, e);
        }
    }

    protected boolean isHeader(String line) {
        String lower = line.toLowerCase();
        return lower.contains("stock_id")
                || lower.contains("prescription_id")
                || lower.contains("batch_lot_id")
                || lower.contains("branch_id")
                || lower.contains("medicine_id")
                || lower.contains("quantity")
                || lower.contains("version")
                || lower.contains("patient_name")
                || lower.contains("created_date")
                || lower.contains("expired_date")
                || lower.contains("manufacture_date");
    }

    protected String getHeader() {
        return null;
    }
}
