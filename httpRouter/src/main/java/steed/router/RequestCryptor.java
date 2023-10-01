package steed.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * http请求加密解密器,可以由客户端加密url,参数等等,再由本加解密器解密,大大增加抓包逆向难度
 * 
 * @author battlesteed
 *
 */
public interface RequestCryptor {
	/**
	 * 解密request传过来的参数名和参数值
	 * @param paramterName 参数名
	 * @param value 参数值,大多数情况,长度为1
	 * @param request 
	 * @return 解密后的参数值和参数名
	 */
	public default RequestParamter decryptParamter(String paramterName,String[] value,HttpServletRequest request) {
		return new RequestParamter(paramterName, value);
	}
	
	/**
	 * 解密url(不包括?后的参数)
	 * @param uri 客户端加密后的url 不包括?后的参数
	 * @param request
	 * @return 解密后的url,不包括?后的参数
	 */
	public default String decryptUrl(String uri,HttpServletRequest request) {
		return uri;
	}
	/**
	 * 判断当前请求是否需要加解密,如:可以通过uri.contains("/api/")来只加密/api/*路径的请求
	 * @param uri
	 * @param request
	 * @return
	 */
	public default boolean shouldIWork(HttpServletRequest request) {
		return true;
	}
	
	/**
	 * 响应body加密(仅当返回body类型为json时才会调用该方法)
	 * @param payload 未加密的原始payload (响应body)
	 * @param request
	 * @param response
	 * @return 加密后的 payload
	 */
	public default String encryptPayload(String payload, HttpServletRequest request, HttpServletResponse response) {
		return payload;
	}
//	/**
//	 *   多线程(多个http请求)操作可能会出错,所以每个请求都会new一个RequestCryptor,
//	 *   如果该类兼容多线程的话,可以重写该方法,直接返回this,提升性能
//	 * @param payload
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	public default RequestCryptor newInstance() {
//		try {
//			return getClass().newInstance();
//		} catch (Exception e) {
//			throw new RuntimeException("自动创建实例失败,请参照文档重写steed.router.RequestCryptor.newInstance(String, HttpServletRequest, HttpServletResponse)方法", e);
//		}
//	}
	
}
