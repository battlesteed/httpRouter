package steed.router.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import steed.router.HttpRouter;
import steed.router.SimpleParamterFiller;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;
import steed.util.base.BaseUtil;
import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory;
import steed.util.reflect.ReflectUtil;

public class APIParamterFiller extends SimpleParamterFiller {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	private static Map<Class<? extends BaseProcessor>, ProcessorConfig> configCache = new HashMap<Class<? extends BaseProcessor>, ProcessorConfig>();

	public APIParamterFiller(HttpRouter router) {
		super();
		router.getPathProcessor().values().forEach((temp) -> {
			loadConfig(temp);
		});
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
		URL resourceURL = BaseUtil.getResourceURL(processor.getName().replace(".", "/").replace("/java", ".json"));
		if (resourceURL != null) {
			try {
				ProcessorConfig fromJson = new Gson().fromJson(new InputStreamReader(resourceURL.openStream()), ProcessorConfig.class);
				Map<String, Parameter> parameters = fromJson.getParameters();
				
				mergeMap(fatherParameter, fromJson.getRemoveParameters(), parameters);
				
				fromJson.getApis().values().forEach((api)->{
					mergeMap(parameters, api.getRemoveParameters(), api.getParameters());
				});
				
				return parameters;
			} catch (JsonSyntaxException | JsonIOException | IOException e) {
				e.printStackTrace();
			}
		}
		return fatherParameter;
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

	@Override
	public void fillParamters2ProcessorData(Object container, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// TODO 数组,map填充
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			String[] split = parameterName.split("\\.");
			Field field = null;
			Class<?> target = container.getClass();
			for (int i = 0; i < split.length; i++) {
				field = ReflectUtil.getField(target, split[i], false);
				if (field == null || !canAccess(field, target)) {
					break;
				}
				if (i == split.length - 1) {
					paramter2Field(field, container, request, parameterName);
				} else {
					container = getFieldValue(field, container, request, parameterName);
				}
			}
		}
	}
}
