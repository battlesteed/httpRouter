package steed.router.converter;

import java.lang.reflect.Field;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import steed.util.reflect.ReflectUtil;

public class DateConverter implements ParamterConverter{

	@Override
	public Object convert(Field field, Object target, HttpServletRequest request, String parameterName) {
		if (!Date.class.isAssignableFrom(field.getType())) {
			throw new IllegalArgumentException("该转换器只能转Date类型的数据!");
		}
		
		return ReflectUtil.convertFromString(field.getType(), request.getParameter(parameterName));
	}
	
//	private Date

}
