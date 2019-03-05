package steed.router.exception;

public class RouterException extends RuntimeException{

	public RouterException() {
		super();
	}

	public RouterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RouterException(String message, Throwable cause) {
		super(message, cause);
	}

	public RouterException(String message) {
		super(message);
	}

	public RouterException(Throwable cause) {
		super(cause);
	}

}
