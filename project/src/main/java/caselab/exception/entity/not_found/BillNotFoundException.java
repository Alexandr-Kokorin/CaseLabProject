package caselab.exception.entity.not_found;

public final class BillNotFoundException extends EntityNotFoundException {
    public BillNotFoundException(Long id) {
        super("bill.not_found", id);
    }
}
