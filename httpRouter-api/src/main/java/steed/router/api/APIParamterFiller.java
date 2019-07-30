package steed.router.api;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.router.HttpRouter;
import steed.router.ModelDriven;
import steed.router.SimpleParamterFiller;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.exception.message.MessageRuntimeException;
import steed.router.processor.BaseProcessor;
import steed.util.AssertUtil;

public class APIParamterFiller extends SimpleParamterFiller {
	private static Logger logger = LoggerFactory.getLogger(APIParamterFiller.class);
	
	private static Map<Class<? extends BaseProcessor>, ProcessorConfig> configCache = new HashMap<Class<? extends BaseProcessor>, ProcessorConfig>();

	private HttpRouter router;
	
	public APIParamterFiller(HttpRouter router) {
		super();
		this.router = router;
		Map<String, Class<? extends BaseProcessor>> pathProcessor = router.getPathProcessor();
		configCache = RouterApiConfig.APIConfigLoader.loadProcessorsConfig(pathProcessor);
		logger.debug("API配置加载完成:%s",new Gson().toJson(configCache));
	}
	
	@Override
	public void fillParamters2ProcessorData(BaseProcessor processor, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String methodName = router.getMethodName(request.getRequestURI());
		ProcessorConfig processorConfig = configCache.get(processor.getClass());
		Map<String, Parameter> parameters = null;
		if (processorConfig != null) {
			Api api = processorConfig.getApis().get(methodName);
			AssertUtil.assertNotNull(api, String.format("处理器%s没有关于api %s的配置,不允许访问!", processor.getClass().getName(),methodName));
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
		if (StringUtil.isStringEmpty(rule.getType())) {
			return;
		}
		switch (rule.getType().toLowerCase()) {
		case "long":
		case "int":
		case "integer":
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
				continue;
			}
			singleParamters2Data(container, request, parameterName);
		}
	}
	
}
