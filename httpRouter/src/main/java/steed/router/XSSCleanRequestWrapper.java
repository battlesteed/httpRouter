package steed.router;

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
	public Map<String, String[]> getParameterMap() {
		if (initedMap) {
			return map;
		}
		Map<String, String[]> tempMap = super.getParameterMap();
		for (String key:tempMap.keySet()) {
			if (map.containsKey(key)) {
				continue;
			}
			map.put(key, xssCleaner.clean(tempMap.get(key), key));
		}
		initedMap = true;
		return map;
	}

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
