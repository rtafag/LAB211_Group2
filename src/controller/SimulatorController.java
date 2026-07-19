package controller;

import java.util.concurrent.CountDownLatch;

import repository.StockRepository;

public class SimulatorController {

    private final StockRepository stockRepo;

    public SimulatorController() {
        this.stockRepo = new StockRepository();
    }

    public void runSimulation(String mode, int numThreads) {
        System.out.println("=== Running simulation with mode: " + mode + " ===");

        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(() -> {
                try {
                    // Giả sử mỗi thread trừ 1 hộp thuốc M0001
                    String medicineId = "M0001";
                    int qtyBoxes = 1;

                    switch (mode) {
                        case "NO_LOCK":
                            // Không đồng bộ → dễ lỗi
                            stockRepo.findByStockId("S00001").deduct(qtyBoxes);
                            break;
                        case "SYNC":
                            stockRepo.deductWithSync(null, medicineId, qtyBoxes);
                            break;
                        case "OPTIMISTIC":
                            stockRepo.deductWithOptimistic(medicineId, qtyBoxes);
                            break;
                        case "FILE_LOCK":
                            stockRepo.deductWithFileLock(medicineId, qtyBoxes);
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown mode: " + mode);
                    }
                } finally {
                    latch.countDown();
                }
            });
            t.start();
        }

        try {
            latch.await(); // chờ tất cả thread kết thúc
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Sau khi chạy xong → kiểm tra lỗi
        detectInventoryInconsistency();
        detectDoubleDispensing();
        detectExpiredBatchDispensed();

        System.out.println("=== Simulation finished for mode: " + mode + " ===\n");
    }

    private void detectInventoryInconsistency() {
        // TODO: đọc stocks.csv → kiểm tra tồn kho âm hoặc sai
        System.out.println("Post-check: Inventory inconsistency detection...");
    }

    private void detectDoubleDispensing() {
        // TODO: đọc dispense_records.csv → kiểm tra đơn thuốc bị phát nhiều lần
        System.out.println("Post-check: Double dispensing detection...");
    }

    private void detectExpiredBatchDispensed() {
        // TODO: đọc batch_lots.csv → kiểm tra lô hết hạn vẫn bị xuất
        System.out.println("Post-check: Expired batch dispensed detection...");
    }
}
