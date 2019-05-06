package steed.router.test.tester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.logging.LoggerFactory;
import steed.router.test.processor.TestDontAccess;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SimpleTest {
	
	@Autowired
	ApplicationContext context;
	
	private int i = 0;
	private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();	
	
	@Test
	public void test1() {
//		BaseUtil.out(context.containsBeanDefinition("TestDontAccess"));
//		SimpleTest existingBean = new SimpleTest();
//		context.getAutowireCapableBeanFactory().autowireBean(existingBean);
//		BaseUtil.out(existingBean.context == null);
	}
	
	/*@Test
	public void testThreadLocal() {
		for (int i = 0; i < 20; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					threadLocal.set(++SimpleTest.this.i);
				}
			}).start();
		}
		try {
			Thread.sleep(1000*5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BaseUtil.out(threadLocal.);
	}*/
}
