package steed.router.converter;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

/**
 *  参数转换器,把http请求中的参数转换成java对象中的字段
 * @author battlesteed
 *
 *@see steed.router.ParamterFiller#registParamterConverter(Class, ParamterConverter)
 */
public interface ParamterConverter {
	/**
	 * 把http请求中的参数转换成java对象中的字段
	 * @param field 要转换的字段
	 * @param target 转换的字段所属的对象
	 * @param request 
	 * @param parameterName 客户端传过来的参数名
	 * 
	 * @return 转换后的字段值
	 */
	public Object convert(Field field,Object target,HttpServletRequest request,String parameterName);
}
