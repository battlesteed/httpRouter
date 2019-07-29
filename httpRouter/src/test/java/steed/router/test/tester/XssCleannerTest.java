package steed.router.test.tester;

import static org.junit.Assert.assertEquals;

import org.jsoup.safety.Whitelist;
import org.junit.Test;

import steed.router.SimpleXSSCleanner;

public class XssCleannerTest {
	
	@Test
	public void testClean() {
		SimpleXSSCleanner simpleXSSCleanner = new SimpleXSSCleanner() {

			@Override
			public void addWhitelist(Whitelist relaxed) {
				super.addWhitelist(relaxed);
				relaxed.addAttributes(":all", "href");
			}
			
		};
		simpleXSSCleanner.setAllowedSpecialCharParams(new String[] {"foo"});
		simpleXSSCleanner.setBaseUri("http://foo.com");
		String[] clean = simpleXSSCleanner.clean(new String[] {"<a target=\"_blank\" href=\"http://foo.com/home/user/home?nickName=admin\">admin</a> 22222"}, "foo");
		assertEquals("<a href=\"http://foo.com/home/user/home?nickName=admin\">admin</a> 22222", clean[0]);
	}
}
