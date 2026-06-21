package util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DataGenerator {

    private static final String DATA_PATH = "data/";
    private static final Random rand = new Random();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String[] FIRSTNAMES = {"Nguyễn", "Trần", "Lê", "Phạm", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô",
        "Dương", "Lý", "Võ", "Đoàn", "Trịnh", "Phan", "Cao", "Chu", "Hà"};
    private static final String[] MIDDLENAMES = {"Văn", "Thị", "Hữu", "Quang", "Minh", "Hoàng", "Anh"};
    private static final String[] LASTNAMES = {"An", "Bình", "Cường", "Dũng", "Cao", "Nam", "Giang", "Hùng", "Yên",
        "Phúc", "Thảo", "Lan", "Hương", "Mai", "Thu", "Hà", "Linh", "Trang"};
    private static final String[] MEDICINES = {"Dietary supplement", "Vitamin supplements", "Multivitamin",
        "Mineral supplements", "Omega-3",
        "Herbal supplements", "Collagen", "Probiotics", "Protein powder", "Fiber supplements", "Glucosamine",
        "Chondroitin", "Coenzyme Q10",};
    private static final String[] UNITS = {"box", "bottle", "pack", "tube"};

    public static void main(String[] args) throws IOException {
        generateBranches();
        generateMedicines();
        generatePharmacists();
        generateStocks();
        generateBatchLots();
        generatePrescriptions();
        generatePrescriptionItems();
        generateDispenseRecords();
        System.out.println("Data generation completed.");
    }

    private static BufferedWriter openCsv(String fileName) throws IOException {
        Path path = Paths.get(DATA_PATH, fileName);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        writer.write("\uFEFF");
        return writer;
    }

    private static void generateBranches() throws IOException {
        try (BufferedWriter fw = openCsv("branches.csv")) {
            fw.write("branch_id,branch_name,address,phone\n");
            for (int i = 1; i <= 20; i++) {
                fw.write(String.format("B%03d,Branch %d,Address %d,090%07d\n", i, i, i,
                        1000000 + rand.nextInt(9000000)));
            }
        }
    }

    private static void generateMedicines() throws IOException {
        try (BufferedWriter fw = openCsv("medicines.csv")) {
            fw.write("medicine_id,medicine_name,unit,units_per_box,description,manufacturer\n");
            for (int i = 1; i <= 200; i++) {
                String unit = UNITS[rand.nextInt(UNITS.length)];
                int unitsPerBox = unit.equals("bottle") ? 6 + rand.nextInt(5) : 10 + rand.nextInt(6);
                fw.write(String.format("M%04d,%s,%s,%d,Desc %d,Manu %d\n", i,
                        MEDICINES[rand.nextInt(MEDICINES.length)], unit, unitsPerBox, i, 1 + rand.nextInt(10)));
            }
        }
    }

    private static void generatePharmacists() throws IOException {
        try (BufferedWriter fw = openCsv("pharmacists.csv")) {
            fw.write("pharmacist_id,pharmacistname,branch_id,username,password,role\n");
            for (int i = 1; i <= 100; i++) {
                String fullName = String.format("%s %s %s",
                        FIRSTNAMES[rand.nextInt(FIRSTNAMES.length)],
                        MIDDLENAMES[rand.nextInt(MIDDLENAMES.length)],
                        LASTNAMES[rand.nextInt(LASTNAMES.length)]);
                fw.write(
                        String.format("P%03d,%s,B%03d,user%d,password123,STAFF\n", i, fullName, 1 + rand.nextInt(20),
                                i));
            }
        }
    }

    private static void generateBatchLots() throws IOException {
        try (BufferedWriter fw = openCsv("batch_lots.csv")) {
            fw.write("batch_lot_id,medicine_id,branch_id,quantity,expiry_date,version\n");
            int nearExpiry = (int) (2000 * 0.2);
            for (int i = 1; i <= 2000; i++) {
                String expiry;
                if (i <= nearExpiry) {
                    expiry = LocalDate.now().plusDays(rand.nextInt(10) + 1).format(DATE_FMT); // 1-10 days
                } else {
                    expiry = LocalDate.now().plusDays(rand.nextInt(365) + 30).format(DATE_FMT); // 1-12 months
                }
                fw.write(String.format("BL%05d,M%04d,B%03d,%d,%s,1\n", i, 1 + rand.nextInt(200), 1 + rand.nextInt(20),
                        20 + rand.nextInt(31), expiry));
            }
        }
    }

    private static void generateStocks() throws IOException {
        try (BufferedWriter fw = openCsv("stocks.csv")) {
            fw.write("stock_id,branch_id,medicine_id,quantity,version\n");
            int id = 1;
            for (int b = 1; b <= 20; b++) {
                for (int m = 1; m <= 200; m += 10) { // 20x200/10 = 400
                    fw.write(String.format("S%05d,B%03d,M%04d,%d,1\n", id++, b, m, 50 + rand.nextInt(150)));
                }
            }
            // Fill up to 4000
            for (; id <= 4000; id++) {
                fw.write(String.format("S%05d,B%03d,M%04d,%d,1\n", id, 1 + rand.nextInt(20), 1 + rand.nextInt(200),
                        10 + rand.nextInt(200)));
            }
        }
    }

    private static void generatePrescriptions() throws IOException {
        try (BufferedWriter fw = openCsv("prescriptions.csv")) {
            fw.write(
                    "prescription_id,patient_name,patient_birthday,created_date,expired_date,status,branch_id,version\n");
            int pending = (int) (2000 * 0.6);
            for (int i = 1; i <= 2000; i++) {
                String status = i <= pending ? "PENDING" : "DISPENSED";
                String patientName = String.format("%s %s %s",
                        FIRSTNAMES[rand.nextInt(FIRSTNAMES.length)],
                        MIDDLENAMES[rand.nextInt(MIDDLENAMES.length)],
                        LASTNAMES[rand.nextInt(LASTNAMES.length)]);
                LocalDate dob = LocalDate.of(1950 + rand.nextInt(60), 1 + rand.nextInt(12), 1 + rand.nextInt(28));
                LocalDate created = LocalDate.now().minusDays(rand.nextInt(10));
                LocalDate expired = created.plusDays(5);
                fw.write(String.format("PR%05d,%s,%s,%s,%s,%s,B%03d,1\n", i, patientName, dob.format(DATE_FMT),
                        created.format(DATE_FMT), expired.format(DATE_FMT), status, 1 + rand.nextInt(20)));
            }
        }
    }

    private static void generatePrescriptionItems() throws IOException {
        try (BufferedWriter fw = openCsv("prescription_items.csv")) {
            fw.write("prescription_item_id,prescription_id,medicine_id,quantity\n");
            for (int i = 1; i <= 5000; i++) {
                fw.write(String.format("PI%05d,PR%05d,M%04d,%d\n", i, 1 + rand.nextInt(2000), 1 + rand.nextInt(200),
                        1 + rand.nextInt(5)));
            }
        }
    }

    private static void generateDispenseRecords() throws IOException {
        try (BufferedWriter fw = openCsv("dispense_records.csv")) {
            fw.write("dispense_record_id,prescription_id,pharmacist_id,dispensed_time,branch_id\n");
            for (int i = 1; i <= 1500; i++) {
                LocalDate dispensed = LocalDate.now().minusDays(rand.nextInt(30));
                fw.write(String.format("DR%05d,PR%05d,P%03d,%s,B%03d\n", i, 1 + rand.nextInt(2000),
                        1 + rand.nextInt(100), dispensed.format(DATE_FMT), 1 + rand.nextInt(20)));
            }
        }
    }
}
