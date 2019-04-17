package steed.router;

public interface JsonSerializer {
	/**
     * java对象转json,用于给客户端返回json数据
     * @param object 要转换的json对象
     * @return json字符串
     */
	String object2Json(Object object);
}
