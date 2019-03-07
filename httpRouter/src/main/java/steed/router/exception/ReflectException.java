package steed.router.exception;

public class ReflectException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ReflectException() {
		super();
	}

	public ReflectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ReflectException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectException(String message) {
		super(message);
	}

	public ReflectException(Throwable cause) {
		super(cause);
	}

}
