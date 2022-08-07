package hq.security;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// if trying to login - skip filter
		if (request.getServletPath().equals("/auth/login") || request.getServletPath().equals("/auth/refresh")) {
			filterChain.doFilter(request, response);
			return;
		}
		// else - try to authenticate with token
		try {
			String accessToken = jwtUtil.extractTokenFromHttpRequest(request);
			UsernamePasswordAuthenticationToken authToken = jwtUtil
					.getAuthenticationTokenFromJwtAccessToken(accessToken);
			SecurityContextHolder.getContext().setAuthentication(authToken);
			filterChain.doFilter(request, response);
		} catch (Exception ex) {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			new ObjectMapper().writeValue(response.getOutputStream(), Map.of("error", ex.getMessage()));
		}

	}

}
