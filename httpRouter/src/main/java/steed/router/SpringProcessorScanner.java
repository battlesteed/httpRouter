package steed.router;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.router.annotation.DontAccess;
import steed.router.annotation.Path;
import steed.router.processor.BaseProcessor;

public class SpringProcessorScanner implements ProcessorScanner{
	private static Logger logger = LoggerFactory.getLogger(SpringProcessorScanner.class);
	private String[] packages4Scan;
	
	public SpringProcessorScanner(String... packages4Scan) {
		super();
		this.packages4Scan = packages4Scan;
	}

	public Map<String, Class<? extends BaseProcessor>> scanProcessor() {
		Map<String, Class<? extends BaseProcessor>> pathProcessor = new HashMap<>();
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		provider.addIncludeFilter(new AnnotationTypeFilter(Path.class));
//		provider.addIncludeFilter(new AssignableTypeFilter(Processor.class));
//		provider.setResourcePattern("**/*.class");
		Set<BeanDefinition> findCandidateComponents;
		if (packages4Scan == null) {
			findCandidateComponents = provider.findCandidateComponents("");
		}else {
			findCandidateComponents = new HashSet<>();
			for(String temp:packages4Scan) {
				findCandidateComponents.addAll(provider.findCandidateComponents(temp));
			}
		}
		
		for (BeanDefinition temp:findCandidateComponents) {
			try {
				Class<? extends BaseProcessor> forName = (Class<? extends BaseProcessor>) Class.forName(temp.getBeanClassName());
				if(forName.getAnnotation(DontAccess.class) != null) {
					logger.info("类"+temp.getBeanClassName()+"含有DontAccess注解,跳过扫描");
					continue;
				}
				Path annotation = forName.getAnnotation(Path.class);
				String path = annotation.value();
				if (pathProcessor.containsKey(path)) {
					logger.warn("%s和%s的path均为%s,%s将被忽略!",pathProcessor.get(path).getName(),temp.getBeanClassName(),path,temp.getBeanClassName());
					continue;
				}
				pathProcessor.put(addSprit(path), forName);
			} catch (ClassNotFoundException | ClassCastException e) {
				logger.error("扫描Processor出错!",e);
			}
		}
		return pathProcessor;
	}

	private String addSprit(String path) {
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.endsWith("/")) {
			path = path + "/";
		}
		return path;
	}
}