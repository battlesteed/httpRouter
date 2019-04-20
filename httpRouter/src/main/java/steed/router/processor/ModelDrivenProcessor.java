package steed.router.processor;

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
	private SteedDomain getModelByReflect(){
		if (domain != null) {
			return domain;
		}
		Class<SteedDomain> clazz = getModelClass(); 
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
