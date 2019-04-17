package steed.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SimpleHttpRouter extends HttpRouter{
	
	//TODO 非spring ProcessorScanner
	public SimpleHttpRouter(ProcessorScanner processorScanner) {
		super(processorScanner);
	}

	@Override
    protected boolean checkPower(HttpServletRequest request,HttpServletResponse response,String uri,String power) {
    	return true;
    }

}
