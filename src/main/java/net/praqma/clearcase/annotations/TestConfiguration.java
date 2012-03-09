package net.praqma.clearcase.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention( RetentionPolicy.RUNTIME )
public @interface TestConfiguration {
	String project() default "test-project";
	String[] component();
	String[] stream();
}
