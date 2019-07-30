package steed.router.api;

public class RouterApiConfig {
	/**
	 * api 配置加载器,可以从rap2或内置api配置文件加载api配置
	 */
	public static APIConfigLoader APIConfigLoader = new SimpleAPIConfigLoader();
}
