package steed.router.test;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import steed.router.HttpRouter;
import steed.router.SimpleHttpRouter;
import steed.util.base.BaseUtil;

@WebFilter(urlPatterns="/*")
public class TestFilter extends HttpFilter{
	public static final HttpRouter httpRouter = new SimpleHttpRouter() {

		@Override
		protected boolean checkPower(HttpServletRequest request, HttpServletResponse response, String uri,
				String power) {
			BaseUtil.getLogger().debug("检测权限%s,uri:%s",power,uri);
			return super.checkPower(request, response, uri, power);
		}
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String requestURI = ((HttpServletRequest)request).getRequestURI();
		if (requestURI.endsWith(".ru") || requestURI.endsWith("/") || !requestURI.contains(".")) {
			httpRouter.forward((HttpServletRequest)request, (HttpServletResponse) response);
		}else {
			super.doFilter(request, response, chain);
		}
	}

}
