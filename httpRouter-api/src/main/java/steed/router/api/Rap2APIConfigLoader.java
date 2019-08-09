package steed.router.api;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.api.domain.ReturnVal;
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
		try {
			loadRap2Data();
		} catch (Exception e) {
			logger.error("加载rap2接口文档出错!");
			return configCache;
		}
		
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
			if (api.has("returnSample")) {
				JsonElement jsonElement = api.get("returnSample");
				a.setReturnSample(jsonElement.getAsString());
			}
			apis2.put(api.get("url").getAsString().replace(path, ""), a);
			
			addParameters(api, a.getParameters());
			addReturnval(api, a.getReturns());
			
			configCache.put(processor, fromJson);
			
		} catch (JsonSyntaxException | JsonIOException e) {
			logger.error("加载rap2 api配置出错!",e);
		}
	}

	private void addReturnval(JsonObject api, List<ReturnVal> returns) {
		JsonArray properties = api.getAsJsonArray("properties");
		Map<Long, ReturnVal> map = new LinkedHashMap<Long, ReturnVal>();
		Map<Long, ReturnVal> index = new HashMap<Long, ReturnVal>();
		for (int i = 0; i < properties.size(); i++) {
			JsonObject param = (JsonObject) properties.get(i);
			if (!"response".equals(param.get("scope").getAsString())) {
				continue;
			}
			
			ReturnVal r = new ReturnVal();
			JsonElement description = param.get("description");
			if (!description.isJsonNull()) {
				r.setDesc(description.getAsString());
			}
			r.setName(param.get("name").getAsString());
			r.setRequire(param.get("required").getAsBoolean());
			r.setType(param.get("type").getAsString());
			long id = param.get("id").getAsLong();
			index.put(id, r);
			
			long parentId = param.get("parentId").getAsLong();
			if (parentId == -1) {
				map.put(id, r);
			}else {
				index.get(parentId).addSonReturn(r);
			}
		}
		
		returns.addAll(map.values());
	}
	private void addParameters(JsonObject api, Map<String, Parameter> parameters) {
		JsonArray properties = api.getAsJsonArray("properties");
		for (int i = 0; i < properties.size(); i++) {
			JsonObject param = (JsonObject) properties.get(i);
			if (!"request".equals(param.get("scope").getAsString())) {
				continue;
			}
			//TODO 加入请求头校验
			if(param.get("pos").getAsInt() != 2) {
				continue;
			}
			Parameter p = new Parameter();
			JsonElement description = param.get("description");
			if (!description.isJsonNull()) {
				p.setDesc(description.getAsString());
			}
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
