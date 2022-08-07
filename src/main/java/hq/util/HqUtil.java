package hq.util;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class HqUtil {

	public URI getUriForPath(String path) {
		return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path(path).toUriString());
	}
	
}
