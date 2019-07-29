package steed.router;

import javax.servlet.http.HttpServletRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.safety.Whitelist;

import steed.ext.util.base.StringUtil;

public class SimpleXSSCleanner implements XSSCleaner{
	
	/**
	 * 允许存在&lt;,/,&gt;等特殊符号的参数名(富文本字段),
	 */
	protected String[] allowedSpecialCharParams = new String[] {};
	/**
	 * 不进行xss过滤的参数名
	 */
	protected String[] paramNotClean = new String[] {};
	private static final String[] strNotArrow = {"^[\\s\\S]*expression([\\s\\S]*)[\\s\\S]*$","^[\\s\\S]*javascript:[\\s\\S]*$"};		
	/**
	 * 网站基本路径
	 */
	private String baseUri;
	/**
	 * 是否trim传过来的参数,用户有时会在输入框前后输入空格,回车等,若设为true,则会自动去除这些空格
	 */
	protected boolean trimParam = true;
	
	public String[] getAllowedSpecialCharParams() {
		return allowedSpecialCharParams;
	}

	public void setAllowedSpecialCharParams(String[] allowedSpecialCharParams) {
		this.allowedSpecialCharParams = allowedSpecialCharParams;
	}

	public String[] getParamNotClean() {
		return paramNotClean;
	}

	public void setParamNotClean(String[] paramNotClean) {
		this.paramNotClean = paramNotClean;
	}

	public String getBaseUri() {
		return baseUri;
	}

	public void setBaseUri(String baseUri) {
		this.baseUri = baseUri;
	}
	
	
	@Override
	public String[] clean(String[] param,String name){
		for (String temp:paramNotClean) {
			if (temp.equals(name)) {
				return param;
			}
		}
		
		boolean allowSpecialCharacter = false;
		for (String temp:allowedSpecialCharParams) {
			if (temp.equals(name)) {
				allowSpecialCharacter = true;
				break;
			}
		}
		for (int i = 0; i < param.length; i++) {
			param[i] = clean(param[i], allowSpecialCharacter);
		}
		
		return param;
	}
	
	/**
	 * 清除富文本内容中的跨站脚本攻击字符,
	 * 如果不是富文本请用trensferrSpecailCharacter
	 * @param str
	 * @return
	 */
	public String cleanXss(String str) {
		Whitelist relaxed = Whitelist.relaxed();
		addWhitelist(relaxed);
//		relaxed.addAttributes("iframe", "src");
//		relaxed.addAttributes("iframe", "height");
//		relaxed.addAttributes("iframe", "width");
//		relaxed.addTags("iframe");
		/*
		 * String baseUri = this.baseUri; if (StringUtil.isStringEmpty(baseUri)) {
		 * HttpServletRequest request = HttpRouter.getRequest(); baseUri =
		 * request.getScheme()+"://"+ request.getServerName() +":"+
		 * request.getServerPort() + request.getContextPath()+"/"; }
		 */
		String clean = Jsoup.clean(str, relaxed);
		Document doc = Jsoup.parse(clean);
		
		validateNode(doc);
		return doc.body().html();
	}

	/**
	 * 添加白名单
	 * @param relaxed
	 */
	public void addWhitelist(Whitelist relaxed) {
		relaxed.addAttributes(":all", "style");
		relaxed.addAttributes(":all", "class");
		relaxed.addTags("meta");
		relaxed.addAttributes("meta", "name");
		relaxed.addAttributes("meta", "content");
	}
	
	private void validateNode(Node n){
		for (Node node:n.childNodes()) {
			for(Attribute a:node.attributes().asList()){
				String value = a.getValue();
				for (String str:strNotArrow) {
					if (value.matches(str)) {
						node.removeAttr(a.getKey());
						break;
					}
				}
			}
			validateNode(node);
		}
	}
	
	
	public String clean(String target,boolean allowSpecialCharacter){
		if (StringUtil.isStringEmpty(target)) {
			return target;
		}
		if (trimParam) {
			target = target.trim();
		}
		if (allowSpecialCharacter) {
			return cleanXss(target);
		}else {
			return StringUtil.transferrCharacter((target).replace("'", "＇"));
		}
	}
}
