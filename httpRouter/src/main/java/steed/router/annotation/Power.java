package steed.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限注解，表明该Processor或方法需要什么权限
 * @author 战马
 *        battle_steed@qq.com
 */
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Power {
	public static String logined = "logined";
	public String value();
	public int level() default 1;
}
