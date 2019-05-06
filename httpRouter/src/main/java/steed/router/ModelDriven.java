package steed.router;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public interface ModelDriven<T> {
    T getModel();
    /**
     * model创建并填充httpRequest参数后,会回调该方法,
     * 可以在该方法设置model额外的字段,比如组织,公司什么的
     * @param t
     */
    default void onModelReady(T t) {};
    
    default Class<T> getModelClass() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		if (!(genericSuperclass instanceof ParameterizedType)) {
			return null;
		}
		ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
		Class<T> clazz = (Class<T>) (parameterizedType.getActualTypeArguments()[0]);
		return clazz;
	}
}