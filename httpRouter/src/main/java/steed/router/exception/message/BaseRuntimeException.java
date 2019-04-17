package steed.router.exception.message;

public class BaseRuntimeException extends RuntimeException{
	private static final long serialVersionUID = 1210513373984889853L;

	public BaseRuntimeException() {
		super();
	}

	public BaseRuntimeException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BaseRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BaseRuntimeException(String message) {
		super(message);
	}

	public BaseRuntimeException(Throwable cause) {
		super(cause);
	}
	
}
