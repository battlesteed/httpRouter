package steed.router.test.api.processor;

import org.junit.Assert;

import steed.router.annotation.Path;
import steed.router.test.domain.Student;

@Path("student")
public class StudentProcessor extends BaseApiTestProcessor<Student>{
	
	public void save() {
		assertNotEmpty(domain.getId());
		assertNotEmpty(domain.getName());
		Assert.assertNull(domain.getAddress());
	}
	
	public void noConfigApi() {
		Assert.fail("没有在json配置的api不允许访问!");
	}
	
	public void delete() {
		Assert.assertNull(domain.getAddress());
		Assert.assertNull(domain.getName());
		assertNotEmpty(domain.getId());
	}
	
}
