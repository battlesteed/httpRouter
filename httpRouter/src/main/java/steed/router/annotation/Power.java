package steed.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
/**
 * 权限管理，表明改action或方法需要什么权限
 * @author 战马
 * @email java@beyondstar.com.cn 
 *        battle_steed@163.com
 * @company 深圳市星超越科技有限公司
 */
public @interface Power {
	public static String logined = "logined";
	public String value();
	public int level() default 1;
}
