package controller;

import java.util.concurrent.CountDownLatch;

public class PharmacistThread extends Thread {
    private final String mode;
    private final String prescriptionId;
    private final String pharmacistId;
    private final SimulatorController controller;
    private final CountDownLatch startLatch;
    private final CountDownLatch endLatch;
    private boolean success;

    public PharmacistThread(String mode, String prescriptionId, String pharmacistId,
                            SimulatorController controller, CountDownLatch startLatch, CountDownLatch endLatch) {
        super("PharmacistThread-" + mode + "-" + pharmacistId);
        this.mode = mode;
        this.prescriptionId = prescriptionId;
        this.pharmacistId = pharmacistId;
        this.controller = controller;
        this.startLatch = startLatch;
        this.endLatch = endLatch;
        this.success = false;
    }

    @Override
    public void run() {
        try {
            startLatch.await(); // Wait for all threads to start concurrently
            success = controller.simulateDispense(mode, prescriptionId, pharmacistId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            success = false;
        } finally {
            endLatch.countDown(); // Signal completion
        }
    }

    public boolean isSuccess() {
        return success;
    }
}
