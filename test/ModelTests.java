
import java.time.LocalDate;

import model.BatchLot;

public class ModelTests {

    public static void main(String[] args) {
        boolean ok = true;

        // Use BatchLot for inventory (no Stock)
        BatchLot batch = BatchLot.fromCsvLine("BL00001,M0001,B001,100," + LocalDate.now().plusDays(30).toString() + ",1");
        try {
            batch.deduct(25);
            if (batch.getQuantity() != 75) {
                System.err.println("FAIL: BatchLot.deduct did not reduce quantity correctly");
                ok = false;
            }
            if (batch.getVersion() != 2) {
                System.err.println("FAIL: BatchLot.deduct did not increment version");
                ok = false;
            }
        } catch (Exception e) {
            System.err.println("FAIL: BatchLot.deduct threw unexpected: " + e);
            ok = false;
        }

        // Overdraw should throw
        try {
            batch.deduct(1000);
            System.err.println("FAIL: BatchLot.deduct allowed overdraft");
            ok = false;
        } catch (IllegalStateException ex) {
            // expected
        }

        // Zero/negative should throw
        try {
            batch.deduct(0);
            System.err.println("FAIL: BatchLot.deduct allowed zero amount");
            ok = false;
        } catch (IllegalArgumentException ex) {
            // expected
        }

        // Test expiry semantics
        BatchLot past = new BatchLot("BL00002", "M0002", "B001", 10, LocalDate.now().minusDays(1), 1);
        BatchLot today = new BatchLot("BL00003", "M0003", "B001", 10, LocalDate.now(), 1);
        BatchLot future = new BatchLot("BL00004", "M0004", "B001", 10, LocalDate.now().plusDays(1), 1);

        if (!past.isExpired()) {
            System.err.println("FAIL: past batch should be expired");
            ok = false;
        }
        if (today.isExpired()) {
            System.err.println("FAIL: today batch should not be expired");
            ok = false;
        }
        if (future.isExpired()) {
            System.err.println("FAIL: future batch should not be expired");
            ok = false;
        }

        // Round-trip CSV for BatchLot
        BatchLot b2 = BatchLot.fromCsvLine(past.toCsvLine());
        if (!b2.equals(past)) {
            System.err.println("FAIL: BatchLot CSV round-trip mismatch");
            ok = false;
        }

        if (ok) {
            System.out.println("ALL TESTS PASSED");
            System.exit(0);
        } else {
            System.err.println("SOME TESTS FAILED");
            System.exit(2);
        }
    }
}
