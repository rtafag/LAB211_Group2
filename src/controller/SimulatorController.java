package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import model.BatchLot;
import model.DispenseRecord;
import model.Prescription;
import model.Stock;
import repository.BatchLotRepository;
import repository.DispenseRecordRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;
import view.SimulatorView.SimulationResult;

public class SimulatorController {

    private final PrescriptionRepository presRepo;
    private final StockRepository stockRepo;
    private final BatchLotRepository lotRepo;
    private final DispenseRecordRepository recordRepo;

    // Simulation temporary lists to run in-memory for speed and platform compatibility
    private List<Prescription> simPrescriptions;
    private List<Stock> simStocks;
    private List<BatchLot> simBatchLots;
    private List<DispenseRecord> simDispenseRecords;

    private final Object syncLock = new Object();
    private final ReentrantLock fileLockSim = new ReentrantLock();

    public SimulatorController() {
        this.presRepo = new PrescriptionRepository();
        this.stockRepo = new StockRepository();
        this.lotRepo = new BatchLotRepository();
        this.recordRepo = new DispenseRecordRepository();
    }

    public void runAllSimulations(List<SimulationResult> resultsList) {
        backupDataFiles();
        try {
            resultsList.add(runSimulationInternal("NO_LOCK", 200));
            resultsList.add(runSimulationInternal("SYNC", 200));
            resultsList.add(runSimulationInternal("OPTIMISTIC", 200));
            resultsList.add(runSimulationInternal("FILE_LOCK", 200));
        } finally {
            restoreDataFiles();
            cleanupBackupFiles();
        }
    }

    public SimulationResult runSimulationInternal(String mode, int numThreads) {
        System.out.println("Starting simulation for mode: " + mode + " with " + numThreads + " threads...");

        // 1. Reset state by restoring backup and reading clean records into memory lists
        restoreDataFiles();
        loadInMemoryData();

        // 2. Prepare the environment for the simulation:
        // - Insert/Update PR_SIM_001 prescription
        // - Set stock for M0001 in B006 to 300 boxes
        // - Set BL00801 (valid batch lot) quantity to 5 boxes
        // - Set BL00015 (expired batch lot) quantity to 30 boxes
        prepareSimulationData();

        // 3. Create coordination latches
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        List<PharmacistThread> threads = new ArrayList<>();
        for (int i = 1; i <= numThreads; i++) {
            threads.add(new PharmacistThread(mode, "PR_SIM_001", "P" + String.format("%03d", i), this, startLatch, endLatch));
        }

        // Start threads (they wait on startLatch)
        for (PharmacistThread t : threads) {
            t.start();
        }

        long startTime = System.currentTimeMillis();
        startLatch.countDown(); // Go!

        try {
            endLatch.await(); // Wait for all threads to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // 4. Save the simulated in-memory state back to the CSV files so post-verifications can read them
        saveInMemoryData();

        // 5. Count successful dispensings in this run
        int successCount = 0;
        for (PharmacistThread t : threads) {
            if (t.isSuccess()) {
                successCount++;
            }
        }

        // 6. Run post-verifications
        int invIncon = detectInventoryInconsistency(300, successCount);
        int doubleDisp = detectDoubleDispensing();
        int expiredDisp = detectExpiredBatchDispensed(30);

        System.out.println("Finished mode: " + mode + ". Success: " + successCount + ", Duration: " + duration + " ms.");
        return new SimulationResult(mode, numThreads, duration, numThreads, successCount, invIncon, doubleDisp, expiredDisp);
    }

    private void loadInMemoryData() {
        simPrescriptions = presRepo.readAll("data/prescriptions.csv");
        simStocks = stockRepo.readAll("data/stocks.csv");
        simBatchLots = lotRepo.readAll("data/batch_lots.csv");
        simDispenseRecords = recordRepo.readAll("data/dispense_records.csv");
    }

    private void saveInMemoryData() {
        presRepo.writeAll("data/prescriptions.csv", simPrescriptions);
        stockRepo.writeAll("data/stocks.csv", simStocks);
        lotRepo.writeAll("data/batch_lots.csv", simBatchLots);
        recordRepo.writeAll("data/dispense_records.csv", simDispenseRecords);
    }

    private void prepareSimulationData() {
        // Prepare prescription: PR_SIM_001, B006 branch, M0001 medicine
        Prescription testPres = new Prescription("PR_SIM_001", "Simulation Patient", "1990-01-01", "2026-07-19", "2026-07-26", "PENDING", "B006", 1);
        boolean foundPres = false;
        for (int i = 0; i < simPrescriptions.size(); i++) {
            if (simPrescriptions.get(i).getPrescriptionId().equals("PR_SIM_001")) {
                simPrescriptions.set(i, testPres);
                foundPres = true;
                break;
            }
        }
        if (!foundPres) {
            simPrescriptions.add(testPres);
        }

        // Prepare Stock: S00101 (or find/add stock for M0001, B006) to 300
        boolean foundStock = false;
        for (int i = 0; i < simStocks.size(); i++) {
            Stock s = simStocks.get(i);
            if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                simStocks.set(i, new Stock(s.getStockId(), "B006", "M0001", 300, 1));
                foundStock = true;
                break;
            }
        }
        if (!foundStock) {
            simStocks.add(new Stock("S00101", "B006", "M0001", 300, 1));
        }

        // Prepare BatchLots:
        // BL00801 (valid) -> 5
        // BL00015 (expired) -> 30
        boolean foundValidLot = false;
        boolean foundExpiredLot = false;
        for (int i = 0; i < simBatchLots.size(); i++) {
            BatchLot lot = simBatchLots.get(i);
            if ("BL00801".equals(lot.getBatchLotId())) {
                simBatchLots.set(i, new BatchLot("BL00801", "M0001", "B006", 5, java.time.LocalDate.now().plusMonths(3), 1));
                foundValidLot = true;
            } else if ("BL00015".equals(lot.getBatchLotId())) {
                simBatchLots.set(i, new BatchLot("BL00015", "M0001", "B006", 30, java.time.LocalDate.now().minusDays(5), 1));
                foundExpiredLot = true;
            }
        }
        if (!foundValidLot) {
            simBatchLots.add(new BatchLot("BL00801", "M0001", "B006", 5, java.time.LocalDate.now().plusMonths(3), 1));
        }
        if (!foundExpiredLot) {
            simBatchLots.add(new BatchLot("BL00015", "M0001", "B006", 30, java.time.LocalDate.now().minusDays(5), 1));
        }

        // Clear previous simulation dispense records
        simDispenseRecords.removeIf(r -> "PR_SIM_001".equals(r.getPrescriptionId()));
    }

    public boolean simulateDispense(String mode, String prescriptionId, String pharmacistId) {
        switch (mode) {
            case "NO_LOCK":
                return dispenseNoLock(prescriptionId, pharmacistId);
            case "SYNC":
                return dispenseSync(prescriptionId, pharmacistId);
            case "OPTIMISTIC":
                return dispenseOptimistic(prescriptionId, pharmacistId);
            case "FILE_LOCK":
                return dispenseFileLock(prescriptionId, pharmacistId);
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    private boolean dispenseNoLock(String prescriptionId, String pharmacistId) {
        // 1. Read prescription (without lock)
        Prescription p = null;
        for (Prescription x : simPrescriptions) {
            if (x.getPrescriptionId().equals(prescriptionId)) {
                p = x;
                break;
            }
        }
        if (p == null || "DISPENSED".equals(p.getStatus())) {
            return false;
        }

        // 2. Check stock
        Stock stock = null;
        for (Stock s : simStocks) {
            if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                stock = s;
                break;
            }
        }
        if (stock == null || stock.getQuantity() < 1) {
            return false;
        }

        // 3. Find batch lot
        BatchLot lot = null;
        BatchLot validLot = null;
        BatchLot expiredLot = null;
        for (BatchLot l : simBatchLots) {
            if ("BL00801".equals(l.getBatchLotId())) {
                validLot = l;
            } else if ("BL00015".equals(l.getBatchLotId())) {
                expiredLot = l;
            }
        }

        // In NO_LOCK, if valid lot is empty, fall back to expired lot to show the bug
        if (validLot != null && validLot.getQuantity() > 0) {
            lot = validLot;
        } else {
            lot = expiredLot;
        }

        if (lot == null || lot.getQuantity() < 1) {
            return false;
        }

        // Simulate concurrent processing delay
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 4. Deduct stock and batch lot
        stock.deduct(1);
        lot.deduct(1);

        // 5. Mark prescription as dispensed
        Prescription updated = new Prescription(
                p.getPrescriptionId(), p.getPatientName(), p.getPatientDob(),
                p.getCreatedDate(), p.getExpiredDate(), "DISPENSED", p.getBranchId(), p.getVersion() + 1
        );
        for (int i = 0; i < simPrescriptions.size(); i++) {
            if (simPrescriptions.get(i).getPrescriptionId().equals(prescriptionId)) {
                simPrescriptions.set(i, updated);
                break;
            }
        }

        // 6. Save dispense record
        DispenseRecord record = new DispenseRecord(
                "DR" + System.nanoTime() + "_" + (int)(Math.random() * 1000),
                prescriptionId, pharmacistId, java.time.LocalDate.now(), "B006"
        );
        simDispenseRecords.add(record);

        return true;
    }

    private boolean dispenseSync(String prescriptionId, String pharmacistId) {
        synchronized (syncLock) {
            // 1. Read prescription
            Prescription p = null;
            for (Prescription x : simPrescriptions) {
                if (x.getPrescriptionId().equals(prescriptionId)) {
                    p = x;
                    break;
                }
            }
            if (p == null || "DISPENSED".equals(p.getStatus())) {
                return false;
            }

            // 2. Check stock
            Stock stock = null;
            for (Stock s : simStocks) {
                if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                    stock = s;
                    break;
                }
            }
            if (stock == null || stock.getQuantity() < 1) {
                return false;
            }

            // 3. Find batch lot (only valid lot)
            BatchLot lot = null;
            for (BatchLot l : simBatchLots) {
                if ("BL00801".equals(l.getBatchLotId())) {
                    lot = l;
                    break;
                }
            }
            if (lot == null || lot.getQuantity() < 1 || lot.isExpired()) {
                return false;
            }

            // 4. Deduct stock and batch lot
            stock.deduct(1);
            lot.deduct(1);

            // 5. Mark prescription as dispensed
            Prescription updated = new Prescription(
                    p.getPrescriptionId(), p.getPatientName(), p.getPatientDob(),
                    p.getCreatedDate(), p.getExpiredDate(), "DISPENSED", p.getBranchId(), p.getVersion() + 1
            );
            for (int i = 0; i < simPrescriptions.size(); i++) {
                if (simPrescriptions.get(i).getPrescriptionId().equals(prescriptionId)) {
                    simPrescriptions.set(i, updated);
                    break;
                }
            }

            // 6. Save dispense record
            DispenseRecord record = new DispenseRecord(
                    "DR" + System.nanoTime(),
                    prescriptionId, pharmacistId, java.time.LocalDate.now(), "B006"
            );
            simDispenseRecords.add(record);

            return true;
        }
    }

    private boolean dispenseOptimistic(String prescriptionId, String pharmacistId) {
        for (int attempt = 0; attempt < 10; attempt++) {
            Prescription p = null;
            synchronized (syncLock) {
                for (Prescription x : simPrescriptions) {
                    if (x.getPrescriptionId().equals(prescriptionId)) {
                        p = x;
                        break;
                    }
                }
            }
            if (p == null) return false;
            if ("DISPENSED".equals(p.getStatus())) {
                return false; // Already dispensed
            }

            Stock stock = null;
            synchronized (syncLock) {
                for (Stock s : simStocks) {
                    if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                        stock = s;
                        break;
                    }
                }
            }
            if (stock == null || stock.getQuantity() < 1) {
                return false;
            }

            BatchLot lot = null;
            synchronized (syncLock) {
                for (BatchLot l : simBatchLots) {
                    if ("BL00801".equals(l.getBatchLotId())) {
                        lot = l;
                        break;
                    }
                }
            }
            if (lot == null || lot.getQuantity() < 1 || lot.isExpired()) {
                return false;
            }

            int expectedPresVersion = p.getVersion();
            int expectedStockVersion = stock.getVersion();
            int expectedLotVersion = lot.getVersion();

            // Try to write atomically if versions haven't changed
            synchronized (syncLock) {
                Prescription currentP = null;
                for (Prescription x : simPrescriptions) {
                    if (x.getPrescriptionId().equals(prescriptionId)) {
                        currentP = x;
                        break;
                    }
                }
                Stock currentS = null;
                for (Stock s : simStocks) {
                    if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                        currentS = s;
                        break;
                    }
                }
                BatchLot currentL = null;
                for (BatchLot l : simBatchLots) {
                    if ("BL00801".equals(l.getBatchLotId())) {
                        currentL = l;
                        break;
                    }
                }

                if (currentP == null || currentP.getVersion() != expectedPresVersion ||
                    currentS == null || currentS.getVersion() != expectedStockVersion ||
                    currentL == null || currentL.getVersion() != expectedLotVersion) {
                    // Conflict! Yield/sleep briefly and retry
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // Write update
                currentS.deduct(1);
                currentL.deduct(1);

                Prescription updated = new Prescription(
                        p.getPrescriptionId(), p.getPatientName(), p.getPatientDob(),
                        p.getCreatedDate(), p.getExpiredDate(), "DISPENSED", p.getBranchId(), p.getVersion() + 1
                );
                for (int i = 0; i < simPrescriptions.size(); i++) {
                    if (simPrescriptions.get(i).getPrescriptionId().equals(prescriptionId)) {
                        simPrescriptions.set(i, updated);
                        break;
                    }
                }

                DispenseRecord record = new DispenseRecord(
                        "DR" + System.nanoTime(),
                        prescriptionId, pharmacistId, java.time.LocalDate.now(), "B006"
                );
                simDispenseRecords.add(record);

                return true;
            }
        }
        return false;
    }

    private boolean dispenseFileLock(String prescriptionId, String pharmacistId) {
        fileLockSim.lock();
        try {
            // 1. Read prescription
            Prescription p = null;
            for (Prescription x : simPrescriptions) {
                if (x.getPrescriptionId().equals(prescriptionId)) {
                    p = x;
                    break;
                }
            }
            if (p == null || "DISPENSED".equals(p.getStatus())) {
                return false;
            }

            // 2. Check stock
            Stock stock = null;
            for (Stock s : simStocks) {
                if ("M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId())) {
                    stock = s;
                    break;
                }
            }
            if (stock == null || stock.getQuantity() < 1) {
                return false;
            }

            // 3. Find batch lot (only valid lot)
            BatchLot lot = null;
            for (BatchLot l : simBatchLots) {
                if ("BL00801".equals(l.getBatchLotId())) {
                    lot = l;
                    break;
                }
            }
            if (lot == null || lot.getQuantity() < 1 || lot.isExpired()) {
                return false;
            }

            // 4. Deduct stock and batch lot
            stock.deduct(1);
            lot.deduct(1);

            // 5. Mark prescription as dispensed
            Prescription updated = new Prescription(
                    p.getPrescriptionId(), p.getPatientName(), p.getPatientDob(),
                    p.getCreatedDate(), p.getExpiredDate(), "DISPENSED", p.getBranchId(), p.getVersion() + 1
            );
            for (int i = 0; i < simPrescriptions.size(); i++) {
                if (simPrescriptions.get(i).getPrescriptionId().equals(prescriptionId)) {
                    simPrescriptions.set(i, updated);
                    break;
                }
            }

            // 6. Save dispense record
            DispenseRecord record = new DispenseRecord(
                    "DR" + System.nanoTime(),
                    prescriptionId, pharmacistId, java.time.LocalDate.now(), "B006"
            );
            simDispenseRecords.add(record);

            return true;
        } finally {
            fileLockSim.unlock();
        }
    }

    private int detectInventoryInconsistency(int initialStock, int successCount) {
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        Stock stock = stocks.stream()
                .filter(s -> "M0001".equals(s.getMedicineId()) && "B006".equals(s.getBranchId()))
                .findFirst()
                .orElse(null);
        if (stock == null) return 0;
        int expected = initialStock - successCount;
        if (stock.getQuantity() != expected) {
            return Math.abs(stock.getQuantity() - expected);
        }
        return 0;
    }

    private int detectDoubleDispensing() {
        List<DispenseRecord> records = recordRepo.readAll("data/dispense_records.csv");
        int count = 0;
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        for (DispenseRecord r : records) {
            if ("PR_SIM_001".equals(r.getPrescriptionId())) {
                counts.put(r.getPrescriptionId(), counts.getOrDefault(r.getPrescriptionId(), 0) + 1);
            }
        }
        for (int c : counts.values()) {
            if (c > 1) {
                count += (c - 1);
            }
        }
        return count;
    }

    private int detectExpiredBatchDispensed(int initialExpiredQty) {
        List<BatchLot> lots = lotRepo.readAll("data/batch_lots.csv");
        BatchLot lot = lots.stream()
                .filter(l -> "BL00015".equals(l.getBatchLotId()))
                .findFirst()
                .orElse(null);
        if (lot == null) return 0;
        if (lot.getQuantity() < initialExpiredQty) {
            return initialExpiredQty - lot.getQuantity();
        }
        return 0;
    }

    private void backupDataFiles() {
        try {
            Files.copy(Paths.get("data/stocks.csv"), Paths.get("data/stocks.csv.bak"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("data/batch_lots.csv"), Paths.get("data/batch_lots.csv.bak"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("data/prescriptions.csv"), Paths.get("data/prescriptions.csv.bak"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get("data/dispense_records.csv"), Paths.get("data/dispense_records.csv.bak"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to backup data files: " + e.getMessage());
        }
    }

    private void restoreDataFiles() {
        try {
            if (Files.exists(Paths.get("data/stocks.csv.bak"))) {
                Files.copy(Paths.get("data/stocks.csv.bak"), Paths.get("data/stocks.csv"), StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(Paths.get("data/batch_lots.csv.bak"))) {
                Files.copy(Paths.get("data/batch_lots.csv.bak"), Paths.get("data/batch_lots.csv"), StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(Paths.get("data/prescriptions.csv.bak"))) {
                Files.copy(Paths.get("data/prescriptions.csv.bak"), Paths.get("data/prescriptions.csv"), StandardCopyOption.REPLACE_EXISTING);
            }
            if (Files.exists(Paths.get("data/dispense_records.csv.bak"))) {
                Files.copy(Paths.get("data/dispense_records.csv.bak"), Paths.get("data/dispense_records.csv"), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Failed to restore data files: " + e.getMessage());
        }
    }

    private void cleanupBackupFiles() {
        try {
            Files.deleteIfExists(Paths.get("data/stocks.csv.bak"));
            Files.deleteIfExists(Paths.get("data/batch_lots.csv.bak"));
            Files.deleteIfExists(Paths.get("data/prescriptions.csv.bak"));
            Files.deleteIfExists(Paths.get("data/dispense_records.csv.bak"));
        } catch (IOException e) {
            System.err.println("Failed to cleanup backup files: " + e.getMessage());
        }
    }
}
