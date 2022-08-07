package hq.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {

		AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		ObjectMapper mapper = new ObjectMapper();

		try {
			authenticationRequest = mapper.readValue(request.getInputStream(), AuthenticationRequest.class);
		} catch (IOException e) {
			try {
				response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				mapper.writeValue(response.getOutputStream(),
						Map.of("error", "Invalid AuthenticationRequest JSON structure"));
			} catch (IOException e1) {}
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				authenticationRequest.getUsername(), authenticationRequest.getPassword());
		return authenticationManager.authenticate(authToken);

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authentication) throws IOException, ServletException {

		UserDetails user = (UserDetails) authentication.getPrincipal();
		String accessToken = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);

		response.setStatus(HttpStatus.OK.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		new ObjectMapper().writeValue(response.getOutputStream(),
				Map.of("accessToken", accessToken, "refreshToken", refreshToken));

	}
}

@Data
class AuthenticationRequest {
	private String username;
	private String password;
}
