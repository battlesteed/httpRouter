package steed.util;

import steed.ext.util.base.StringUtil;
import steed.router.domain.Message;
import steed.router.exception.message.MessageRuntimeException;

public class AssertUtil {

	/**
	 * 更加简洁的数据校验方法,若 !yourAssert ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param yourAssert
	 * @param message    
	 * @param statusCode
	 */
	public static void assertTrue(boolean yourAssert, String message, int statusCode) {
		assertTrue(yourAssert, message, statusCode, null);
	}
	public static void assertTrue(boolean yourAssert, String message, int statusCode,Object content) {
		if (!yourAssert) {
			throw new MessageRuntimeException(new Message(statusCode, message, content));
		}
	}

	/**
	 * 更加简洁的数据校验方法,若 !yourAssert ,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param yourAssert
	 * @param message    
	 */
	public static void assertTrue(boolean yourAssert, String message) {
		assertTrue(yourAssert, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted为空或null ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 * @param statusCode
	 */
	public static void assertNotEmpty(String asserted, String message, int statusCode) {
		assertTrue(!StringUtil.isStringEmpty(asserted), message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted为空或null ,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertNotEmpty(String asserted, String message) {
		assertNotEmpty(asserted, message, Message.statusCode_MessageExecption);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted不为空且不为null ,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertEmpty(String asserted, String message) {
		assertEmpty(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted不为空且不为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertEmpty(String asserted, String message, int statusCode) {
		assertTrue(StringUtil.isStringEmpty(asserted), message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertNotNull(Object asserted, String message) {
		assertNotNull(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted为null,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertNotNull(Object asserted, String message, int statusCode) {
		assertTrue(asserted != null, message, statusCode);
	}

	/**
	 * 更加简洁的数据校验方法,若 asserted不为null,则会抛出 new MessageRuntimeException({@link Message#statusCode_MessageExecption}, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertNull(Object asserted, String message) {
		assertNull(asserted, message, Message.statusCode_MessageExecption);
	}
	/**
	 * 更加简洁的数据校验方法,若 asserted不为null,则会抛出 new MessageRuntimeException(statusCode, message);
	 * 框架会自动把相关信息转成json返回给客户端
	 * 
	 * @param asserted
	 * @param message    
	 */
	public static void assertNull(Object asserted, String message, int statusCode) {
		assertTrue(asserted == null, message, statusCode);
	}

}
