package hq.api;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.exceptions.TokenExpiredException;

import hq.domain.user.User;
import hq.security.JwtAuthenticationException;
import hq.security.JwtUtil;
import hq.service.user.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthResource {

	private final UserService userService;
	private final JwtUtil jwtUtil;

	@GetMapping("/refresh")
	public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String refreshToken = null;
		User user = null;
		
		try {
			refreshToken = jwtUtil.extractTokenFromHttpRequest(request);
			user = userService.findByUsername(jwtUtil.extractUsernameFromToken(refreshToken));
		} 
		catch (TokenExpiredException | JwtAuthenticationException ex) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
		}
		
		return ResponseEntity.ok(Map.of("accessToken", jwtUtil.generateAccessToken(user), "refreshToken", refreshToken));

	}
}
