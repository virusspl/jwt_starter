package hq.service.user;

public class UsernameValidator {

	public static final String VALID_USERNAME_SYNTAX_MESSAGE = "Username containing 3 to 20 characters of: a-Z, 0-9 and _ allowed";
	
	public static boolean isValid(String username) {
		return username.matches("^[a-zA-Z][a-zA-Z0-9_]{2,20}$");
	}
	
}
