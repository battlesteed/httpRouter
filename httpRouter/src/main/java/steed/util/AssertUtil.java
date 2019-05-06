package steed.util;

import steed.ext.util.base.StringUtil;
import steed.router.domain.Message;
import steed.router.exception.message.MessageRuntimeException;

public class AssertUtil {
	
	/**
	 * 更加简洁的数据校验方法
	 * 
	 * @param yourAssert 你的断言
	 * @param message 若 !yourAssert ,则会抛出 new MessageRuntimeException(message);
	 */
	public static void assertTrue(boolean yourAssert,String message,int statusCode){
		if (!yourAssert) {
			throw new MessageRuntimeException(statusCode,message);
		}
	}
	
	public static void assertTrue(boolean yourAssert,String message){
		assertTrue(yourAssert, message, Message.statusCode_UnknownError);
	}
	public static void assertNotEmpty(String asserted,String message,int statusCode){
}
	public static void assertNotEmpty(String asserted,String message){
		assertTrue(!StringUtil.isStringEmpty(asserted), message);
	}
	public static void assertEmpty(String asserted,String message){
		assertTrue(StringUtil.isStringEmpty(asserted), message);
	}
	public static void assertEmpty(String asserted,String message,int statusCode){
		assertTrue(StringUtil.isStringEmpty(asserted), message,statusCode);
	}
	public static void assertNotNull(Object asserted,String message){
		assertNotNull(asserted, message, Message.statusCode_UnknownError);
	}
	public static void assertNotNull(Object asserted,String message,int statusCode){
		assertTrue(asserted != null, message,statusCode);
	}
	public static void assertNull(Object asserted,String message){
		assertTrue(asserted == null, message);
	}
	public static void assertNull(Object asserted,String message,int statusCode){
		assertTrue(asserted == null, message,statusCode);
	}
	
}
