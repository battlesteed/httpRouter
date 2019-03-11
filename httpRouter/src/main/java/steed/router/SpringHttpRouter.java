package steed.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;

import steed.router.processor.BaseProcessor;

public class SpringHttpRouter extends HttpRouter{
	@Autowired
	private ApplicationContext context;
	
	private Gson gson = new Gson();
	
	public SpringHttpRouter(ProcessorScanner processorScanner) {
		super(processorScanner);
	}
    
    public SpringHttpRouter(String... packages4Scan) {
    	this(new SpringProcessorScanner(packages4Scan));
    }

	@Override
    protected boolean checkPower(HttpServletRequest request,HttpServletResponse response,String uri,String power) {
    	return true;
    }
	
	@Override
	protected BaseProcessor newProcessor(Class<? extends BaseProcessor> processor)
			throws InstantiationException, IllegalAccessException {
		BaseProcessor newProcessor = super.newProcessor(processor);
		if (context != null) {
			context.getAutowireCapableBeanFactory().autowireBean(newProcessor);
		}
		return newProcessor;
	}

	@Override
	protected String object2Json(Object object) {
		return gson.toJson(object);
	}
}

