package steed.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ParameterFiller {
	/**
     *  把http请求中的参数填充到Processor
	 * @throws Exception 
     *    
     */
	  void fillParamters2ProcessorData(Object container,HttpServletRequest request,HttpServletResponse response) throws Exception ;
}
