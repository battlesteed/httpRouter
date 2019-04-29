package steed.router.api;

import java.util.Map;

import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;

public interface APIConfigLoader {

	Map<Class<? extends BaseProcessor>, ProcessorConfig> loadProcessorsConfig(Map<String, Class<? extends BaseProcessor>> pathProcessor);

}