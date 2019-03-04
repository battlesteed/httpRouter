package steed.router.processor;

import java.io.Serializable;
/**
 * 处理器,处理HttpRouter分发过来的http请求<br>
 * 若Processor中的方法返回String,HttpRouter会forward到String对应的jsp页面,若返回其它类型的对象,则会将对象转成json写到response,
 * 若方法返回null或方法返回值为void则HttpRouter不做任何处理<br><br>
 *  注意:  该类的所有public方法和字段均会被http请求访问到,请注意安全.<b>不要乱用public修饰符!</b>
 *  
 * @author battlesteed
 * @see #steed_forward
 *
 */
public class BaseProcessor implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 *  return该值会自动forward到该方法对应的jsp页面
	 * (例如该Processor的path是“admin”，调用的方法是"index",则对应的jsp路径为“/WEB-INF/jsp/admin/index.jsp”)
	 * 
	 */
	protected static final String steed_forward = steed.router.HttpRouter.steed_forward;
	
	
}
