package net.praqma.clearcase.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention( RetentionPolicy.RUNTIME )
public @interface TestConfiguration {
	String project() default "test-project";
	String pvob() default "TestPVOB";
	//String path() default "cc-test";
}
