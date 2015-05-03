package ph.hatch.ddd.oe.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
public @interface OverrideName {

    String name() default "";

}
