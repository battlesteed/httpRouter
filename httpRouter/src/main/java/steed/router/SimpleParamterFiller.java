package steed.router;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectUtil;
import steed.router.annotation.DontAccess;
import steed.router.converter.BaseTypeConverter;
import steed.router.converter.ParamterConverter;
import steed.router.exception.RouterException;
import steed.router.processor.BaseProcessor;

/**
 * 参数填充器,把request 传过来的参数填充到实体类
 * @author battlesteed
 *
 */
public class SimpleParamterFiller implements ParameterFiller{
	private static LinkedHashMap<Class<?>, ParamterConverter> paramterConverterMap = new LinkedHashMap<>();
	private static final Logger logger =LoggerFactory.getLogger(SimpleParamterFiller.class);
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
	
    @Override
	public void fillParamters2ProcessorData(BaseProcessor processor,HttpServletRequest request,HttpServletResponse response) throws Exception {
    	fillParamters2Data(processor, request, response);
    	if (processor instanceof ModelDriven<?>) {
			Object model = ((ModelDriven<?>) processor).getModel();
			if (model != null) {
				fillParamters2Data(model, request, response);
				((ModelDriven<Object>) processor).onModelReady(model);
			}
		}
	}
	
    protected void fillParamters2Data(Object container,HttpServletRequest request,HttpServletResponse response) throws Exception {
		/*Object model = null;
		if (processor instanceof ModelDriven<?>) {
			model = ((ModelDriven<?>) processor).getModel();
		}*/
    	//TODO 数组,map填充
    	Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			singleParamters2Data(container, request, parameterName);
		}
		
	}

    protected void singleParamters2Data(Object container, HttpServletRequest request, String parameterName)
			throws Exception {
		String[] split = parameterName.split("\\.");
		Field field = null;
		Class<?> target = container.getClass();
		for( int i = 0; i < split.length; i++){
			field = ReflectUtil.getField(target, split[i], false);
			if (field == null || !canAccess(field, target)) {
				break;
			}
			if (i == split.length-1) {
				paramter2Field(field, container, request, parameterName);
			}else {
				container = getFieldValue(field, container, request, parameterName);
			}
		}
	}
    
    protected void paramter2Field(Field field, Object container, HttpServletRequest request, String parameterName) throws Exception {
		
		try {
			if (RouterConfig.devMode) {
				logger.debug("开始填充参数%s:%s",parameterName, request.getParameter(parameterName));
			}
			Object convertParamter = convertParamter(field, container, request, parameterName);
			if (Modifier.isPublic(field.getModifiers())) {
				field.setAccessible(true);
				field.set(container, convertParamter);
			}else {
				Method method = getSetterMethod(field, container.getClass());
				method.invoke(container, convertParamter);
			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			logger.error("转换参数"+parameterName+"出错",e);
			throw e;
		}
	}

	protected Object convertParamter(Field field,Object container,HttpServletRequest request,String parameterName) {
    	for (Class<?> temp:paramterConverterMap.keySet()) {
			if (temp.isAssignableFrom(field.getType())) {
				return paramterConverterMap.get(temp).convert(field, container, request, parameterName);
			}
		}
    	if (field.getType() == String.class) {
			return request.getParameter(parameterName);
		}
    	if (ReflectUtil.isClassBaseType(field.getType())) {
			return baseTypeConverter.convert(field, container, request, parameterName);
		}
    	logger.warn("未找到可以转换类%s的转换器,放弃转换该参数!",field.getType().getName());
    	return null;
    }
    
    
    protected Object getFieldValue(Field field,Object target,HttpServletRequest request,String parameterName) {
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
    protected boolean canAccess(Field field,Class<?> target) {
    	if (field.getAnnotation(DontAccess.class) != null || Modifier.isStatic(field.getModifiers())) {
			return false;
		}
    	if (Modifier.isPublic(field.getModifiers())) {
			return true;
		}
    	Method method = getSetterMethod(field, target);
    	return method != null && method.getAnnotation(DontAccess.class) == null;
    }
    
    private Method getSetterMethod(Field field, Class<?> target) {
		return getMethod(target, StringUtil.getFieldSetterName(field.getName()), true,field.getType());
	}
    
    private static final Map<String, Method> methodCache = new HashMap<>();
    /**
	 * 获取类的方法(包括父类)
	 * @param clazz
	 * @param methodName
	 * @param onlyPublic 是否只获取public方法
	 * @return 方法不存在则返回null
	 */
	public static Method getMethod(Class<?> clazz,String methodName,boolean onlyPublic,Class<?>... parameterTypes){
//		Class<?> target = clazz;
		Method declaredMethod = null;
		String key = clazz.getName()+"."+methodName;
		if (methodCache.containsKey(key)) {
			declaredMethod = methodCache.get(key);
			if (declaredMethod == null) {
				return null;
			}
		}
		while(clazz != Object.class && declaredMethod == null){
			try {
				declaredMethod = clazz.getDeclaredMethod(methodName,parameterTypes);
			} catch (NoSuchMethodException | SecurityException e) {
				//BaseUtil.getLogger().info("获取方法出错!{}",e.getMessage());
			}
			clazz = clazz.getSuperclass();
		}
		if (declaredMethod != null && onlyPublic && !Modifier.isPublic(declaredMethod.getModifiers())) {
			return null;
		}
//		BaseUtil.getLogger().info("{}没有{}方法",new Object[]{target.getName(),methodName});
		return declaredMethod;
	}
	
}
