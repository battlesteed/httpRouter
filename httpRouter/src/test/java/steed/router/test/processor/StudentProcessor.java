package steed.router.test.processor;

import java.util.HashMap;

import steed.router.annotation.Path;
import steed.router.annotation.Power;
import steed.router.processor.BaseProcessor;

@Path("/student")
@Power("测试类权限")
public class StudentProcessor extends BaseProcessor{
	
	@Power("测试权限1")
	public void ad() {
		
	}
	
	public Object index() {
		return new HashMap<>();
	}
	
	private void ad2() {
		
	}
	
	@Power("测试权限2")
	public String ad3() {
		return steed_forward;
	}
	
	public long ad5() {
		return 0;
	}
	public String ad4() {
		return "/WEB-INF/jsp/index.jsp";
	}
}
