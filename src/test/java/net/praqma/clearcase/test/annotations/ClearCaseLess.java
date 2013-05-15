package net.praqma.clearcase.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotating a method with ClearCaseLess will disable the ClearCase setup and teardown.
 * 
 * @author wolfgang
 *
 */
@Retention( RUNTIME )
@Target( { METHOD } )
public @interface ClearCaseLess {
}