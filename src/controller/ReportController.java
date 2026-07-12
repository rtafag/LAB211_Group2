package controller;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import model.BatchLot;
import model.Prescription;
import model.PrescriptionItem;
import model.Stock;
import repository.BatchLotRepository;
import repository.MedicineRepository;
import repository.PrescriptionItemRepository;
import repository.PrescriptionRepository;
import repository.StockRepository;

public class ReportController {

    private static final NumberFormat MONEY_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    static {
        MONEY_FORMAT.setMaximumFractionDigits(0);
        MONEY_FORMAT.setMinimumFractionDigits(0);
    }

    private final StockRepository stockRepo;
    private final PrescriptionRepository presRepo;
    private final BatchLotRepository lotRepo;
    private final PrescriptionItemRepository presItemRepo;
    private final MedicineRepository medicineRepo;

    public ReportController(StockRepository s, PrescriptionRepository p, BatchLotRepository b) {
        this(s, p, b, new PrescriptionItemRepository(), new MedicineRepository());
    }

    public ReportController(StockRepository s, PrescriptionRepository p, BatchLotRepository b,
            PrescriptionItemRepository pi, MedicineRepository m) {
        this.stockRepo = s;
        this.presRepo = p;
        this.lotRepo = b;
        this.presItemRepo = pi;
        this.medicineRepo = m;
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

        if (dispensed.isEmpty()) {
            System.out.println("No dispensed prescriptions found for branch " + branchId + ".");
            return;
        }

        System.out.println("Total dispensed prescriptions: " + dispensed.size());
        System.out.println("--- PRESCRIPTION DETAILS ---");

        double totalRevenue = 0;
        for (Prescription p : dispensed) {
            List<PrescriptionItem> items = presItemRepo.findByPrescriptionId(p.getPrescriptionId());
            double prescriptionTotal = 0;

            for (PrescriptionItem item : items) {
                String medicineId = item.getMedicineId();
                int quantity = item.getQuantity();

                List<model.Medicine> medicines = medicineRepo.findAll();
                model.Medicine medicine = medicines.stream()
                        .filter(m -> m.getMedicineId().equals(medicineId))
                        .findFirst()
                        .orElse(null);

                if (medicine != null) {
                    double lineTotal = quantity * medicine.getPrice();
                    prescriptionTotal += lineTotal;
                    System.out.println("  Medicine: " + medicine.getMedicineName()
                            + " | Qty: " + quantity
                            + " | Price: " + formatMoney(medicine.getPrice())
                            + " | Line Total: " + formatMoney(lineTotal));
                }
            }

            totalRevenue += prescriptionTotal;
            System.out.println("Prescription " + p.getPrescriptionId()
                    + " | Total: " + formatMoney(prescriptionTotal));
            System.out.println();
        }

        System.out.println("===== TOTAL REVENUE: " + formatMoney(totalRevenue) + " =====");
    }

    private String formatMoney(double amount) {
        return MONEY_FORMAT.format(amount);
    }
}
