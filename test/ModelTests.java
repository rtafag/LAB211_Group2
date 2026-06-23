import java.time.LocalDate;

import model.BatchLot;
import model.Stock;

public class ModelTests {

    public static void main(String[] args) {
        boolean ok = true;

        // Test Stock.deduct
        Stock s = Stock.fromCsvLine("S00001,B001,M0001,100,1");
        try {
            s.deduct(30);
            if (s.getQuantity() != 70) {
                System.err.println("FAIL: Stock.deduct did not reduce quantity correctly");
                ok = false;
            }
        } catch (Exception e) {
            System.err.println("FAIL: Stock.deduct threw unexpected: " + e);
            ok = false;
        }

        // Overdraw should throw
        try {
            s.deduct(1000);
            System.err.println("FAIL: Stock.deduct allowed overdraft");
            ok = false;
        } catch (IllegalStateException ex) {
            // expected
        }

        // Test BatchLot.isExpired
        BatchLot past = new BatchLot("BL00001", "M0001", "B001", 10, LocalDate.now().minusDays(1), 1);
        BatchLot today = new BatchLot("BL00002", "M0002", "B001", 10, LocalDate.now(), 1);
        BatchLot future = new BatchLot("BL00003", "M0003", "B001", 10, LocalDate.now().plusDays(1), 1);

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

        // Round-trip CSV for Stock
        Stock s2 = Stock.fromCsvLine(s.toCsvLine());
        if (!s2.equals(s)) {
            System.err.println("FAIL: Stock CSV round-trip mismatch");
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
