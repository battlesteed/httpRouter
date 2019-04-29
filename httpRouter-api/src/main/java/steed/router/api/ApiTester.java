package steed.router.api;

import org.junit.Assert;

public class ApiTester {
	protected void assertFail(String json) {
		Assert.assertTrue(!json.contains("\"statusCode\":0"));
	}
	protected void assertSuccess(String json) {
		assertStatusCode(json, 0);
	}
	
	protected void assertStatusCode(String json,int statusCode) {
		Assert.assertTrue(json+"状态码不为"+statusCode+"!", json.contains("\"statusCode\":"+statusCode));
	}
}
