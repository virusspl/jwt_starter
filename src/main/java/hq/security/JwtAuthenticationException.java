package hq.security;

public class JwtAuthenticationException extends Exception{

	private static final long serialVersionUID = 6477530674017355611L;

	public JwtAuthenticationException(String message) {
		super(message);
	}
	
}
