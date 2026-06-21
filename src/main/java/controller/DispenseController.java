package controller;

import java.time.LocalDate;
import java.util.List;

import model.BatchLot;
import model.DispenseRecord;
import model.Prescription;
import model.PrescriptionItem;
import repository.BatchLotRepository;
import repository.DispenseRecordRepository;
import repository.PrescriptionItemRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;

public class DispenseController {
    private final PrescriptionRepository presRepo;
    private final StockRepository stockRepo;
    private final BatchLotRepository lotRepo;
    private final PrescriptionItemRepository itemRepo;
    private final DispenseRecordRepository recordRepo;

    public DispenseController(PrescriptionRepository p, StockRepository s, BatchLotRepository b,
            PrescriptionItemRepository i, DispenseRecordRepository r) {
        this.presRepo = p;
        this.stockRepo = s;
        this.lotRepo = b;
        this.itemRepo = i;
        this.recordRepo = r;
    }

    public void processDispense(String prescriptionId, String pharmacistId) {
        Prescription prescription = presRepo.findById(prescriptionId);
        if (prescription == null) {
            throw new IllegalArgumentException("Prescription not found: " + prescriptionId);
        }
        if ("DISPENSED".equals(prescription.getStatus())) {
            throw new IllegalStateException("Prescription " + prescriptionId + " is already dispensed");
        }

        List<PrescriptionItem> items = itemRepo.findByPrescriptionId(prescriptionId);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("No items found for prescription " + prescriptionId);
        }

        String branchId = prescription.getBranchId();
        for (PrescriptionItem item : items) {
            stockRepo.deductWithSync(branchId, item.getMedicineId(), item.getQuantity());
            BatchLot bestLot = lotRepo.findBestLot(item.getMedicineId(), branchId);
            if (bestLot == null) {
                throw new IllegalStateException("No valid batch lot for medicine " + item.getMedicineId()
                        + " in branch " + branchId);
            }
            lotRepo.consumeFromLot(bestLot.getBatchLotId(), item.getQuantity());
        }

        try {
            presRepo.markDispensed(prescriptionId);
        } catch (PrescriptionRepository.PrescriptionAlreadyDispensedException e) {
            throw new IllegalStateException("Prescription already dispensed: " + prescriptionId, e);
        }

        DispenseRecord record = new DispenseRecord(generateRecordId(), prescriptionId, pharmacistId,
                LocalDate.now(), branchId);
        recordRepo.save(record);
    }

    private String generateRecordId() {
        return "DR" + System.nanoTime();
    }
}
