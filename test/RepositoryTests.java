import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import model.BatchLot;
import model.Prescription;
import model.PrescriptionStatus;
import model.Stock;
import repository.BatchLotRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;

public class RepositoryTests {
    public static void main(String[] args) throws Exception {
        boolean ok = true;

        Path tempDir = Files.createTempDirectory("repo-tests");
        Path stockFile = tempDir.resolve("stocks.csv");
        Files.writeString(stockFile, String.join(System.lineSeparator(),
                "stock_id,branch_id,medicine_id,quantity,version",
                "S001,B001,M001,10,1",
                ""));

        StockRepository stockRepo = new StockRepository(stockFile.toString());
        stockRepo.deductWithSync("M001", 3);
        List<Stock> stocks = stockRepo.readAll(stockFile.toString());
        Stock updated = stocks.get(0);
        if (updated.getQuantity() != 7 || updated.getVersion() != 2) {
            System.err.println("FAIL: StockRepository.deductWithSync did not update correctly");
            ok = false;
        }

        Path prescriptionFile = tempDir.resolve("prescriptions.csv");
        Files.writeString(prescriptionFile, String.join(System.lineSeparator(),
                "prescription_id,patient_name,patient_dob,created_date,expired_date,status,branch_id,version",
                "PR001,Test,1990-01-01,2026-01-01,2026-12-31,PENDING,B001,1",
                ""));

        PrescriptionRepository prescriptionRepo = new PrescriptionRepository(prescriptionFile.toString());
        prescriptionRepo.markDispensed("PR001");
        try {
            prescriptionRepo.markDispensed("PR001");
            System.err.println("FAIL: second dispense should throw PrescriptionAlreadyDispensedException");
            ok = false;
        } catch (PrescriptionRepository.PrescriptionAlreadyDispensedException expected) {
            // expected
        }

        List<Prescription> prescriptions = prescriptionRepo.readAll(prescriptionFile.toString());
        Prescription dispensed = prescriptions.get(0);
        if (dispensed.toCsvLine().contains("DISPENSED") == false) {
            System.err.println("FAIL: PrescriptionRepository.markDispensed did not mark status as DISPENSED");
            ok = false;
        }

        Path concurrentPrescriptionFile = tempDir.resolve("prescriptions-concurrent.csv");
        Files.writeString(concurrentPrescriptionFile, String.join(System.lineSeparator(),
                "prescription_id,patient_name,patient_dob,created_date,expired_date,status,branch_id,version",
                "PR002,Concurrent,1990-01-01,2026-01-01,2026-12-31,PENDING,B001,1",
                ""));
        PrescriptionRepository concurrentRepo = new PrescriptionRepository(concurrentPrescriptionFile.toString());
        final int[] successCount = { 0 };
        final int[] failureCount = { 0 };
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    concurrentRepo.markDispensed("PR002");
                    synchronized (System.out) {
                        successCount[0]++;
                    }
                } catch (PrescriptionRepository.PrescriptionAlreadyDispensedException ex) {
                    synchronized (System.out) {
                        failureCount[0]++;
                    }
                } catch (Exception ex) {
                    synchronized (System.out) {
                        failureCount[0]++;
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        if (successCount[0] != 1 || failureCount[0] != 4) {
            System.err.println("FAIL: concurrent dispensing should yield exactly one success and four failures");
            ok = false;
        }

        Path batchFile = tempDir.resolve("batch_lots.csv");
        Files.writeString(batchFile, String.join(System.lineSeparator(),
                "batch_lot_id,medicine_id,branch_id,quantity,manufacture_date,expiry_date,version",
                "BL001,M001,B001,20,2026-01-01,2026-12-31,1",
                "BL002,M001,B001,15,2026-01-01,2026-06-30,1",
                ""));

        BatchLotRepository batchRepo = new BatchLotRepository(batchFile.toString());
        BatchLot chosen = batchRepo.findBestLot("M001");
        if (chosen == null || !"BL002".equals(chosen.getBatchLotId())) {
            System.err.println("FAIL: BatchLotRepository.findBestLot did not select the FEFO batch");
            ok = false;
        }

        batchRepo.consumeFromLot("BL002", 5);
        List<BatchLot> lots = batchRepo.readAll(batchFile.toString());
        BatchLot lot = lots.stream().filter(l -> "BL002".equals(l.getBatchLotId())).findFirst().orElse(null);
        if (lot == null || lot.getQuantity() != 10) {
            System.err.println("FAIL: BatchLotRepository.consumeFromLot did not reduce quantity correctly");
            ok = false;
        }

        if (ok) {
            System.out.println("REPOSITORY TESTS PASSED");
            System.exit(0);
        } else {
            System.err.println("REPOSITORY TESTS FAILED");
            System.exit(2);
        }
    }
}
