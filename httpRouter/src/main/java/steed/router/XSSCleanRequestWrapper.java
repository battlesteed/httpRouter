package steed.router;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class XSSCleanRequestWrapper extends HttpServletRequestWrapper{
	private boolean initedMap = false;
	private Map<String, String[]> map = new HashMap<String, String[]>();
	
	private XSSCleaner xssCleaner = RouterConfig.defaultXSSCleaner;

	public XSSCleanRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		Map<String, String[]> parameterMap = getParameterMap();
		return Collections.enumeration(parameterMap.keySet());
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		if (initedMap) {
			return map;
		}
		Map<String, String[]> tempMap = super.getParameterMap();
		for (String key:tempMap.keySet()) {
			if (map.containsKey(key)) {
				continue;
			}
			String[] value = tempMap.get(key);
			if (RouterConfig.requestCryptor != null) {
				RequestParamter decrypt = RouterConfig.requestCryptor.decryptParamter(key, value, this);
				value = decrypt.getValue();
				key = decrypt.getKey();
			}
			if (key != null && value != null) {
				value = xssCleaner.clean(value, key);
			}
			map.put(key, value);
		}
		initedMap = true;
		return map;
	}
	
//	private void config() {
//		
//	}
//
//	@Override
//	public Collection<Part> getParts() throws IOException, ServletException {
//		ServletRequest request = getRequest();
//		if (request instanceof Request) {
//			Request r = (Request) request;
//		}
//		return super.getParts();
//	}
//
//	@Override
//	public Part getPart(String name) throws IOException, ServletException {
//		// TODO Auto-generated method stub
//		return super.getPart(name);
//	}

	@Override
	public String getParameter(String name) {
		String[] temp = getParameterValues(name);
		if (temp == null || temp.length < 1) {
			return null;
		}
		return temp[0];
	}
	
	
	@Override
	public String[] getParameterValues(String name) {
		String[] strings = map.get(name);
		if (strings == null) {
			strings = super.getParameterValues(name);
			if (strings == null) {
				return strings;
			}
			map.put(name, xssCleaner.clean(strings, name));
		}
		return strings;
	}
}
