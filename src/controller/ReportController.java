package controller;

import java.util.List;
import model.*;
import repository.*;

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
}
