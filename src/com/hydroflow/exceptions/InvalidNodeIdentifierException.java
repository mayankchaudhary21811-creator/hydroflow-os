package com.hydroflow.exceptions;

/**
 * Unchecked exception thrown when asset identifiers fail alphanumeric regex validation.
 */
public class InvalidNodeIdentifierException extends IllegalArgumentException {
    private final String invalidIdentifier;

    public InvalidNodeIdentifierException(String invalidIdentifier) {
        super("Invalid water network asset identifier format: " + invalidIdentifier +
              " (Must match pattern: ^[A-Z0-9]{6,16}$)");
        this.invalidIdentifier = invalidIdentifier;
    }

    public String getInvalidIdentifier() {
        return invalidIdentifier;
    }
}
