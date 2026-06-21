package controller;

import repository.*;
import model.*;
import java.util.List;

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
        System.out.println("===== BÁO CÁO TỒN KHO =====");
        List<Stock> stocks = stockRepo.readAll("data/stocks.csv");
        stocks.forEach(s -> System.out.println(s.toCsvLine()));

        System.out.println("===== ĐƠN THUỐC ĐÃ XUẤT =====");
        List<Prescription> pres = presRepo.readAll("data/prescriptions.csv");
        pres.stream()
            .filter(p -> "DISPENSED".equals(p.getStatus()))
            .forEach(p -> System.out.println(p.toCsvLine()));

        System.out.println("===== LÔ THUỐC SẮP HẾT HẠN =====");
        List<BatchLot> lots = lotRepo.readAll("data/batch_lots.csv");
        lots.stream()
            .filter(l -> l.isNearExpiry())
            .forEach(l -> System.out.println("Cảnh báo: " + l.getBatchLotId()));
    }
}
