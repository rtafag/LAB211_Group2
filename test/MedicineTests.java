import model.Medicine;

public class MedicineTests {
    public static void main(String[] args) {
        Medicine medicine = Medicine.fromCsvLine("M0001,Vitamin supplement,chai,12,For daily use,ABC Pharma");

        if (!"chai".equals(medicine.getUnit())) {
            throw new AssertionError("Expected chai unit");
        }
        if (medicine.getUnitsPerBox() != 12) {
            throw new AssertionError("Expected 12 units per box");
        }

        Medicine roundTrip = Medicine.fromCsvLine(medicine.toCsvLine());
        if (!"chai".equals(roundTrip.getUnit()) || roundTrip.getUnitsPerBox() != 12) {
            throw new AssertionError("Round-trip medicine parsing failed");
        }

        System.out.println("MEDICINE TESTS PASSED");
    }
}
