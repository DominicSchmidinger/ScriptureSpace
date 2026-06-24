package spengergasse.at.util;

/**
 * Eigene Exception für ungültige Eingaben in Scripture Space.
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException() {
        super("Ungültige Eingabe");
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
