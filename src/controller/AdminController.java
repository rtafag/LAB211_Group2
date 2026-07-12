package controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.Branch;
import model.DispenseRecord;
import model.Medicine;
import model.Pharmacist;
import model.PharmacistRole;
import model.PrescriptionItem;
import repository.BranchRepository;
import repository.DispenseRecordRepository;
import repository.MedicineRepository;
import repository.PharmacistRepository;
import repository.PrescriptionItemRepository;
import repository.PrescriptionRepository;

public class AdminController {

    private final PharmacistRepository pharmacistRepo;
    private final BranchRepository branchRepo;
    private final PrescriptionRepository presRepo;
    private final PrescriptionItemRepository presItemRepo;
    private final MedicineRepository medicineRepo;
    private final DispenseRecordRepository dispenseRepo;

    public AdminController() {
        this.pharmacistRepo = new PharmacistRepository();
        this.branchRepo = new BranchRepository();
        this.presRepo = new PrescriptionRepository();
        this.presItemRepo = new PrescriptionItemRepository();
        this.medicineRepo = new MedicineRepository();
        this.dispenseRepo = new DispenseRecordRepository();
    }

    // ===== STAFF MANAGEMENT =====
    public List<Pharmacist> getAllPharmacists() {
        return pharmacistRepo.findAll();
    }

    public Pharmacist getPharmacistById(String id) {
        return pharmacistRepo.findAll().stream()
                .filter(p -> p.getPharmacistId().equals(id))
                .findFirst().orElse(null);
    }

    public Pharmacist addPharmacist(String name, String branchId, String phoneNumber, String password, String role) {
        if (branchRepo.findById(branchId) == null) {
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        PharmacistRole pharmacistRole;
        try {
            pharmacistRole = PharmacistRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Use STAFF or ADMIN.");
        }
        String id = pharmacistRepo.generateNextId();
        Pharmacist p = new Pharmacist(id, name, branchId, phoneNumber, password, pharmacistRole);
        pharmacistRepo.save(p);
        return p;
    }

    public Pharmacist editPharmacist(String id, String newName, String newBranchId, String newPhone, String newRole) {
        List<Pharmacist> all = pharmacistRepo.findAll();
        Pharmacist current = all.stream().filter(p -> p.getPharmacistId().equals(id)).findFirst().orElse(null);
        if (current == null) {
            throw new IllegalArgumentException("Pharmacist not found: " + id);
        }
        if (newBranchId != null && !newBranchId.isBlank() && branchRepo.findById(newBranchId) == null) {
            throw new IllegalArgumentException("Branch not found: " + newBranchId);
        }
        String name = (newName == null || newName.isBlank()) ? current.getName() : newName;
        String branchId = (newBranchId == null || newBranchId.isBlank()) ? current.getBranchId() : newBranchId;
        String phone = (newPhone == null || newPhone.isBlank()) ? current.getUsername() : newPhone;
        PharmacistRole role = current.getRole();
        if (newRole != null && !newRole.isBlank()) {
            try {
                role = PharmacistRole.valueOf(newRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role. Use STAFF or ADMIN.");
            }
        }
        Pharmacist updated = new Pharmacist(id, name, branchId, phone, current.getPasswordHash(), role);
        pharmacistRepo.update(updated);
        return updated;
    }

    public void deletePharmacist(String id) {
        Pharmacist p = pharmacistRepo.findAll().stream()
                .filter(ph -> ph.getPharmacistId().equals(id)).findFirst().orElse(null);
        if (p == null) {
            throw new IllegalArgumentException("Pharmacist not found: " + id);
        }
        pharmacistRepo.delete(id);
    }

    // ===== BRANCH MANAGEMENT =====
    public List<Branch> getAllBranches() {
        return branchRepo.findAll();
    }

    public Branch getBranchById(String id) {
        return branchRepo.findById(id);
    }

    public Branch addBranch(String name, String address, String phone) {
        String id = branchRepo.generateNextId();
        Branch b = new Branch(id, name, address, phone);
        branchRepo.save(b);
        return b;
    }

    public Branch editBranch(String id, String newName, String newAddress, String newPhone) {
        Branch current = branchRepo.findById(id);
        if (current == null) {
            throw new IllegalArgumentException("Branch not found: " + id);
        }
        String name = (newName == null || newName.isBlank()) ? current.getBranchName() : newName;
        String address = (newAddress == null || newAddress.isBlank()) ? current.getAddress() : newAddress;
        String phone = (newPhone == null || newPhone.isBlank()) ? current.getPhone() : newPhone;
        Branch updated = new Branch(id, name, address, phone);
        branchRepo.update(updated);
        return updated;
    }

    public void deleteBranch(String id) {
        Branch branch = branchRepo.findById(id);
        if (branch == null) {
            throw new IllegalArgumentException("Branch not found: " + id);
        }
        boolean hasStaff = pharmacistRepo.findAll().stream()
                .anyMatch(p -> p.getBranchId().equals(id));
        if (hasStaff) {
            throw new IllegalStateException("Cannot delete branch " + id + " - it still has pharmacists assigned.");
        }
        branchRepo.delete(id);
    }

    // ===== REVENUE REPORTS =====
    public double getBranchRevenue(String branchId, boolean last30Days) {
        if (branchRepo.findById(branchId) == null) {
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        List<DispenseRecord> records = dispenseRepo.readAll("data/dispense_records.csv");
        LocalDate cutoff = last30Days ? LocalDate.now().minusDays(30) : null;

        Map<String, Double> medicinePriceCache = buildPriceCache();

        double total = 0;
        for (DispenseRecord dr : records) {
            if (!branchId.equals(dr.getBranchId())) {
                continue;
            }
            if (cutoff != null && dr.getDispensedTime().isBefore(cutoff)) {
                continue;
            }
            total += calcPrescriptionTotal(dr.getPrescriptionId(), medicinePriceCache);
        }
        return total;
    }

    public Map<String, Double> getAllBranchRevenues(boolean last30Days) {
        List<DispenseRecord> records = dispenseRepo.readAll("data/dispense_records.csv");
        LocalDate cutoff = last30Days ? LocalDate.now().minusDays(30) : null;
        Map<String, Double> medicinePriceCache = buildPriceCache();

        Map<String, Double> result = new java.util.LinkedHashMap<>();
        for (Branch b : branchRepo.findAll()) {
            result.put(b.getBranchId(), 0.0);
        }

        for (DispenseRecord dr : records) {
            if (cutoff != null && dr.getDispensedTime().isBefore(cutoff)) {
                continue;
            }
            String bid = dr.getBranchId();
            double amt = calcPrescriptionTotal(dr.getPrescriptionId(), medicinePriceCache);
            result.merge(bid, amt, Double::sum);
        }
        return result;
    }

    private double calcPrescriptionTotal(String prescriptionId, Map<String, Double> priceCache) {
        List<PrescriptionItem> items = presItemRepo.findByPrescriptionId(prescriptionId);
        double total = 0;
        for (PrescriptionItem item : items) {
            Double price = priceCache.get(item.getMedicineId());
            if (price != null) {
                total += price * item.getQuantity();
            }
        }
        return total;
    }

    private Map<String, Double> buildPriceCache() {
        return medicineRepo.findAll().stream()
                .collect(Collectors.toMap(Medicine::getMedicineId, Medicine::getPrice, (a, b) -> a));
    }
}
