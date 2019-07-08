package steed.router.test.tester;


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

/**
 * 路由测试,主要测试转发规则,访问权限等
 * @author battlesteed
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Configuration
//@WebAppConfiguration
public class RouteTest {
	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void testRoute() {
		
		testNow("/"+new Random().nextInt(),HttpStatus.NOT_FOUND);
		
		testNow("/test/"+new Random().nextInt(),HttpStatus.NOT_FOUND);
		
		testNow("/test/ad2", HttpStatus.NOT_FOUND);
		
		testNow("/test/ad2.do", HttpStatus.NOT_FOUND);
		
		testNow("/test/getModel", HttpStatus.NOT_FOUND);
		
		testNow("/test/testDontAccess", HttpStatus.NOT_FOUND);
		
		testNow("/test/testStaticMethod", HttpStatus.NOT_FOUND);
		
		testNow("/test/testStatic", HttpStatus.NOT_FOUND);
		
		testNow("/testDontAccess/a1214232refdsf", HttpStatus.NOT_FOUND);
	}
	
	private void testNow(String url, HttpStatus status) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Void>(headers),String.class);
		assertThat(entity.getStatusCode()).isEqualTo(status);
	}
}
