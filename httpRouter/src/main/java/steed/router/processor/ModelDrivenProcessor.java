package steed.router.processor;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ch.qos.logback.classic.gaffer.PropertyUtil;
import ch.qos.logback.core.util.ContextUtil;
import steed.hibernatemaster.domain.BaseDatabaseDomain;
import steed.hibernatemaster.domain.BaseDomain;
import steed.hibernatemaster.domain.Page;
import steed.hibernatemaster.util.DaoUtil;
import steed.hibernatemaster.util.HibernateUtil;
import steed.router.HttpRouter;
import steed.router.ModelDriven;
import steed.router.exception.ReflectException;
import steed.util.base.BaseUtil;
import steed.util.base.DomainUtil;
import steed.util.base.StringUtil;
import steed.util.reflect.ReflectUtil;
/**
 * 处理器,处理HttpRouter分发过来的http请求<br>
 * 若Processor中的方法返回String,HttpRouter会forward到String对应的jsp页面(若string以.jsp结尾)或直接把string内容返回给客户端,若返回其它类型的对象,则会将对象转成json写到response,
 * 若方法返回null或方法返回值为void则HttpRouter不做任何处理<br><br>
 *  注意:  该类的所有public方法和字段均会被http请求访问到,请注意安全.<b>不要乱用public修饰符!</b>
 *  
 * @author battlesteed
 * @see #steed_forward
 *
 */
public class ModelDrivenProcessor<SteedDomain> extends BaseProcessor implements ModelDriven<SteedDomain> {
	private static final long serialVersionUID = 7774350640186420795L;
	
	/**
	 *	┏┓　┏┓
	 * ┏┛┻━━━┛┻┓
	 * ┃　　　　　　　┃
	 * ┃　　　━　　　┃
	 * ┃　┳┛　┗┳　┃
	 * ┃　　　　　　　┃
	 * ┃　　　┻　　　┃
	 * ┃　　　　　　　┃
	 * ┗━┓　　　┏━┛Code is far away from bug with the animal protecting
	 *    ┃　　　┃   神兽保佑
	 *    ┃　　　┃   代码无BUG!
	 *    ┃　　　┗━━━┓
	 *    ┃　　　　   ┣┓
	 *    ┃　　　___┏┛
	 *    ┗┓┓┏━┳┓┏┛
	 *     ┃┫┫┃┫┫
	 *     ┗┻┛┗┻┛
	 */
	
	protected SteedDomain domain;
	
	protected void afterDomainCreate(SteedDomain domain){}

	/**
	 * 通过泛型获取action对应的model
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SteedDomain getModelByReflect(){
		if (domain != null) {
			return domain;
		}
		ParameterizedType parameterizedType = (ParameterizedType)this.getClass().getGenericSuperclass();
		Class<SteedDomain> clazz = (Class<SteedDomain>) (parameterizedType.getActualTypeArguments()[0]); 
		try {
			domain = clazz.newInstance();
			return domain;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReflectException(e);
		}
	}
	
	@Override
	public SteedDomain getModel() {
		return getModelByReflect();
	}
}
