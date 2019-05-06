package steed.router.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import steed.ext.util.logging.LoggerFactory;
import steed.router.HttpRouter;
import steed.router.SpringHttpRouter;

//@EnableAutoConfiguration
@EnableAutoConfiguration(exclude={JtaAutoConfiguration.class})
@Configuration
@ServletComponentScan(basePackages = "steed")
@SpringBootApplication
public class App{
	
	/*@Bean
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		UserDetails user =  User.withUsername("ifs").password("password").roles("USER").build();
		return new InMemoryUserDetailsManager(Arrays.asList(user));
	}*/

	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}
	
	@Bean
	public HttpRouter getRouter() {
		try {
			return new SpringHttpRouter("steed") {
				
				@Override
				protected boolean checkPower(HttpServletRequest request, HttpServletResponse response, String uri,
						String power) {
					LoggerFactory.getLogger().debug("检测权限%s,uri:%s",power,uri);
					return super.checkPower(request, response, uri, power);
				}
			};
		} catch (Error e) {
			e.printStackTrace();
		}
		return null;
	}

}
