package hq.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserServiceException extends ResponseStatusException{

	private static final long serialVersionUID = 3249952222525976432L;

	public UserServiceException(HttpStatus status, String message, Object ...args) {
		super(status, String.format(message, args));
	}

}
