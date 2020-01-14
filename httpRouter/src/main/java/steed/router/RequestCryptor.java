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
	 * 响应body加密(仅当返回body类型为json时才会调用该方法)
	 * @param payload 未加密的原始payload (响应body)
	 * @param request
	 * @param response
	 * @return 加密后的 payload
	 */
	public default String encryptPayload(String payload, HttpServletRequest request, HttpServletResponse response) {
		return payload;
	}
	
}
