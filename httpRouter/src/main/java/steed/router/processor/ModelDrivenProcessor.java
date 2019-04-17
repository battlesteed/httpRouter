package steed.router.processor;

import java.lang.reflect.ParameterizedType;

import steed.router.ModelDriven;
import steed.router.exception.ReflectException;

public abstract class ModelDrivenProcessor<SteedDomain> extends BaseProcessor implements ModelDriven<SteedDomain> {
	private static final long serialVersionUID = 7774350640186420795L;
	
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
