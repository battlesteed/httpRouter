package steed.router.processor;

import java.io.Serializable;
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
 *  <ul>
 * @author battlesteed
 * @see steed.router.annotation.DontAccess
 *
 */
public abstract class BaseProcessor implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 *  return该值会自动forward到该方法对应的jsp页面
	 * (例如该Processor的path是“admin”，调用的方法是"index",则对应的jsp路径为“/WEB-INF/jsp/admin/index.jsp”)
	 * 
	 */
	protected static final String steed_forward = steed.router.HttpRouter.steed_forward;
	
	/**
	 * Processor开始处理http请求之前会执行该方法
	 * @param methodName Processor即将被http访问到的方法名
	 */
	public void beforeAction(String methodName) {
		
	}
	
	/**
	 * Processor开始处理完请求后会执行该方法
	 * @param methodName Processor即将被http访问到的方法名
	 */
	public void afterAction(String methodName) {
		
	}
	
}
