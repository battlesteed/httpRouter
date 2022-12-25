package steed.router;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface ModelDriven<T> {
	T getModel();

	/**
	 * model创建并填充httpRequest参数后,会回调该方法, 可以在该方法设置model额外的字段,比如组织,公司之类
	 * 
	 * @param t
	 */
	default void onModelReady(T t) {
	};

	default Class<T> getModelClass() {
		Class<?> clazz = getClass();
		Type genericSuperclass = clazz.getGenericSuperclass();
		while (!(genericSuperclass instanceof ParameterizedType) && clazz != Object.class) {
			clazz = clazz.getSuperclass();
			genericSuperclass = clazz.getGenericSuperclass();
		}
		if (clazz == Object.class) {
			return null;
		}
//		if (!(genericSuperclass instanceof ParameterizedType)) {
//			return null;
//		}
		try {
			ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
			return (Class<T>) (parameterizedType.getActualTypeArguments()[0]);
		} catch (ClassCastException e) {
			return null;
		}
	}
}