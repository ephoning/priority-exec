package org.eddie.exec;

import java.util.concurrent.CancellationException;

public class TaskException extends Exception {
	
	public static final long serialVersionUID = TaskException.class.hashCode();
	
	public TaskException() {
	}
	
	public TaskException(String message) {
		super(message);
	}

	public TaskException(String message, Throwable cause) {
		super(message, cause);
	}

	public String toString() {
		String message = null;
		Throwable cause = getCause();
		if (cause != null) {
			String causeMessage = null;
			if(cause instanceof CancellationException) {
				causeMessage = "task cancellation";
			}
			else {
				causeMessage = cause.getMessage();
			}
			message = String.format("%s (caused by: %s)", getMessage(), causeMessage);
		}
		else {
			message = getMessage();
		}
		return message;
	}
}
