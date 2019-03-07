package steed.router;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

public class SimpleHttpRouter extends HttpRouter{
	private Gson gson = new Gson();
	
	@Override
    protected boolean checkPower(HttpServletRequest request,HttpServletResponse response,String uri,String power) {
    	return true;
    }

	@Override
	protected String object2Json(Object object) {
		return gson.toJson(object);
	}
}
