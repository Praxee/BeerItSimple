package exception;

public class LoginException extends BISException {
    private final String typeError = "Connection Error";
    private final String message = "Login and password doesn't match.";

    public String getTypeError() {
        return typeError;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
