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

import org.springframework.beans.factory.annotation.Autowired;

import steed.router.HttpRouter;

@WebFilter(urlPatterns="/*")
public class RouterFilter extends HttpFilter{
	
	@Autowired
	public HttpRouter httpRouter;

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
