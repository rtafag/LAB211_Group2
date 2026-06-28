package controller;

import java.util.List;

import model.BatchLot;
import model.Prescription;
import model.Stock;
import repository.BatchLotRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;

public class ReportController {

    private final StockRepository stockRepo;
    private final PrescriptionRepository presRepo;
    private final BatchLotRepository lotRepo;

    public ReportController(StockRepository s, PrescriptionRepository p, BatchLotRepository b) {
        this.stockRepo = s;
        this.presRepo = p;
        this.lotRepo = b;
    }

    public void printReport() {
        System.out.println("===== STOCK REPORT =====");
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        stocks.forEach(s -> System.out.println(s.toCsvLine()));

        System.out.println("===== DISPENSED PRESCRIPTIONS =====");
        List<Prescription> pres = presRepo.readAll("data/prescriptions.csv");
        pres.stream()
                .filter(p -> "DISPENSED".equals(p.getStatus()))
                .forEach(p -> System.out.println(p.toCsvLine()));

        System.out.println("===== NEAR-EXPIRY BATCHES =====");
        List<BatchLot> lots = lotRepo.readAll("data/batch_lots.csv");
        lots.stream()
                .filter(l -> l.isNearExpiry())
                .forEach(l -> System.out.println("Warning: " + l.getBatchLotId()));
    }

    public void printNearExpiryWarning(String branchId) {
        System.out.println("===== NEAR-EXPIRY WARNING - " + branchId + " =====");
        List<BatchLot> lots = lotRepo.readAll("data/batch_lots.csv");
        List<BatchLot> nearExpiryLots = lots.stream()
                .filter(l -> branchId.equals(l.getBranchId()))
                .filter(BatchLot::isNearExpiry)
                .toList();

        if (nearExpiryLots.isEmpty()) {
            System.out.println("No near-expiry batch lots found for branch " + branchId + ".");
            return;
        }

        nearExpiryLots.forEach(l -> System.out.println(
                "Batch: " + l.getBatchLotId()
                + " | Branch: " + l.getBranchId()
                + " | Medicine: " + l.getMedicineId()
                + " | Qty: " + l.getQuantity()
                + " | Expiry: " + l.getExpiryDate()));
    }

    public void printSaleReport(String branchId) {
        System.out.println("===== SALE REPORT - " + branchId + " =====");
        List<Prescription> prescriptions = presRepo.readAll("data/prescriptions.csv");
        List<Prescription> dispensed = prescriptions.stream()
                .filter(p -> branchId.equals(p.getBranchId()))
                .filter(p -> "DISPENSED".equals(p.getStatus()))
                .toList();

        System.out.println("Total dispensed prescriptions in " + branchId + ": " + dispensed.size());
        dispensed.forEach(p -> System.out.println(
                "Prescription: " + p.getPrescriptionId()
                + " | Branch: " + p.getBranchId()
                + " | Created: " + p.getCreatedDate()));
    }
}
