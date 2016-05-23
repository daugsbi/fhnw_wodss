package ch.fhnw.wodss;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableEncryptableProperties
public class WodssServer extends SpringBootServletInitializer {
	
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(WodssServer.class);

	public static void main(String[] args) {
		LOG.info("Staring WODSS Server...");
		SpringApplication.run(WodssServer.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(WodssServer.class);
	}

	/**
	 * Enables multipart/form-data over PUT
	 * 
	 * @return the multipart resolver.
	 */
	@Bean
	public MultipartResolver multipartResolver() {
		return new StandardServletMultipartResolver() {
			@Override
			public boolean isMultipart(HttpServletRequest request) {
				String method = request.getMethod().toLowerCase();
				// By default, only POST is allowed. Since this is an 'update'
				// we should accept PUT.
				if (!Arrays.asList("put", "post").contains(method)) {
					return false;
				}
				String contentType = request.getContentType();
				return (contentType != null && contentType.toLowerCase().startsWith("multipart/"));
			}
		};
	}
}
