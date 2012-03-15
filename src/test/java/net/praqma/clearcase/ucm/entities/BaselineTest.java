package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.*;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;

public class BaselineTest
{
    private static Appender app;
		
	@BeforeClass
	public static void startup() {
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
        app = new ConsoleAppender();
        app.setMinimumLevel( LogLevel.DEBUG );
        Logger.addAppender( app );
	}
	
    @AfterClass
    public static void end() {
        Logger.removeAppender( app ); 
       }
    
	@Test
	public void testLoad()
	{
		String baseline = "baseline:CHW_BASELINE_51@\\Cool_PVOB";
		Baseline bl = null;
		try
		{
			bl = UCMEntity.getBaseline( baseline, false );
		}
		catch ( UCMException e )
		{
			fail( "Could not get baseline" );
		}
	}
	
	@Test
	public void testLoadNotExist()
	{
		String baseline = "baseline:CHW_BASELINE_51_no@\\Cool_PVOB";
		Baseline bl = null;
		try
		{
			bl = UCMEntity.getBaseline( baseline, false );
			fail( "Could not get baseline" );
		}
		catch ( UCMException e )
		{
			
		}
	}

	@Test
	public void testStringify() throws UCMException
	{
		Baseline bl = UCMEntity.getBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		assertNotNull( bl.stringify() );
	}

	@Test
	public void testBaseline() throws UCMException
	{
		Baseline bl = UCMEntity.getBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		assertNotNull( bl.stringify() );
		
		assertEquals( "baseline:CHW_BASELINE_51@\\Cool_PVOB", bl.getFullyQualifiedName() );
		assertEquals( "\\Cool_PVOB", bl.pvob );
		assertEquals( "CHW_BASELINE_51", bl.shortname );
		assertEquals( "chw", bl.user );
	}

	/*
	@Test
	public void testGetEntity()
	{
		fail( "Not yet implemented" );
	}
	*/

	@Test
	public void testGetPromotionLevel() throws UCMException
	{
		Baseline bl = UCMEntity.getBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		
		assertEquals( "TESTED", bl.getPromotionLevel( true ).toString() );
		
		assertEquals( "TESTED", bl.getPromotionLevel( false ).toString() );
	}

	@Test
	public void testPromote() throws UCMException
	{
		Baseline bl = UCMEntity.getBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		
		bl.promote();
	}

	@Test
	public void testDeliver() throws UCMException
	{
		Baseline bl = UCMEntity.getBaseline( "CHW_BASELINE_51_posted_delivery@\\Cool_PVOB", true );
		Stream source = UCMEntity.getStream("stream:bn_stream@\\Cool_PVOB");
		Stream target = UCMEntity.getStream("stream:bn_stream@\\Cool_PVOB");
		assertNotNull( bl.stringify() );
		assertNotNull( source.stringify() );
		assertNotNull( target.stringify() );
		bl.deliver(source, target, (File) null, "viewtag", false, false, false);
	}

	/*
	@Test
	public void testDemote()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetDiffs()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetComponent()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetStream()
	{
		fail( "Not yet implemented" );
	}
	*/

}
