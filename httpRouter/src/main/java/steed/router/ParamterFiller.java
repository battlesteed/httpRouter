package steed.router;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steed.router.converter.BaseTypeConverter;
import steed.router.converter.ParamterConverter;
import steed.router.exception.RouterException;
import steed.router.processor.BaseProcessor;
import steed.util.base.StringUtil;
import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory;
import steed.util.reflect.ReflectUtil;

/**
 * 参数填充器,把request 传过来的参数填充到实体类
 * @author battlesteed
 *
 */
public class ParamterFiller {
	private static LinkedHashMap<Class<?>, ParamterConverter> paramterConverterMap = new LinkedHashMap<>();
	private static final Logger logger =LoggerFactory.getLogger(ParamterFiller.class);
	private static final BaseTypeConverter baseTypeConverter = new BaseTypeConverter();
	
	/**
	 *  注册参数转换器,先注册的转换器优先权高,并<b>不会按匹配度来选择converter</b>.<br><br>
	 *  比如,先后调用了registParamterConverter(List.class, converter1),registParamterConverter(ArrayList.class, converter2)<br>
	 *  那么ArrayList将会调用converter1进行转换,converter2永远不会被调用到
	 * @param clazz
	 * @param converter
	 */
	public static void registParamterConverter(Class<?> clazz,ParamterConverter converter) {
		paramterConverterMap.put(clazz, converter);
	}
	
	/**
	 *  注销参数转换器
	 * @param clazz
	 * @param converter
	 */
	public static void unRegistParamterConverter(Class<?> clazz) {
		paramterConverterMap.remove(clazz);
	}
	
	/*class FieldChain{
		String name;
		Set<FieldChain> son = new HashSet<>();
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof FieldChain) {
				return name.equals(((FieldChain)obj).name);
			}
			return false;
		}
		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}*/
	
	  /**
      *     把http请求中的参数填充到Processor
     *    
     */
    protected void fillParamters2ProcessorData(BaseProcessor processor,HttpServletRequest request,HttpServletResponse response) {
    	Class<? extends BaseProcessor> class1 = processor.getClass();
		/*Object model = null;
		if (processor instanceof ModelDriven<?>) {
			model = ((ModelDriven<?>) processor).getModel();
		}*/
    	//TODO 数组,map填充
    	Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			String[] split = parameterName.split("\\.");
			Field field = null;
			Class<?> target = processor.getClass();
			Object container = processor;
			for(String temp:split){
				field = ReflectUtil.getField(target, temp, true);
				if (field == null || !canAccess(field, target)) {
					break;
				}
				container = getFieldValue(field, container, request, parameterName);
			}
		}
	}
    
    private Object paramter2Field(Field field,Object target,HttpServletRequest request,String parameterName) {
    	for (Class<?> temp:paramterConverterMap.keySet()) {
			if (temp.isAssignableFrom(field.getType())) {
				return paramterConverterMap.get(temp).convert(field, target, request, parameterName);
			}
		}
    	if (ReflectUtil.isClassBaseType(field.getType())) {
			return baseTypeConverter.convert(field, target, request, parameterName);
		}
    	logger.warn("未找到可以转换类%s的转换器,放弃转换该参数!",field.getType().getName());
    	return null;
    }
    
    
    private Object getFieldValue(Field field,Object target,HttpServletRequest request,String parameterName) {
    	field.setAccessible(true);
    	Object object = null;
		try {
			object = field.get(target);
			if (object == null) {
				object = field.getType().newInstance();
				field.set(object, target);
			}
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException e) {
			throw new RouterException(e);
		}
    	return object;
    }
    
    /**
     * 没有public set方法的字段不自动填充数据
     * @return
     */
    private boolean canAccess(Field field,Class<?> target) {
    	if (Modifier.isPublic(field.getModifiers())) {
			return true;
		}
    	Method method = ReflectUtil.getMethod(target, StringUtil.getFieldIsMethodName(field.getName()), true);
    	return method != null;
    }
	
}
