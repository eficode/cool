package net.praqma.clearcase.test;

import java.lang.annotation.Annotation;

import net.praqma.clearcase.annotations.TestConfiguration;
import junit.framework.TestCase;

public abstract class CoolTestCase extends TestCase {
	
	public void clearcaseSetup() {
		
	}
	
	protected void setUp() {
		System.out.println( "YEAH! " + getClass().getAnnotations() );
		TestConfiguration config = getClass().getAnnotation( TestConfiguration.class );
		String project = config.project();
		System.out.println( "Project: " + project );
	}
	
    protected void runTest() throws Throwable {
    	System.out.println( "BAM!" );
    	super.runTest();
    }
    


    public void runBare() throws Throwable {
    	System.out.println( "WACK!" );
        Thread t = Thread.currentThread();
        String o = getClass().getName()+'.'+t.getName();
        t.setName("Executing "+getName());
        try {
            super.runBare();
        } finally {
            t.setName(o);
        }
    }
}
