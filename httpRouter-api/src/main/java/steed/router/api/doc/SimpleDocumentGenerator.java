package steed.router.api.doc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import steed.ext.util.base.BaseUtil;
import steed.ext.util.base.PathUtil;
import steed.ext.util.base.StringUtil;
import steed.ext.util.logging.Logger;
import steed.ext.util.logging.LoggerFactory;
import steed.ext.util.reflect.ReflectUtil;
import steed.router.api.SimpleAPIConfigLoader;
import steed.router.api.domain.Api;
import steed.router.api.domain.Parameter;
import steed.router.api.domain.ProcessorConfig;
import steed.router.api.domain.Summary;
import steed.router.processor.BaseProcessor;

public class SimpleDocumentGenerator implements DocumentGenerator{
	private static Logger logger = LoggerFactory.getLogger(SimpleDocumentGenerator.class);
	private String templatePath;
	private String destPath;
	private String charset = "UTF-8";
	private StringBuffer menus = new StringBuffer();
	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public SimpleDocumentGenerator() {
		super();
		templatePath = PathUtil.mergePath(BaseUtil.getResourceURL("").getFile(), "docTemplate/").replace("test-classes", "classes");
		destPath = PathUtil.mergePath(BaseUtil.getResourceURL("").getFile(), "generatedDocument/").replace("test-classes", "classes");
	}
	
	public SimpleDocumentGenerator(String templatePath, String destPath) {
		super();
		this.templatePath = templatePath;
		this.destPath = destPath;
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
		
		Summary summary = new Summary();
		summary.setMenus(menus.toString());
		
		StringBuffer readTemplateFile = readTemplateFile(null, summary, "SUMMARY.md");
		
		writeDoc(readTemplateFile, getDocDest("SUMMARY.md"));
	}
	
	protected StringBuffer replaceMark(String line,ProcessorConfig processorConfig,Object target) {
		//logger.debug(line);
		Pattern p = Pattern.compile("\\{\\{(\\S+)\\}\\}");  
        Matcher m = p.matcher(line);
        StringBuffer sb = new StringBuffer() ;
        while (m.find()) {
        	String group = m.group(1);
        	m.appendReplacement(sb, dealMark(group, processorConfig, target));
		}
        m.appendTail(sb);
        return sb;
	}
	
	protected String dealMark(String group,ProcessorConfig processorConfig,Object target) {
		Object fieldValueByGetter = ReflectUtil.getFieldValueByGetter(target, group);
    	if (fieldValueByGetter != null) {
			if (fieldValueByGetter instanceof String) {
				return(String)fieldValueByGetter;
			}else if("apis".equals(group)) {
				Map<String, Api> apis = (Map<String, Api>) fieldValueByGetter;
				StringBuffer apisDoc = new StringBuffer();
				apis.forEach((key,value)->{
					apisDoc.append(readTemplateFile(processorConfig, value, "api.md"));
				});
				return apisDoc.toString();
			}else if ("parameters".equals(group)) {
				Map<String, Parameter> parameters = (Map<String, Parameter>) fieldValueByGetter;
				StringBuffer parameterDoc = new StringBuffer();
				parameters.forEach((key,value)->{
					parameterDoc.append(readTemplateFile(processorConfig, value, "parameter.md"));
				});
				return parameterDoc.toString();
			}else if(fieldValueByGetter instanceof Boolean) {
				return (boolean)fieldValueByGetter ? "是":"否";
			}
		}
		return "";
	}
	
	protected void generate(ProcessorConfig processorConfig) {
		StringBuffer doc = readTemplateFile(processorConfig, processorConfig, "processor.md");
		String path = processorConfig.getPath();
		if (path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		String docName = path+".md";
		File docDest = getDocDest(docName);
		if (docDest != null) {
			writeDoc(doc, docDest);
			logger.debug("生成文档%s成功",docName);
		}
		
		
		menus.append(String.format("* [%s](%s)", processorConfig.getName(),docName)).append("\n");
		
//		processorConfig. 
	}

	private void writeDoc(StringBuffer doc, File docDest) {
		OutputStreamWriter out = null;
		try {
			out = new OutputStreamWriter(new FileOutputStream(docDest), charset);
			BufferedWriter writer = new BufferedWriter(out);
			writer.write(doc.toString().replaceAll("```\\s+```", ""));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private StringBuffer readTemplateFile(ProcessorConfig processorConfig, Object target, String templateName) {
		BufferedReader bufferedReader = null;
		try {
			File docTemplate = getDocTemplate(templateName);
			StringBuffer sb = new StringBuffer();
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(docTemplate), charset));
			String readLine = bufferedReader.readLine();
			while (readLine != null) {
				sb.append(replaceMark(readLine, processorConfig, target)).append("\n");
				readLine = bufferedReader.readLine();
			}
			//logger.debug("生成文档-->\n"+sb);
			return sb;
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
	}

	protected File getDocDest(String docName) {
		File lockFile = new File(PathUtil.mergePath(destPath, docName+".lock"));
		if (lockFile.exists()) {
			return null;
		}
		File docDest = new File(PathUtil.mergePath(destPath, docName));
		if (docDest.exists()) {
			docDest.delete();
		}
		docDest.mkdirs();
		docDest.delete();
		return docDest;
	}
	
	protected File getDocTemplate(String templateName) {
		return new File(PathUtil.mergePath(templatePath, templateName));
	}
	 
	
}
