package steed.router.processor;

import java.io.Serializable;
/**
 * 处理器,处理HttpRoute分发过来的http请求<br><br>
 *  注意:  该类的所有public方法均会被http请求访问到,请注意安全.<b>不要乱用public修饰符!</b>
 * @author battlesteed
 *
 */
public class BaseProcessor implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 *  return该值会自动forward到该方法对应的jsp页面
	 * (例如该Processor的path是“admin”，调用的方法是"index",则对应的jsp路径为“/WEB-INF/jsp/admin/index.jsp”)
	 */
	public static final String steed_forward = "steed_forward";
	
	
}
