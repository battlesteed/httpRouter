package steed.router.test;


import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.junit.Assert;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import steed.util.base.StringUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@Configuration
//@WebAppConfiguration
public class ParamterFillerTest {
	@Autowired
	private TestRestTemplate client;

	@Test
	public void testBaseParam() {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("param1", new Random().nextInt(10000)+"");
		params.add("param2", "true");
		params.add("param3", new Random().nextLong()+"");
		
		String httpRestClient = httpRestClient("/test/baseTypeParamTest", HttpMethod.POST, params);
		Map<String, String> fromJson = new Gson().fromJson(httpRestClient, new TypeToken<Map<String, String>>(){}.getType());
		for (Entry<String, String> e:fromJson.entrySet()) {
			Assert.assertTrue(e.getValue().equals(params.getFirst(e.getKey())));
		}
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
