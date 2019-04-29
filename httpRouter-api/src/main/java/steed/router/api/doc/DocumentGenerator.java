package steed.router.api.doc;

import java.util.Map;

import steed.router.processor.BaseProcessor;

public interface DocumentGenerator {
	void generate(Map<String, Class<? extends BaseProcessor>> processors);
}
