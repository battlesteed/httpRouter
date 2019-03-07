package steed.router.converter;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;

import steed.util.reflect.ReflectUtil;

public class BaseTypeConverter implements ParamterConverter{
//	public static Class<?>[] baseTypeClass = new Class<?>[] {Byte.class,Short.class,Integer.class,Float.class,Boolean.class,Character.class,Double.class ,Long.class};

	@Override
	public Object convert(Field field, Object target, HttpServletRequest request, String parameterName) {
		boolean classBaseData = ReflectUtil.isClassBaseType(field.getType());
		if (!classBaseData) {
			throw new IllegalArgumentException("该转换器只能转换steed.util.reflect.ReflectUtil.isClassBaseType方法返回true的数据类型!");
		}
		return ReflectUtil.convertFromString(field.getType(), request.getParameter(parameterName));
	}

}
