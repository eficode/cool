package net.praqma.clearcase.test;

import java.io.File;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.clearcase.util.SetupUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaselineTest extends CoolTestCase {
	
	private static String uniqueTestVobName = "cool" + getUniqueTimestamp();
	
	@BeforeClass
	public static void startup() throws Exception {
		//uniqueTestVobName = "cool" + getUniqueTimestamp();
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
	public void testLoadAndPromotionLevel() throws Exception {
		Baseline bl = context.baselines.get( "client-3" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
	}
	
	@Test
	public void testCreateBaseline() throws Exception {
		String viewtag = uniqueTestVobName + "_one_int";
		System.out.println( "VIEW: " + context.views.get( viewtag ) );
		//File path = new File( context.views.get( viewtag ).getPath() );
		File path = new File( context.mvfs + "/" + uniqueTestVobName + "_one_int/" + uniqueTestVobName );
		
		System.out.println( "PATH: " + path );
		
		try {
			addNewContent( context.components.get( "Model" ), path, "test.txt" );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		
		Baseline rb = Baseline.create( "new-baseline", context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
	}
	
	@Test
	public void testPromote() throws Exception {
		Baseline bl = context.baselines.get( "client-3" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
		bl.promote();
		assertEquals( PromotionLevel.BUILT, bl.getPromotionLevel( false ) );
	}
	
	@Test
	public void testDemote() throws Exception {
		Baseline bl = context.baselines.get( "client-2" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
		bl.demote();
		assertEquals( PromotionLevel.REJECTED, bl.getPromotionLevel( false ) );
	}
	
	@Test
	public void testSetPromotionLevel() throws Exception {
		Baseline bl = context.baselines.get( "client-1" ).load();
		
		assertNotNull( bl );
		assertEquals( PromotionLevel.INITIAL, bl.getPromotionLevel( false ) );
		bl.setPromotionLevel( PromotionLevel.RELEASED );
		assertEquals( PromotionLevel.RELEASED, bl.getPromotionLevel( false ) );
	}
	
	@Test
	public void testGetStream() throws Exception {
		Baseline bl = context.baselines.get( "client-1" ).load();
		
		assertEquals( context.streams.get( uniqueTestVobName + "_one_int" ), bl.getStream() );
	}
	
	@Test
	public void testGetComponent() throws Exception {
		Baseline bl = context.baselines.get( "client-1" ).load();
		
		assertEquals( context.components.get( "Clientapp" ), bl.getComponent() );
	}
	
	@Test
	public void testGet() throws Exception {
		Baseline bl = Baseline.get( "_System_1.0@" + pvob );
		
		assertNotNull( bl );
	}
	
	@Test
	public void testGetPvob() throws Exception {
		Baseline bl = Baseline.get( "_System_1.0", pvob );
		
		assertNotNull( bl );
	}
	
	
}
