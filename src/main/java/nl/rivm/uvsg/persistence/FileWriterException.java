package nl.rivm.uvsg.persistence;

// TODO MMo - Do we really need this class?
public class FileWriterException extends Exception {

    FileWriterException(Exception cause) {
        super(cause);
    }

    FileWriterException(String message, Exception cause) {
        super(message, cause);
    }
}
