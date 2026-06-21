import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import controller.*;
import repository.*;
import model.*;

public class ControllerTests {
    @Test
    public void testLoginSuccess() {
        UserRepository repo = new UserRepository("data/pharmacists.csv");
        LoginController login = new LoginController(repo);

        User u = login.login("user1", "password123");
        assertEquals("PHARMACIST", u.getRole());
    }

    @Test
    public void testLoginFail() {
        UserRepository repo = new UserRepository("data/pharmacists.csv");
        LoginController login = new LoginController(repo);

        assertThrows(IllegalArgumentException.class, () -> {
            login.login("user1", "wrongpass");
        });
    }

    @Test
    public void testProcessDispense() throws Exception {
        Path tempDir = Files.createTempDirectory("dispense-test");
        Path prescriptions = tempDir.resolve("prescriptions.csv");
        Path items = tempDir.resolve("prescription_items.csv");
        Path stocks = tempDir.resolve("stocks.csv");
        Path lots = tempDir.resolve("batch_lots.csv");
        Path records = tempDir.resolve("dispense_records.csv");

        Files.writeString(prescriptions, String.join(System.lineSeparator(),
                "prescription_id,patient_name,patient_dob,created_date,expired_date,status,branch_id,version",
                "PR001,Test User,1990-01-01,2026-06-01,2026-06-10,PENDING,B001,1",
                ""));
        Files.writeString(items, String.join(System.lineSeparator(),
                "prescription_item_id,prescription_id,medicine_id,quantity",
                "PI001,PR001,M001,2",
                ""));
        Files.writeString(stocks, String.join(System.lineSeparator(),
                "stock_id,branch_id,medicine_id,quantity_boxes,version",
                "S001,B001,M001,5,1",
                ""));
        Files.writeString(lots, String.join(System.lineSeparator(),
                "batch_lot_id,medicine_id,branch_id,quantity_boxes,expiry_date,version",
                "BL001,M001,B001,5,2026-12-31,1",
                ""));
        Files.writeString(records, "dispense_record_id,prescription_id,pharmacist_id,dispensed_time,branch_id\n");

        PrescriptionRepository presRepo = new PrescriptionRepository(prescriptions.toString());
        StockRepository stockRepo = new StockRepository(stocks.toString());
        BatchLotRepository lotRepo = new BatchLotRepository(lots.toString());
        PrescriptionItemRepository itemRepo = new PrescriptionItemRepository(items.toString());
        DispenseRecordRepository recordRepo = new DispenseRecordRepository(records.toString());

        DispenseController dispenseController = new DispenseController(presRepo, stockRepo, lotRepo, itemRepo,
                recordRepo);
        dispenseController.processDispense("PR001", "P001");

        Prescription updated = presRepo.findById("PR001");
        assertNotNull(updated);
        assertEquals("DISPENSED", updated.getStatus());

        List<Stock> updatedStocks = stockRepo.readAll(stocks.toString());
        assertEquals(3, updatedStocks.get(0).getQuantity());

        List<BatchLot> updatedLots = lotRepo.readAll(lots.toString());
        assertEquals(3, updatedLots.get(0).getQuantity());

        List<DispenseRecord> savedRecords = recordRepo.readAll(records.toString());
        assertEquals(2, savedRecords.size());
        assertEquals("PR001", savedRecords.get(1).getPrescriptionId());
    }
}
