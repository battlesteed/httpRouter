package steed.router.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import steed.router.ModelDriven;
import steed.router.annotation.Path;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;
import steed.util.base.BaseUtil;
import steed.util.base.StringUtil;
import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory;
import steed.util.reflect.ReflectResult;
import steed.util.reflect.ReflectUtil;

public class SimpleAPIConfigLoader implements APIConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	
	private static Map<Class<? extends BaseProcessor>, ProcessorConfig> configCache = new HashMap<Class<? extends BaseProcessor>, ProcessorConfig>();
	
	@Override
	public Map<Class<? extends BaseProcessor>, ProcessorConfig> loadProcessorsConfig(Map<String, Class<? extends BaseProcessor>> pathProcessor) {
		pathProcessor.values().forEach((temp) -> {
			loadConfig(temp);
		});
		return configCache;
	}

	private Map<String, Parameter> loadConfig(Class<? extends BaseProcessor> processor) {
		Map<String, Parameter> fatherParameter = new HashMap<String, Parameter>();
		if(configCache.get(processor) != null) {
			return configCache.get(processor).getParameters();
		}
		if (BaseProcessor.class.isAssignableFrom(processor.getSuperclass())) {
			fatherParameter.putAll(loadConfig((Class<? extends BaseProcessor>) processor.getSuperclass()));
		}
		
		logger.debug("加载类%s的api配置",processor.getName());
		URL resourceURL = BaseUtil.getResourceURL(processor.getName().replace(".", "/")+".json");
		if (resourceURL != null) {
			try {
				ProcessorConfig fromJson = new Gson().fromJson(new InputStreamReader(resourceURL.openStream()), ProcessorConfig.class);
				Map<String, Parameter> parameters = fromJson.getParameters();
				
				setParameterType(processor, parameters);
				
				setPath(processor, fromJson);
				
				mergeMap(fatherParameter, fromJson.getRemoveParameters(), parameters);
				
				fromJson.getApis().values().forEach((api)->{
					mergeMap(parameters, api.getRemoveParameters(), api.getParameters());
					setParameterType(processor, api.getParameters());
				});
				configCache.put(processor, fromJson);
				
				return parameters;
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}
		return fatherParameter;
	}
	

	private void setPath(Class<? extends BaseProcessor> processor, ProcessorConfig fromJson) {
		if (StringUtil.isStringEmpty(fromJson.getPath())) {
			Path annotation = processor.getAnnotation(Path.class);
			if (annotation != null) {
				fromJson.setPath(annotation.value());
			}
		}
	}

	private void mergeMap(Map<String, Parameter> source, String[] removeParameters, Map<String, Parameter> target) {
		source.entrySet().forEach((parameter) -> {
			if (!target.containsKey(parameter.getKey())) {
				target.putIfAbsent(parameter.getKey(), parameter.getValue());
			}
		});
		if (removeParameters != null) {
			for (String temp : removeParameters) {
				target.remove(temp);
			}
		}
	}
	
	private Class<?> getModelClass(Class<? extends ModelDriven<?>> clazz) {
		ParameterizedType parameterizedType = (ParameterizedType)clazz.getGenericSuperclass();
		return (Class<?>) (parameterizedType.getActualTypeArguments()[0]);
	}

	private Field getField(Class<? extends BaseProcessor> processor, String fieldName) {
		ReflectResult field = ReflectUtil.getChainField(processor, fieldName);
		if (field == null && ModelDriven.class.isAssignableFrom(processor)) {
			try {
				field = ReflectUtil.getChainField(getModelClass((Class<? extends ModelDriven<?>>) processor), fieldName);
			} catch (SecurityException e1) {
				e1.printStackTrace();
			}
		}
		return field.getField();
	}
	
	
	private void setParameterType(Class<? extends BaseProcessor> processor, Map<String, Parameter> parameters) {
		parameters.entrySet().forEach((e)->{
			if(StringUtil.isStringEmpty(e.getValue().getType())) {
				Field field = getField(processor, e.getKey());
				if (field != null) {
					e.getValue().setType(field.getType().getSimpleName());
				}
			}
		});
	}
}
