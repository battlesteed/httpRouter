package steed.router.processor;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.router.HttpRouter;
import steed.router.RouterConfig;
import steed.router.domain.Message;
import steed.router.exception.message.MessageRuntimeException;
import steed.util.AssertUtil;
/**
 * 处理器,处理HttpRouter分发过来的http请求<br>
 * 若Processor中的方法返回String,HttpRouter会forward到String对应的jsp页面(若string以.jsp结尾)或直接把string内容返回给客户端,若返回其它类型的对象,则会将对象转成json写到response,
 * 若方法返回null或方法返回值为void则HttpRouter不做任何处理<br><br>
 *  注意: public方法和字段均会被http请求访问到,请注意安全,可以加DontAccess注解禁止http访问.重写父类方法public方法时注意加DontAccess注解,<b>不要乱用public修饰符!</b>
 *  <br><br>
 *  BaseProcessor的子类若加了{@link steed.router.annotation.Path} 注解,则会被http访问到,详细访问规则如下:<br><br>
 *  假设类A继承了BaseProcessor,并加了steed.router.annotation.Path("foo") 注解,则:<br>
 *  <ul>
 *  <li>A类所有(public 任意返回值类型 方法名()) 的方法会被 uri为 '/foo/方法名' 的http请求调用.</li>
 *  <li>A类父类及A类所有非public 方法或有参数的方法或方法名以get,set开头的方法均不会被http请求调用.</li>
 *  <li>A类父类及A类所有public字段或public 的 set方法  均会被自动填充http请求传过来的参数 </li>
 *  <li>所有加了{@link steed.router.annotation.DontAccess}注解或非public或静态的类,字段,方法均会禁止http访问</li>
 *  </ul>
 * @author battlesteed
 * @see steed.router.annotation.DontAccess
 * @see #steed_forward
 *
 */
public abstract class BaseProcessor implements Serializable {
	private static Logger logger = LoggerFactory.getLogger(BaseProcessor.class);
	private static final long serialVersionUID = 1L;
	
	/**
	 * 该Processor被http访问到的方法名
	 */
	protected String executeMethod;
	
	/**
	 *  return该值会自动forward到该方法对应的jsp页面
	 * (例如该Processor的path是“admin”，调用的方法是"index",则对应的jsp路径为“/WEB-INF/jsp/admin/index.jsp”)
	 * 
	 */
	protected static final String steed_forward = RouterConfig.steed_forward;
	
	/**
	 * Processor开始处理http请求之前会执行该方法
	 * @param methodName Processor即将被http访问到的方法名
	 */
	public void beforeAction(String methodName) {
		
	}
	
	/**
	 * Processor开始处理完请求后会执行该方法
	 * @param methodName Processor即将被http访问到的方法名
	 * @param returnValue Processor方法返回值,若方法为void,则该值为null
	 */
	public void afterAction(String methodName,Object returnValue) {
		
	}
	
	protected HttpServletResponse getResponse() {
		return HttpRouter.getResponse();
	}
	protected HttpServletRequest getRequest() {
		return HttpRouter.getRequest();
//		return ContextUtil.getRequest();
	}
	
	protected HttpSession getSession() {
		return getRequest().getSession();
	}
	
	/**
	 * 把对象以json形式写到response的输出流，一般用于ajax
	 * @param obj 要输出的对象
	 */
	protected void writeJson(Object obj){
		getResponse().setHeader("Content-Type", "application/json");
		writeString(RouterConfig.defaultJsonSerializer.object2Json(obj));
	}
	
	/**
	 * 往request中放东西
	 * @param key 键
	 * @param obj 值
	 */
	protected void setRequestAttribute(String key,Object obj){
		getRequest().setAttribute(key, obj);
	}
	/**
	 * 从request中取东西
	 * @param key 键
	 */
	protected Object getRequestAttribute(String key){
		return getRequest().getAttribute(key);
	}
	/**
	 * 获取request中的参数
	 * @param key 键
	 */
	protected String getRequestParameter(String key){
		return getRequest().getParameter(key);
	}
	/**
	 * 获取request中的参数
	 * @param key 键
	 */
	protected boolean isRequestParameterEmpty(String key){
		return StringUtil.isStringEmpty(getRequest().getParameter(key));
	}
	
	protected String[] getRequestParameters(String key){
		return getRequest().getParameterValues(key);
	}
	/**
	 * 往session中放东西
	 * @param key 键
	 * @param obj 值
	 */
	protected void setSessionAttribute(String key,Object obj){
		getSession().setAttribute(key, obj);
	}
	
	protected ServletContext getServletContext() {
		//艹，兼容Servlet2.5只能这样写
		return getRequest().getSession().getServletContext();
	}
	
	/**
	 * 把string写到response的输出流
	 * @param string
	 */
	protected void writeString(String string){
		try {
			logger.debug("返回给客户端内容:%s",string);
			ServletOutputStream out;
			out = getResponse().getOutputStream();
			out.write(StringUtil.getSystemCharacterSetBytes(string));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 更加简洁的数据校验方法,若 !yourAssert ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param yourAssert
	 * @param message    
	 * @param statusCode
	 */
	public void assertTrue(boolean yourAssert, String message, int statusCode) {
		AssertUtil.assertTrue(yourAssert, message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 !yourAssert ,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param yourAssert
	 * @param message    
	 */
	public void assertTrue(boolean yourAssert, String message) {
		AssertUtil.assertTrue(yourAssert, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted为空或null ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 * @param statusCode
	 */
	public void assertNotEmpty(String asserted, String message, int statusCode) {
		AssertUtil.assertTrue(!StringUtil.isStringEmpty(asserted), message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted为空或null ,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertNotEmpty(String asserted, String message) {
		AssertUtil.assertNotEmpty(asserted, message, Message.statusCode_MessageExecption);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted不为空且不为null ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertEmpty(String asserted, String message) {
		AssertUtil.assertEmpty(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted不为空且不为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertEmpty(String asserted, String message, int statusCode) {
		AssertUtil.assertTrue(StringUtil.isStringEmpty(asserted), message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertNotNull(Object asserted, String message) {
		AssertUtil.assertNotNull(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted为null,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertNotNull(Object asserted, String message, int statusCode) {
		AssertUtil.assertTrue(asserted != null, message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted不为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertNull(Object asserted, String message) {
		AssertUtil.assertNull(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted不为null,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public void assertNull(Object asserted, String message, int statusCode) {
		AssertUtil.assertTrue(asserted == null, message, statusCode);
	}
	
}
