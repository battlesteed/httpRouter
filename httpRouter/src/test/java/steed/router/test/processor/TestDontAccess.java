package steed.router.test.processor;

import steed.router.annotation.DontAccess;
import steed.router.annotation.Path;
import steed.router.processor.ModelDrivenProcessor;
import steed.router.test.domain.Student;

@DontAccess
@Path("/testDontAccess")
public class TestDontAccess extends ModelDrivenProcessor<Student>{
	public void ad() {
		
	}
	
}
