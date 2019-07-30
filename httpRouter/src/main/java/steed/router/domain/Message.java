package steed.router.domain;

import java.util.HashMap;
import java.util.Map;

import steed.hibernatemaster.domain.BaseDomain;
/**
 * 前端提示消息实体类
 * @author 战马
 *
 */
public class Message extends BaseDomain{
	private static final long serialVersionUID = 7956968695852861290L;
	public static final int statusCodeSuccess = 0;
	/**
	 * 通用异常状态码
	 */
	public static final int statusCode_UnknownError = 300;
	/**
	 * 消息提示状态码
	 */
	public static final int statusCode_MessageExecption = 301;
	private Integer statusCode;
	private String message;
	private String url;
	private String title;
	private Object content;
	
	public Message(Integer statusCode,String message, String title) {
		this.statusCode = statusCode;
		this.message = message;
		this.title = title;
	}

	public Message(Integer statusCode, String message, String url, String title) {
		this.statusCode = statusCode;
		this.message = message;
		this.url = url;
		this.title = title;
	}
	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Message() {
		this(statusCodeSuccess, "成功!");
	}
	public Message(String message) {
		this(statusCodeSuccess, message);
	}
	
	public Message(Object content) {
		this();
		this.content = content;
	}
	
	public Message(String message, Object content) {
		this(message);
		this.content = content;
	}
	
	public Message(Integer statusCode, String message, Object content) {
		super();
		this.statusCode = statusCode;
		this.message = message;
		this.content = content;
	}

	public Message(Integer statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	public boolean isSuccess() {
		return statusCode == statusCodeSuccess;
	}
	
	/**
	 * 往content添加数据,调用该方法之前必须保证content为null或Map&lt;String, Object&gt;,如下写法<pre>{@code
	 * new Message().addContent("foo", 1).addContent("bar", "bar321");
	 * }</pre>
	 * @param key
	 * @param value
	 * @return this,方便做链式写法
	 */
	public Message addContent(String key,Object value) {
		getContentMap().put(key, value);
		return this;
	}
	
	private Map<String, Object> getContentMap(){
		if (content == null) {
			content = new HashMap<String, Object>();
		}
		return (Map<String, Object>) content;
	}
	
}
