package steed.router.api.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Api {
	private String desc;
	private String name;
	/**
	 * 无需在json文件配置,自动填充
	 */
	private String path;
	private Map<String, Parameter> parameters;
	private String[] removeParameters;
	
	private List<ReturnVal> returns = new ArrayList<ReturnVal>();
	
	private String returnSample;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
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
	
	public List<ReturnVal> getReturns() {
		return returns;
	}
	
	public void setReturns(List<ReturnVal> returns) {
		this.returns = returns;
	}
	
	public String getReturnSample() {
		return returnSample;
	}
	
	public void setReturnSample(String returnSample) {
		this.returnSample = returnSample;
	}
	
}
