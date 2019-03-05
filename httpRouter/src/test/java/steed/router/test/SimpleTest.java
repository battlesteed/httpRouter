package steed.router.test;

import org.junit.Test;

import steed.util.logging.LoggerFactory;

public class SimpleTest {
	private int i = 0;
	private ThreadLocal<Integer> threadLocal = new ThreadLocal<>();	
//	@Test
	public void testLogBack() {
		LoggerFactory.getLogger().warn("gfsdgfdsg");
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
