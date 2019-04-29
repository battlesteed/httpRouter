package steed.router.api.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import steed.router.api.SimpleAPIConfigLoader;
import steed.router.api.domain.ProcessorConfig;
import steed.router.processor.BaseProcessor;
import steed.util.base.BaseUtil;
import steed.util.base.PathUtil;
import steed.util.base.StringUtil;

public class SimpleDocumentGenerator implements DocumentGenerator{
	private String templatePath;
	private String destPath;
	
	public SimpleDocumentGenerator() {
		super();
		templatePath = BaseUtil.getResourceURL("docTemplate/").getFile();
		destPath = BaseUtil.getResourceURL("generatedDocument/").getFile();
	}

	@Override
	public void generate(Map<String, Class<? extends BaseProcessor>> processors) {
		Map<Class<? extends BaseProcessor>, ProcessorConfig> loadProcessorsConfig = new SimpleAPIConfigLoader().loadProcessorsConfig(processors);
		
		loadProcessorsConfig.entrySet().forEach((temp)->{
			ProcessorConfig processorConfig = temp.getValue();
			String path = processorConfig.getPath();
			if (!StringUtil.isStringEmpty(path) && !processorConfig.getApis().isEmpty()) {
				generate(processorConfig);
			}
		});
	}
	
	protected void replaceMark(String line,ProcessorConfig processorConfig,Object target) {
		Pattern p = Pattern.compile("{{\\S+}}");  
        Matcher m = p.matcher(line);
//        m.
	}
	
	protected void generate(ProcessorConfig processorConfig) {
		StringBuffer sb = new StringBuffer();
		File docTemplate = getDocTemplate();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(docTemplate),"UTF-8"));
			String readLine = bufferedReader.readLine();
			while (readLine != null) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("读取processor模板文件失败!",e);
		}finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
//		processorConfig. 
	}

	
	protected File getDocTemplate() {
		return new File(PathUtil.mergePath(templatePath, "processor.txt"));
	}
	 
	
}
