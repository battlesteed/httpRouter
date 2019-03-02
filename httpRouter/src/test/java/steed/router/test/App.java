package steed.router.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;

//@EnableAutoConfiguration
@EnableAutoConfiguration(exclude={JtaAutoConfiguration.class})
@Configuration
@ServletComponentScan(basePackages = "steed")
public class App{
	
	/*@Bean
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		UserDetails user =  User.withUsername("ifs").password("password").roles("USER").build();
		return new InMemoryUserDetailsManager(Arrays.asList(user));
	}*/

	public static void main(String[] args) throws Exception {
		SpringApplication.run(App.class, args);
	}

}
