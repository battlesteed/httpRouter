package steed.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
/**
 * 路径,表明该action对应哪个路径
 * @author 战马 battle_steed@qq.com
 */
public @interface Path {
	public String value();


}
