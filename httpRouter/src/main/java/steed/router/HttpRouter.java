package steed.router;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.HibernateUtil;
import steed.router.annotation.DontAccess;
import steed.router.annotation.Power;
import steed.router.domain.Message;
import steed.router.exception.message.MessageExceptionInterface;
import steed.router.processor.BaseProcessor;


/** 
 *  http路由器,建议用单例模式,全局保存一个HttpRouter
 * @author 战马 battle_steed@qq.com
 * 
 */  
public abstract class HttpRouter{
	
	protected ParameterFiller paramterFiller = new SimpleParamterFiller();
    private Map<String, Class<? extends BaseProcessor>> pathProcessor = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(HttpRouter.class);
    
    private static final ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<HttpServletResponse>();
    
	public static HttpServletRequest getRequest(){
		return requestThreadLocal.get();
	}
	public static HttpServletResponse getResponse(){
		return responseThreadLocal.get();
	}
	
    public ParameterFiller getParamterFiller() {
		return paramterFiller;
	}
	public void setParamterFiller(ParameterFiller paramterFiller) {
		this.paramterFiller = paramterFiller;
	}
	public Map<String, Class<? extends BaseProcessor>> getPathProcessor() {
		return pathProcessor;
	}
    
    public HttpRouter(ProcessorScanner processorScanner) {
    	super();
    	pathProcessor = processorScanner.scanProcessor();
    	Config.autoBeginTransaction = true;
    	Config.autoCommitTransaction = false;
    }
   
  
    /**
     * 	权限检测,可以根据具体业务根据uri或power判断用户是否有权限访问该url,可以在这里通过response返回没有权限信息或者抛MessageRuntimException
     * @param uri uri
     * @param power Processor上面的power注解,若没有注解,则该参数为null
     * @return 是否有权限访问该uri
     */
    protected abstract boolean checkPower(HttpServletRequest request,HttpServletResponse response,String uri,String power);
    
    /**
     * java对象转json,用于给客户端返回json数据
     * @param object 要转换的json对象
     * @return json字符串
     */
    protected String object2Json(Object object) {
    	return RouterConfig.defaultJsonSerializer.object2Json(object);
    }

	/**
	 * 把对象以json形式写到response的输出流，一般用于ajax
	 * @param obj 要输出的对象
	 */
	protected void writeJsonMessage(Object obj,HttpServletRequest request,HttpServletResponse response){
		if (obj == null) {
			return;
		}
		response.setHeader("Content-Type", "application/json");
		String json = object2Json(obj);
		try {
			writeString(json, request, response);
		} catch (IOException e) {
			logger.error("返回json给客户端出错!",e);
		}
	}
	
	/**
	 * 把string写到response的输出流
	 * @param string
	 * @throws IOException 
	 */
	protected void writeString(String string, HttpServletRequest request, HttpServletResponse response) throws IOException{
		ServletOutputStream out = response.getOutputStream();
		out.write(string.getBytes(RouterConfig.charset));
		out.flush();
		logger.debug("返回给客户端内容----->"+string);
	}
    
    protected boolean shouldReturnJsonMessage(Exception e,HttpServletRequest request, HttpServletResponse response) {
    	return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
    
    protected void onException(Exception e,HttpServletRequest request, HttpServletResponse response) {
    	Message message;
    	if (e instanceof MessageExceptionInterface) {
			message = ((MessageExceptionInterface)e).getMsg();
			if (RouterConfig.devMode) {
				logger.debug("抛出异常提示:",e);
			}
		}else {
			message = new Message(Message.statusCode_UnknownError, RouterConfig.defaultErrorMessage);
			if (RouterConfig.devMode && !StringUtil.isStringEmpty(e.getMessage())) {
				//开发模式直接提示异常信息
				message.setMessage(e.getMessage());
			}
			logger.error("发生未知错误!",e);
		}
//    	if (StringUtil.isStringEmpty(message.getMessage())) {
//			message.setMessage(RouterConfig.defaultErrorMessage);
//		}
    	if (shouldReturnJsonMessage(e, request, response)) {
			writeJsonMessage(message, request, response);
		}else {
			try {
				request.setAttribute(RouterConfig.exceptionAttributeKey, e);
				request.setAttribute(RouterConfig.messageAttributeKey, RouterConfig.messageAttributeKey);
				request.getRequestDispatcher(mergePath(RouterConfig.baseJspPath, RouterConfig.message_page)).forward(request, response);
			} catch (ServletException | IOException e1) {
				logger.error("跳转消息提示页出错!",e);
			}
		}
    	//writeJsonMessage(obj, response);
    }
  
    private void printParam(ServletRequest req) {
		List<String> keyList = Collections.list(req.getParameterNames());
		if (!keyList.isEmpty()) {
			logger.debug("-----------参数-----------");
			StringBuffer buffer = new StringBuffer();
			for (String s : keyList) {
				String[] parameterValues = req.getParameterValues(s);
				buffer.append(s).append( "----->");
				for(String str:parameterValues){
					buffer.append(str).append("   ");
				}
			}
			logger.debug(buffer.toString());
			logger.debug("-----------参数-----------");
		}
	}
    
    public void forward(HttpServletRequest request, HttpServletResponse response){  
    	try {
    		request = new XSSCleanRequestWrapper(request);
    		requestThreadLocal.set(request);
    		responseThreadLocal.set(response);
    		if (RouterConfig.devMode) {
				printParam(request);
				logger.debug("请求url--->%s", request.getRequestURL());
			}
			try {
				forwardNow(request, response);
			} catch (IOException | ServletException e) {
				logger.error("httpRouter分发请求出错!",e);
			}
			if (DaoUtil.getTransactionType() != null) {
				DaoUtil.managTransaction();
			}
		}catch (Exception e) {
			onException(e, request, response);
			DaoUtil.rollbackTransaction();
		}finally {
			requestThreadLocal.remove();
			responseThreadLocal.remove();
			HibernateUtil.release();
			DaoUtil.relese();
		}
    }
    
    private void forwardNow(HttpServletRequest request, HttpServletResponse response) throws Exception{  
		String requestURI = request.getRequestURI();
		String parentPath = getParentPath(requestURI);
		Class<? extends BaseProcessor> processor = getProcessor(response, parentPath);
		
		if (processor == null) {
			return;
		}
		
		String methodName = getMethodName(requestURI);
		
		try {
			Method method = processor.getMethod(methodName);
			if (method.getDeclaringClass() != processor) {
				logger.error("方法%s为父类方法,不允许通过http调用,若要重写,请在子类重写该方法!",method.toString());
				response.sendError(RouterConfig.FORBIDDEN_STATUS_CODE);
				return;
			}
			if (methodName.startsWith("set") || methodName.startsWith("get") 
					|| method.getAnnotation(DontAccess.class) != null
					|| Modifier.isStatic(method.getModifiers())) {
				logger.error("方法%s包含DontAccess注解或方法名以get,set开头或为静态方法,不能通过http访问!",method.toString());
				response.sendError(RouterConfig.FORBIDDEN_STATUS_CODE);
				return;
			}
			
			Power annotation = method.getAnnotation(Power.class);
			//只进行一次权限判断,方法有power则只判断方法的权限,类权限不判断
			if (annotation != null) {
				if (checkPower(request, response, requestURI,annotation.value())) {
					return;
				}
			}else if (!checkPower(request, response, requestURI, getPower(processor.getAnnotation(Power.class)))) {
				return;
			}
			
			try {
				BaseProcessor newInstance = newProcessor(processor);
				paramterFiller.fillParamters2ProcessorData(newInstance, request, response);
				
				newInstance.beforeAction(methodName);
				Object invoke = method.invoke(newInstance);
				newInstance.afterAction(methodName, invoke);
				
				if (invoke != null) {
					if (invoke instanceof String) {
						String jsp = (String) invoke;
						if (RouterConfig.steed_forward.equals(invoke)) {
							jsp = mergePath(RouterConfig.baseJspPath, parentPath+methodName+".jsp");
						}
						if (jsp.endsWith(".jsp")) {
							logger.debug("forward到%s",jsp);
							if (!jsp.startsWith("/")) {
								jsp = mergePath(RouterConfig.baseJspPath, parentPath + jsp);
							}
							request.getRequestDispatcher(jsp).forward(request, response);;
						}else {
							writeString(jsp, request, response);
						}
					}else {
						writeJsonMessage(invoke, request, response);
					}
				}
			} catch (Exception e) {
				if (e instanceof InvocationTargetException) {
					InvocationTargetException e1 = (InvocationTargetException) e;
					Throwable cause = e1.getCause();
					if (cause != null && cause instanceof Exception) {
						e = (Exception) cause;
					}
				}
				if (!(e instanceof MessageExceptionInterface)) {
					logger.warn(processor.getName()+"处理请求失败!",e);
				}
//				response.sendError(500);
				throw e;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			logger.warn("在%s中未找到public的 %s 方法!",processor.getName(),methodName);
			response.sendError(RouterConfig.NOT_FOUND_STATUS_CODE);
			return;
		}
    }
    
	protected BaseProcessor newProcessor(Class<? extends BaseProcessor> processor)
			throws InstantiationException, IllegalAccessException {
		/*if (context != null) {
			try {
				BaseProcessor bean = context.getBean(processor);
				if (bean != null) {
					return bean;
				}
			} catch (BeansException e) {
				if (RouterConfig.devMode) {
					logger.warn("从spring获取"+processor.getName()+"出错",e);
				}
			}
		}*/
		return processor.newInstance();
	}
	
	private Class<? extends BaseProcessor> getProcessor(HttpServletResponse response, String parentPath) throws IOException {
		Class<? extends BaseProcessor> processor = pathProcessor.get(parentPath);
		if (processor == null) {
			logger.warn("未找到path为 %s 的Processor!", parentPath);
			response.sendError(RouterConfig.NOT_FOUND_STATUS_CODE);
			return null;
		}
		if (processor.getAnnotation(DontAccess.class) != null) {
			logger.error("类%s包含DontAccess注解,不能通过http访问!",processor.getName());
			response.sendError(RouterConfig.FORBIDDEN_STATUS_CODE);
			return null;
		}
		return processor;
	}
	
	private String getParentPath(String requestURI) {
		return requestURI.substring(0,requestURI.lastIndexOf("/")+1);
	}
    
    /**
	 * 合并路径,防止出现双斜杠或者没有斜杠
	 * @return
	 */
	private static String mergePath(String path1,String path2){
		return mergePath(path1, path2, "/");
	}
	
	private static String mergePath(String path1,String path2,String separator){
		if (path2.startsWith(separator)&&path1.endsWith(separator)) {
			return path1 + path2.substring(1);
		}else if(!path2.startsWith(separator)&&!path1.endsWith(separator)){
			return path1 + separator + path2;
		}else if(path2.startsWith(separator)&&path1.endsWith(separator)){
			return path1.substring(0, path1.length()-1) + path2;
		}else {
			return path1 + path2;
		}
	}

	private String getPower(Power annotation) {
		if (annotation != null) {
			return annotation.value();
		}
		return null;
	}
    
	 public String getMethodName(String requestURI) {
		String method = requestURI.substring(requestURI.lastIndexOf("/")+1,requestURI.length());
    	if (method.contains(".")) {
			method = method.substring(0,method.indexOf("."));
		}
    	if ("".equals(method)) {
			return "index";
		}
		return method;
	}
	
}
