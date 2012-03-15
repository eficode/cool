package net.praqma.clearcase.test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.annotations.TestConfiguration;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;

import junit.framework.TestCase;

public abstract class CoolTestCase extends TestCase {
	
	protected static boolean rolling = true;
	
	protected PVob pvob;
	protected boolean well = true;
	
	@BeforeClass
	public static void clearcaseSetup() {
		
	}
	
	@Override
	protected void setUp() {
		System.out.println( "Setup" );
		TestConfiguration config = getClass().getAnnotation( TestConfiguration.class );
		String project = config.project();
		String pvob = config.pvob();
		
		try {
			this.pvob = (PVob) PVob.create( pvob, true, null, "testing" );
		} catch( CleartoolException e ) {
			e.print( System.err );
			well = false;
		}
		
		System.out.println( "Project: " + project );
	}
	
	@Override
    protected void runTest() throws Throwable {
    	System.out.println( "runTest!" );
    	super.runTest();
    }
    

    @Override
	public void runBare() throws Throwable {
    	System.out.println( "runBare!" );
        Thread t = Thread.currentThread();
        String o = getClass().getName()+'.'+t.getName();
        t.setName("Executing "+getName());
        try {
            super.runBare();
        } finally {
            t.setName(o);
        }
    }
    
    @Override
    protected void tearDown() {
    	System.out.println( "DOWN!" );
    	
    	try {
			pvob.remove();
		} catch( CleartoolException e ) {
			e.print( System.err );
		}
    }
}
