package steed.router;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;

import steed.ext.util.base.PathUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectUtil;
import steed.hibernatemaster.Config;
import steed.hibernatemaster.util.DaoUtil;
import steed.router.annotation.DontAccess;
import steed.router.annotation.Power;
import steed.router.domain.Message;
import steed.router.exception.message.MessageExceptionInterface;
import steed.router.exception.message.MessageRuntimeException;
import steed.router.processor.BaseProcessor;
import steed.util.ext.base.IOUtil;


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
		setJsonMimeContentType(response);
		String json = object2Json(obj);
		try {
			writeString(json, request, response);
		} catch (IOException e) {
			logger.error("返回json给客户端出错!",e);
		}
	}

	private void setJsonMimeContentType(HttpServletResponse response) {
		response.setHeader("Content-Type", "application/json;charset=" + RouterConfig.charset);
	}
	
	/**
	 * 把string写到response的输出流
	 * @param string
	 * @throws IOException 
	 */
	protected void writeString(String string, HttpServletRequest request, HttpServletResponse response) throws IOException{
		ServletOutputStream out = response.getOutputStream();
		if (RouterConfig.requestCryptor != null && RouterConfig.requestCryptor.shouldIWork(request)) {
			string = RouterConfig.requestCryptor.encryptPayload(string, request, response);
		}
		out.write(string.getBytes(RouterConfig.charset));
		out.flush();
		logger.debug("返回给客户端内容----->"+string);
	}
    
    protected boolean shouldReturnJsonMessage(Exception e,HttpServletRequest request, HttpServletResponse response) {
    	return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
    
    protected void onException(Exception e,HttpServletRequest request, HttpServletResponse response) {
    	Message message;
    	if (e instanceof MessageExceptionInterface) {//兼容旧版messageRuntime
			message = ((MessageExceptionInterface)e).getMsg();
			if (RouterConfig.devMode) {
				logger.debug("抛出异常提示:",e);
			}else {
				logger.debug("抛出异常提示:",e.getMessage());
			}
		}else {
			message = new Message(Message.statusCode_UnknownError, RouterConfig.defaultErrorMessage);
			if ((RouterConfig.devMode  || 
	    			MessageRuntimeException.class.getSimpleName().equals(e.getClass().getSimpleName()) )
					&& !StringUtil.isStringEmpty(e.getMessage())) {
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
				request.getRequestDispatcher(PathUtil.mergePath(RouterConfig.baseJspPath, RouterConfig.message_page)).forward(request, response);
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
    		dealMultipartNotWork(request);
    		
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
			DaoUtil.relese();
			requestThreadLocal.remove();
			responseThreadLocal.remove();
		}
    }

    /**
     * 部分项目因为不是通过Servlet分发的http请求(filter分发,更灵活),导致springBoot的Multipart配置不生效
     * @param request
     */
	private void dealMultipartNotWork(HttpServletRequest request) {
		if (request instanceof RequestFacade) {
			Request r = (Request) ReflectUtil.getValue("request", request);
			r.getContext().setAllowCasualMultipartParsing(true);
//			r.getWrapper().setMultipartConfigElement(new MultipartConfigElement(""));
		}
	}
    
    private void forwardNow(HttpServletRequest request, HttpServletResponse response) throws Exception{  
		String requestURI = request.getRequestURI();
		if (RouterConfig.requestCryptor != null && RouterConfig.requestCryptor.shouldIWork(request)) {
			requestURI = RouterConfig.requestCryptor.decryptUrl(requestURI, request);
			logger.debug("解密后真正请求的URI:" + requestURI);
		}
		
		String parentPath = getParentPath(requestURI);
		Class<? extends BaseProcessor> processor = getProcessor(response, parentPath);
		
		if (processor == null) {
			return;
		}
		
		String methodName = getMethodName(requestURI);
		
		try {
			Method method = processor.getMethod(methodName);
			if (method.getDeclaringClass() != processor) {
				logger.error("方法%s为父类方法,不允许通过http调用,若要调用,请在子类重写该方法!",method.toString());
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
				if (!checkPower(request, response, requestURI,annotation.value())) {
					return;
				}
			}else if (!checkPower(request, response, requestURI, getPower(processor.getAnnotation(Power.class)))) {
				return;
			}
			
			try {
				BaseProcessor newInstance = newProcessor(processor);
				paramterFiller.fillParamters2ProcessorData(newInstance, request, response);
				
				ReflectUtil.setValue("executeMethod", newInstance, methodName);
				
				newInstance.beforeAction(methodName);
				Object invoke = method.invoke(newInstance);
				newInstance.afterAction(methodName, invoke, method.getReturnType());
				
				if (invoke != null) {
					if (invoke instanceof String) {
						String jsp = (String) invoke;
						if (RouterConfig.steed_forward.equals(invoke)) {
							jsp = PathUtil.mergePath(RouterConfig.baseJspPath, parentPath+methodName+".jsp");
						}
						if (jsp.endsWith(".jsp")) {
							if (!jsp.startsWith("/")) {
								jsp = PathUtil.mergePath(RouterConfig.baseJspPath, parentPath + jsp);
							}
							logger.debug("forward到%s",jsp);
							request.getRequestDispatcher(jsp).forward(request, response);;
						}else {
							//json类型的字符串设置content-type = application/json,效率原因,不解析json,通过前后括号判断
							if ((jsp.startsWith("{") && jsp.endsWith("}")) || (jsp.startsWith("[") && jsp.endsWith("]"))) {
								setJsonMimeContentType(response);
							}
							writeString(jsp, request, response);
						}
					}else if(invoke instanceof File) {
						ServletOutputStream outputStream = response.getOutputStream();
						IOUtil.file2OutpuStream((File)invoke, outputStream);
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
