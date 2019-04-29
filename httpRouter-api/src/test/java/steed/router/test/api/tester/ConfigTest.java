package steed.router.test.api.tester;


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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import steed.router.api.ApiTester;

/**
 * 路由测试,主要测试转发规则,访问权限等
 * @author battlesteed
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Configuration
//@WebAppConfiguration
public class ConfigTest extends ApiTester{
	@Autowired
	private TestRestTemplate client;

	@Test
	public void noConfigApi() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		
		String httpRestClient = httpRestClient("/student/noConfigApi", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("address", "address");
		params.add("name", new Random().nextInt(10000)+"");
		httpRestClient = httpRestClient("/student/noConfigApi", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("id", new Random().nextLong()+"");
		httpRestClient = httpRestClient("/student/noConfigApi", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("sign", "33232");
		httpRestClient = httpRestClient("/student/noConfigApi", HttpMethod.POST, params);
		assertFail(httpRestClient);
	}
	
	@Test
	public void save() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		
		String httpRestClient = httpRestClient("/student/save", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("address", "address");
		params.add("name", new Random().nextInt(10000)+"");
		httpRestClient = httpRestClient("/student/save", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("id", new Random().nextLong()+"");
		httpRestClient = httpRestClient("/student/save", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("sign", "33232");
		httpRestClient = httpRestClient("/student/save", HttpMethod.POST, params);
		assertSuccess(httpRestClient);
		
		params.add("number", "3323a2");
		httpRestClient = httpRestClient("/student/save", HttpMethod.POST, params);
		assertFail(httpRestClient);
	}
	
	@Test
	public void delete() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		
		String httpRestClient = httpRestClient("/student/delete", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("address", "address");
		params.add("name", new Random().nextInt(10000)+"");
		httpRestClient = httpRestClient("/student/delete", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("id", new Random().nextLong()+"");
		httpRestClient = httpRestClient("/student/delete", HttpMethod.POST, params);
		assertFail(httpRestClient);
		
		params.add("sign", "33232");
		httpRestClient = httpRestClient("/student/delete", HttpMethod.POST, params);
		assertSuccess(httpRestClient);
	}
	
	public String httpRestClient(String url, HttpMethod method, MultiValueMap<String, String> params) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        HttpHeaders headers = new HttpHeaders();
        //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        //  执行HTTP请求
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }

}
