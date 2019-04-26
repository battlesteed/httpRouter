package steed.router.api;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import steed.router.HttpRouter;
import steed.router.ModelDriven;
import steed.router.SimpleParamterFiller;
import steed.router.annotation.Path;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.exception.message.MessageRuntimeException;
import steed.router.processor.BaseProcessor;
import steed.util.AssertUtil;
import steed.util.base.BaseUtil;
import steed.util.base.StringUtil;
import steed.util.logging.Logger;
import steed.util.logging.LoggerFactory;
import steed.util.reflect.ReflectUtil;

public class APIParamterFiller extends SimpleParamterFiller {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	private HttpRouter router;
	
	private static Map<Class<? extends BaseProcessor>, ProcessorConfig> configCache = new HashMap<Class<? extends BaseProcessor>, ProcessorConfig>();

	public APIParamterFiller(HttpRouter router) {
		super();
		this.router = router;
		router.getPathProcessor().values().forEach((temp) -> {
			loadConfig(temp);
		});
		logger.debug("API配置加载完成:%s",new Gson().toJson(configCache));
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

	private Field getField(Class<? extends BaseProcessor> processor, String fieldName) {
		Field field = ReflectUtil.getDeclaredField(processor, fieldName);
		if (field == null && ModelDriven.class.isAssignableFrom(processor)) {
			try {
				Method method = processor.getMethod("getModel");
//				Type genericReturnType = method.getReturnType();
				field = ReflectUtil.getDeclaredField(method.getReturnType(), fieldName);
			} catch (NoSuchMethodException | SecurityException e1) {
				e1.printStackTrace();
			}
		}
		return field;
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

	@Override
	public void fillParamters2ProcessorData(BaseProcessor processor, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String methodName = router.getMethodName(request.getRequestURI());
		ProcessorConfig processorConfig = configCache.get(processor.getClass());
		Map<String, Parameter> parameters = null;
		if (processorConfig != null) {
			Api api = processorConfig.getApis().get(methodName);
			parameters = api.getParameters();
			parameters.entrySet().forEach((temp)->{
				validateParameter(request, temp.getKey(), temp.getValue());
			});
		}
		
		fillParamters2Data(parameters, processor, request, response);
    	if (processor instanceof ModelDriven<?>) {
			Object model = ((ModelDriven<?>) processor).getModel();
			if (model != null) {
				fillParamters2Data(parameters,model, request, response);
				((ModelDriven<Object>) processor).onModelReady(model);
			}
		}
	}

	private void validateParameter(HttpServletRequest request,String fieldName, Parameter rule) {
		String value = request.getParameter(fieldName);
		if (StringUtil.isStringEmpty(value)) {
			AssertUtil.assertTrue(!rule.isRequire(), "参数"+fieldName+"必传!");
		}else {
			validateParameterType(fieldName, rule, value);
		}
	}

	private void validateParameterType(String fieldName,Parameter rule,String value) {
		switch (rule.getType().toLowerCase()) {
		case "long":
		case "int":
			try {
				Long.parseLong(value);
			} catch (Exception e) {
				throw new MessageRuntimeException(fieldName+"必须为"+rule.getType()+"类型!");
			}
			break;
		case "string":

		default:
			break;
		}
	}

	protected void fillParamters2Data(Map<String, Parameter> parameters,Object container, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (parameters == null) {
			fillParamters2Data(container, request, response);
			return;
		}
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if (!parameters.containsKey(parameterName)) {
				logger.info("参数%s不在parameters中,不自动装配.",parameterName);
			}
			singleParamters2Data(container, request, parameterName);
		}
	}

	
}
