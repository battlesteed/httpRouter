package steed.router.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import steed.router.api.domain.Parameter;

/**
 * 路径,表明该Processor对应哪个路径
 * @author 战马 battle_steed@qq.com
 */
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocParam {
	public String value() default "";
	public int maxLength() default -1;
	public int minLength() default -1;
	public boolean require() default Parameter.defaultRequire;
}
