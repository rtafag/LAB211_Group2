package repository;

public class PrescriptionAlreadyDispensedException extends Exception {
    public PrescriptionAlreadyDispensedException(String message) {
        super(message);
    }
}
