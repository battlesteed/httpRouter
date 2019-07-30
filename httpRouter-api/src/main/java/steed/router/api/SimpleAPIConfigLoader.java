package steed.router.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.PathUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectResult;
import steed.ext.util.reflect.ReflectUtil;
import steed.router.ModelDriven;
import steed.router.annotation.Path;
import steed.router.api.annotation.DocParam;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;

public class SimpleAPIConfigLoader implements APIConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	
	public static Map<Class<? extends BaseProcessor>, ProcessorConfig> configCache = new HashMap<Class<? extends BaseProcessor>, ProcessorConfig>();
	
	@Override
	public Map<Class<? extends BaseProcessor>, ProcessorConfig> loadProcessorsConfig(Map<String, Class<? extends BaseProcessor>> pathProcessor) {
		pathProcessor.values().forEach((temp) -> {
			loadConfig(temp);
		});
		return configCache;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Parameter> loadConfig(Class<? extends BaseProcessor> processor) {
		Map<String, Parameter> fatherParameter = new HashMap<String, Parameter>();
		if(configCache.get(processor) != null) {
			return configCache.get(processor).getParameters();
		}
		if (BaseProcessor.class.isAssignableFrom(processor.getSuperclass())) {
			fatherParameter.putAll(loadConfig((Class<? extends BaseProcessor>) processor.getSuperclass()));
		}
		
		URL resourceURL = BaseUtil.getResourceURL(processor.getName().replace(".", "/")+".json");
		if (resourceURL != null) {
			try {
				logger.debug("加载类%s的api配置文件",processor.getName());
				ProcessorConfig fromJson = new Gson().fromJson(new InputStreamReader(resourceURL.openStream()), ProcessorConfig.class);
				Map<String, Parameter> parameters = fromJson.getParameters();
				
				setParameterInfo(processor, parameters);
				
				setPath(processor, fromJson);
				
				mergeMap(fatherParameter, fromJson.getRemoveParameters(), parameters);
				
				fromJson.getApis().forEach((key,api)->{
					mergeMap(parameters, api.getRemoveParameters(), api.getParameters());
					setParameterInfo(processor, api.getParameters());
					api.setPath(PathUtil.mergePath(fromJson.getPath(), key));
				});
				configCache.put(processor, fromJson);
				
				return parameters;
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}
		return fatherParameter;
	}
	

	protected void setPath(Class<? extends BaseProcessor> processor, ProcessorConfig fromJson) {
		if (StringUtil.isStringEmpty(fromJson.getPath())) {
			Path annotation = processor.getAnnotation(Path.class);
			if (annotation != null) {
				fromJson.setPath(annotation.value());
			}
		}
	}

	protected void mergeMap(Map<String, Parameter> source, String[] removeParameters, Map<String, Parameter> target) {
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
	
	protected Class<?> getModelClass(Class<? extends ModelDriven<?>> clazz) {
			ParameterizedType parameterizedType = (ParameterizedType)clazz.getGenericSuperclass();
		Type type = parameterizedType.getActualTypeArguments()[0];
		if (type instanceof Class<?>) {
			return (Class<?>)type;
		}
		return null;
	}

	protected Field getField(Class<? extends BaseProcessor> processor, String fieldName) {
		ReflectResult field = ReflectUtil.getChainField(processor, fieldName);
		if (field == null && ModelDriven.class.isAssignableFrom(processor)) {
			try {
				Class<?> modelClass = getModelClass((Class<? extends ModelDriven<?>>) processor);
				if (modelClass == null) {
					return null;
				}
				field = ReflectUtil.getChainField(modelClass, fieldName);
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}
		}
		if (field == null) {
			return null;
		}
		return field.getField();
	}
	
	
	protected void setParameterInfo(Class<? extends BaseProcessor> processor, Map<String, Parameter> parameters) {
		parameters.forEach((k,v)->{
			Field field = getField(processor, k);
			if (field != null) {
				if(StringUtil.isStringEmpty(v.getType())) {
					v.setType(field.getType().getSimpleName());
				}
				setParamByAnnotion(v, field);
			}
			if (v.isRequire() == null) {
				v.setRequire(Parameter.defaultRequire);
			}
			v.setName(k);
		});
	}

	protected void setParamByAnnotion(Parameter v, Field field) {
		DocParam annotation = field.getAnnotation(DocParam.class);
		if (annotation != null) {
			if (StringUtil.isStringEmpty(v.getDesc())) {
				v.setDesc(annotation.value());
			}
			if (v.getMaxLength() == -1) {
				v.setMaxLength(annotation.maxLength());
			}
			if (v.getMinLength() == -1) {
				v.setMinLength(annotation.minLength());
			}
			if (v.isRequire() == null) {
				v.setRequire(annotation.require());
			}
		}
	}
}
