package steed.router.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steed.router.HttpRouter;
import steed.util.base.BaseUtil;

@WebServlet(urlPatterns="*.ru")
public class TestServlet extends HttpServlet{
	public static final HttpRouter httpRouter = new HttpRouter() {

		@Override
		protected boolean checkPower(HttpServletRequest request, HttpServletResponse response, String uri,
				String power) {
			BaseUtil.getLogger().debug("检测权限{},uri:{}",power,uri);
			return super.checkPower(request, response, uri, power);
		}
	};
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		httpRouter.forward(req, resp);
	}

}
