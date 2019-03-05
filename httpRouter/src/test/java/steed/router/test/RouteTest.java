package steed.router.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Configuration
//@WebAppConfiguration
public class RouteTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void test404() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		ResponseEntity<String> entity = this.restTemplate.exchange("/"+new Random().nextInt(), HttpMethod.GET, new HttpEntity<Void>(headers),
				String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		entity = this.restTemplate.exchange("/test/"+new Random().nextInt(), HttpMethod.GET, new HttpEntity<Void>(headers),
				String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		entity = this.restTemplate.exchange("/test/ad2", HttpMethod.GET, new HttpEntity<Void>(headers),
				String.class);
		assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
