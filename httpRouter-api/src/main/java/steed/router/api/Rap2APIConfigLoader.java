package steed.router.api;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;

/**
 * 阿里巴巴rap2 接口配置加载器
 * @author battlesteed
 *
 */
public abstract class Rap2APIConfigLoader extends SimpleAPIConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	/**
	 * 存放url-interfaces节点
	 */
	private Map<String, JsonObject> apis = new HashMap<String, JsonObject>();
	
	public Map<String, JsonObject> getApis() {
		return apis;
	}

	@Override
	public Map<Class<? extends BaseProcessor>, ProcessorConfig> loadProcessorsConfig(Map<String, Class<? extends BaseProcessor>> pathProcessor) {
		loadRap2Data();
		
		apis.entrySet().forEach((temp) -> {
			String url = temp.getKey();
			String path = url.substring(0, url.lastIndexOf("/")+1);
			Class<? extends BaseProcessor> processor = pathProcessor.get(path);
			if (processor != null) {
				loadConfig(path, processor, temp.getValue());
			}else {
				logger.warn("未找到path为"+path+"的processor!");
			}
		});
		return configCache;
	}

	private void loadRap2Data() {
		String rap2Data = getRap2Data();
		JsonArray modules = ((JsonObject) new JsonParser().parse(rap2Data)).getAsJsonObject("data").getAsJsonArray("modules");
		for (int i = 0; i < modules.size(); i++) {
			JsonObject moudle = (JsonObject) modules.get(i);
			JsonArray interfaces = moudle.getAsJsonArray("interfaces");
			for (int j = 0; j < interfaces.size(); j++) {
				JsonObject api = (JsonObject) interfaces.get(j);
				apis.put(api.get("url").getAsString(), api);
			}
		}
	}
	
	/**
	 * 获取rap2平台接口配置文件
	 * @return http://rap2api.taobao.org/repository/get?id=仓库id 返回的json
	 */
	protected abstract String getRap2Data();
	
	private void loadConfig(String path,Class<? extends BaseProcessor> processor,JsonObject api) {
		try {
			logger.debug("加载类%s的api配置",processor.getName());
			ProcessorConfig fromJson = getProcessorConfig(processor);
			Map<String, Parameter> parameters = fromJson.getParameters();
			
			
			setParameterInfo(processor, parameters);
			
			setPath(processor, fromJson);
			
			Map<String, Api> apis2 = fromJson.getApis();
			
			Api a = new Api();
			a.setDesc(api.get("description").getAsString());
			a.setName(api.get("name").getAsString());
			a.setPath(api.get("url").getAsString());
			apis2.put(api.get("url").getAsString().replace(path, ""), a);
			
			addParameters(api, a.getParameters());
			
			configCache.put(processor, fromJson);
			
		} catch (JsonSyntaxException | JsonIOException e) {
			logger.error("加载rap2 api配置出错!",e);
		}
	}

	private void addParameters(JsonObject api, Map<String, Parameter> parameters) {
		JsonArray properties = api.getAsJsonArray("properties");
		for (int i = 0; i < properties.size(); i++) {
			JsonObject param = (JsonObject) properties.get(i);
			if (!"request".equals(param.get("scope").getAsString())) {
				continue;
			}
			Parameter p = new Parameter();
			p.setDesc(param.get("description").getAsString());
			p.setName(param.get("name").getAsString());
			p.setRequire(param.get("required").getAsBoolean());
			p.setType(param.get("type").getAsString());
			parameters.put(p.getName(), p);
		}
	}

	private ProcessorConfig getProcessorConfig(Class<? extends BaseProcessor> processor) {
		ProcessorConfig processorConfig = configCache.get(processor);
		if (processorConfig == null) {
			processorConfig = new ProcessorConfig();
			configCache.put(processor, processorConfig);
		}
		return processorConfig;
	}
	
}
