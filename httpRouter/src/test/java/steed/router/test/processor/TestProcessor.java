package steed.router.test.processor;

import java.util.HashMap;
import java.util.Map;

import steed.router.ModelDriven;
import steed.router.annotation.Path;
import steed.router.annotation.Power;
import steed.router.processor.BaseProcessor;

@Path("/test")
@Power("测试类权限")
public class TestProcessor extends BaseProcessor implements ModelDriven<Student>{
	public int param1;
	public boolean param2;
	public String param3;
	
	@Power("测试权限1")
	public void ad() {
		
	}
	
	public Map<String, Object> baseTypeParamTest(){
		HashMap<String, Object> hashMap = new HashMap<>();
		hashMap.put("param1", param1+"");
		hashMap.put("param2", param2+"");
		hashMap.put("param3", param3+"");
		return hashMap;
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

	@Override
	public Student getModel() {
		return null;
	}
}
