package steed.router;

import java.util.Map;

import steed.router.processor.BaseProcessor;

public interface ProcessorScanner {
	Map<String, Class<? extends BaseProcessor>> scanProcessor();
}
