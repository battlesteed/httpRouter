package steed.router.test.api.tester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringRunner;

import steed.router.HttpRouter;
import steed.router.api.doc.SimpleDocumentGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class DocumentGeneratorTester {
	@Autowired
	private HttpRouter httpRouter;
	
	@Test
	public void test() {
//		System.out.println(getClass().getClassLoader().getResource("").getFile()+"");
		new SimpleDocumentGenerator().generate(httpRouter.getPathProcessor());
	}
}
