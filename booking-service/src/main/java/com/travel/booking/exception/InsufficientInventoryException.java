package com.travel.booking.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String message) {
        super(message);
    }

    public InsufficientInventoryException(String itemType, Long itemId) {
        super(String.format("Insufficient inventory for itemType: '%s', itemId: %d", itemType, itemId));
    }
}
