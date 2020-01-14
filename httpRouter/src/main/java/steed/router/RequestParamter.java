package steed.router;

public class RequestParamter {
	private String key;
	private String[] value;
	public RequestParamter(String key, String[] value) {
		super();
		this.key = key;
		this.value = value;
	}
	public RequestParamter() {
		super();
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String[] getValue() {
		return value;
	}
	public void setValue(String[] value) {
		this.value = value;
	}
	
}
