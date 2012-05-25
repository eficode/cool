package net.praqma.clearcase.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.clearcase.util.SetupUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ComponentTest extends CoolTestCase {
	
	private static String uniqueTestVobName = "cool" + getUniqueTimestamp();
	
	@BeforeClass
	public static void startup() throws Exception {
		uniqueTestVobName = "cool" + getUniqueTimestamp();
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
	}
	
	@AfterClass
	public static void end() throws CleartoolException {
		if( pvob != null ) {
			try {
				SetupUtils.tearDown( pvob );
			} catch( CleartoolException e ) {
				ExceptionUtils.print( e, System.out, true );
			}
		} else {
			/* Not possible to tear down */
		}
	}

	@Test
	public void testLoad() throws Exception {
		Component model = context.components.get( "Model" ).load();
	}
	
	@Test
	public void testCreate() throws Exception {
		File view = new File( context.mvfs + "/" + uniqueTestVobName + "_one_int/" + uniqueTestVobName );
		Component mycomp = Component.create( "new-component", pvob, "Praqma", "my comment", view );
		
		assertNotNull( mycomp );
		assertEquals( "new-component", mycomp.getShortname() );
	}
	
	@Test
	public void testGetRootDir() throws Exception {
		Component model = context.components.get( "Model" );
		
		String root = new File( "/" + uniqueTestVobName + "/" + "Model" ).getName();
		System.out.println( "ROOT: " + root );
		assertEquals( root, model.getRootDir() );
	}
	
	@Test
	public void testGet() throws Exception {
		Component model = Component.get( "Model@\\" + uniqueTestVobName + "_PVOB" );
		
		assertNotNull( model );
	}


}
