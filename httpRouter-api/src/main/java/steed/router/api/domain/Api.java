package steed.router.api.domain;

import java.util.HashMap;
import java.util.Map;

public class Api {
	private String desc;
	private Map<String, Parameter> parameters;
	private String[] removeParameters;
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
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
