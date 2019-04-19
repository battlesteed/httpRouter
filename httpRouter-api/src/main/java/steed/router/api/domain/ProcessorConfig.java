package steed.router.api.domain;

import java.util.HashMap;
import java.util.Map;

public class ProcessorConfig {
	private String path;
	private String desc;
	private Map<String, Parameter> parameters;
	private String[] removeParameters;
	private Map<String, Api> apis;
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Map<String, Api> getApis() {
		if (apis == null) {
			apis = new HashMap<String, Api>();
		}
		return apis;
	}
	public void setApis(Map<String, Api> apis) {
		this.apis = apis;
	}
	public Map<String, Parameter> getParameters() {
		if (parameters == null) {
			parameters = new HashMap<String, Parameter>();
		}
		return parameters;
	}
	public void setParameters(Map<String, Parameter> parameters) {
		this.parameters = parameters;
	}
	public String[] getRemoveParameters() {
		return removeParameters;
	}
	public void setRemoveParameters(String[] removeParameters) {
		this.removeParameters = removeParameters;
	}
}
