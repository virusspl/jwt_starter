package hq.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JwtUtil {
	private final long ACCESS_TOKEN_LIFE = 180 * 60 * 1000;
	private final long REFRESH_TOKEN_LIFE = 360 * 60 * 1000;
	private final String JWT_SECRET = "asIwaswalkingupthestarisImeetamanwhowasnttherehewasntthereagaintodayiwishiwishhedgoaway!";
	private final String JWT_ISSUER = "Spring Boot Headquarters App";
	private Algorithm algorithm;
	private JWTVerifier verifier;

	public JwtUtil() {
		this.algorithm = Algorithm.HMAC256(JWT_SECRET.getBytes());
		this.verifier = JWT.require(algorithm).build();
	}

	public String generateAccessToken(UserDetails user) {
		return JWT.create().withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_LIFE)).withIssuer(JWT_ISSUER)
				.withClaim("roles",
						user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.sign(algorithm);
	}

	public String generateRefreshToken(UserDetails user) {
		return JWT.create().withSubject(user.getUsername())
				.withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_LIFE)).withIssuer(JWT_ISSUER)
				.sign(algorithm);
	}

	public String extractUsernameFromToken(String token) {
		DecodedJWT decodedJwt = verifier.verify(token);
		return decodedJwt.getSubject();
	}

	public String extractTokenFromHttpRequest(HttpServletRequest request) throws JwtAuthenticationException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header == null) {
			throw new JwtAuthenticationException("Authorization header is missing");
		} else if (!header.startsWith("Bearer ")) {
			throw new JwtAuthenticationException("Authorization header doesn't start with Bearer prefix");
		}

		return header.substring("Bearer ".length());

	}

	public UsernamePasswordAuthenticationToken getAuthenticationTokenFromJwtAccessToken(String token) {
		DecodedJWT decodedJwt = verifier.verify(token);
		String[] roles = decodedJwt.getClaim("roles").asArray(String.class);
		
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));

		return new UsernamePasswordAuthenticationToken(decodedJwt.getSubject(), null, authorities);

	}

}
