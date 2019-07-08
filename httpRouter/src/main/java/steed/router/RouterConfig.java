package steed.router;

import com.google.gson.Gson;

public class RouterConfig {
	public static boolean devMode = false;
	public final static String steed_forward = "steed_forward";
	public static String message_page = "message.jsp";
	/**
	 * 放到request域的异常key
	 */
	public final static String exceptionAttributeKey = "exception";
	/**
	 * 放到request域的提示信息key
	 */
	public final static String messageAttributeKey = "exMessage";
	
	public static int NOT_FOUND_StATUS_CODE = 404;
	/**
	 * 安全问题,不用403,否则类名,字段名,方法名等会被扫描到
	 */
	public static int FORBIDDEN_STATUS_CODE = 404;
	
	public static String charset = "UTF-8";
	
	public static String jspPath = "/WEB-INF/jsp/";
	
	public static JsonSerializer defaultJsonSerializer = (obj)->{ return new Gson().toJson(obj);};
	
	public static XSSCleaner defaultXSSCleaner = new SimpleXSSCleanner();
}
