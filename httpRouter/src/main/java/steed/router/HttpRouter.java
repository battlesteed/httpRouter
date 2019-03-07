package steed.router;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import steed.router.annotation.Path;
import steed.router.annotation.Power;
import steed.router.processor.BaseProcessor;
import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory;


/** 
 *  http路由器,建议用单例模式,全局保存一个HttpRouter
 * @author 战马 battle_steed@qq.com
 * 
 */  
public abstract class HttpRouter{
	public final static String steed_forward = "steed_forward";
	public static String charset = "UTF-8";
	
	private static final String jspPath = "/WEB-INF/jsp/";
	
	public final static ParamterFiller paramterFiller = new ParamterFiller();
    private static Map<String, Class<? extends BaseProcessor>> pathProcessor = new HashMap<>();
    private static Logger logger = LoggerFactory.getLogger(HttpRouter.class);
    
    private static final ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<HttpServletRequest>();
    private static final ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<HttpServletResponse>();
	
	public static HttpServletRequest getRequest(){
		return requestThreadLocal.get();
	}
	public static HttpServletResponse getResponse(){
		return responseThreadLocal.get();
	}
    public static Map<String, Class<? extends BaseProcessor>> getPathProcessor() {
		return pathProcessor;
	}

	static {
    	scanProcessor();
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
    protected abstract String object2Json(Object object);

	/**
	 * 把对象以json形式写到response的输出流，一般用于ajax
	 * @param obj 要输出的对象
	 * @throws IOException 
	 */
	protected void writeJsonMessage(Object obj,HttpServletResponse response) throws IOException{
		if (obj == null) {
			return;
		}
		response.setHeader("Content-Type", "application/json");
		String json = object2Json(obj);
		logger.debug("返回json----->"+json);
		writeString(json,response);
	}
	
	/**
	 * 把string写到response的输出流
	 * @param string
	 * @throws IOException 
	 */
	protected void writeString(String string,HttpServletResponse response) throws IOException{
		ServletOutputStream out = response.getOutputStream();
		out.write(string.getBytes(charset));
		out.flush();
	}
    
    
    /**
     *   把http请求中的参数填充到Processor
     */
    protected void fillParamters2ProcessorData(BaseProcessor processor,HttpServletRequest request,HttpServletResponse response) {
    	paramterFiller.fillParamters2ProcessorData(processor, request, response);
    	if (processor instanceof ModelDriven<?>) {
			Object model = ((ModelDriven<?>) processor).getModel();
			if (model != null) {
				paramterFiller.fillParamters2ProcessorData(model, request, response);
			}
		}
	}
  
    public void forward(HttpServletRequest request, HttpServletResponse response){  
    	try {
			requestThreadLocal.set(request);
			responseThreadLocal.set(response);
			try {
				forwardNow(request, response);
			} catch (IOException | ServletException e) {
				logger.error("httpRouter分发请求出错!",e);
			}
		}finally {
			requestThreadLocal.remove();
			responseThreadLocal.remove();
		}
    }
    
    private void forwardNow(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{  
		String requestURI = request.getRequestURI();
		String parentPath = requestURI.substring(0,requestURI.lastIndexOf("/")+1);
		Class<? extends BaseProcessor> processor = pathProcessor.get(parentPath);
		if (processor == null) {
			logger.warn("未找到path为 %s 的Processor!",parentPath);
			response.sendError(404);
			return;
		}
		
		String methodName = getMethodName(requestURI);
		try {
			Method method = processor.getMethod(methodName);
			/*Class<?> returnType = method.getReturnType();
			if (returnType != String.class && returnType != Void.TYPE) {
				logger.warn("在%s中的 %s 方法返回值不为void或string,不能通过http访问!",processor.getName(),methodName);
				response.sendError(404);
				return;
			}*/
			
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
				BaseProcessor newInstance = processor.newInstance();
				fillParamters2ProcessorData(newInstance, request, response);
				Object invoke = method.invoke(newInstance);
				if (invoke != null) {
					if (invoke instanceof String) {
						String jsp = (String) invoke;
						if (steed_forward.equals(invoke)) {
							jsp = mergePath(jspPath, parentPath+methodName+".jsp");
						}
						if (jsp.endsWith(".jsp")) {
							logger.debug("forward到%s",jsp);
							request.getRequestDispatcher(jsp).forward(request, response);;
						}else {
							writeString(jsp, response);
						}
					}else {
						writeJsonMessage(invoke, response);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| InstantiationException e) {
				logger.warn("实例化%s失败!",processor.getName());
				response.sendError(500);
				return;
			}
		} catch (NoSuchMethodException | SecurityException e) {
			logger.warn("在%s中未找到public的 %s 方法!",processor.getName(),methodName);
			response.sendError(404);
			return;
		}
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
    
	private static String getMethodName(String requestURI) {
		String method = requestURI.substring(requestURI.lastIndexOf("/")+1,requestURI.length());
    	if (method.contains(".")) {
			method = method.substring(0,method.indexOf("."));
		}
    	if ("".equals(method)) {
			return "index";
		}
		return method;
	}

	@SuppressWarnings("unchecked")
	private static void scanProcessor() {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Path.class));
//		provider.addIncludeFilter(new AssignableTypeFilter(Processor.class));
//		provider.setResourcePattern("**/*.class");
		Set<BeanDefinition> findCandidateComponents = provider.findCandidateComponents("steed");
		for (BeanDefinition temp:findCandidateComponents) {
			try {
				Class<? extends BaseProcessor> forName = (Class<? extends BaseProcessor>) Class.forName(temp.getBeanClassName());
				Path annotation = forName.getAnnotation(Path.class);
				String path = annotation.value();
				if (pathProcessor.containsKey(path)) {
					logger.warn("%s和%s的path均为%s,%s将被忽略!",pathProcessor.get(path).getName(),temp.getBeanClassName(),path,temp.getBeanClassName());
					continue;
				}
				pathProcessor.put(addSprit(path), forName);
			} catch (ClassNotFoundException | ClassCastException e) {
				logger.error("扫描Processor出错!",e);
			}
		}
	} 
	
	private static String addSprit(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		return path;
	}
	
}
