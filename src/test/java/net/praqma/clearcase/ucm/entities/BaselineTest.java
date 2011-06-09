package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import org.junit.*;

import net.praqma.clearcase.ucm.UCMException;

public class BaselineTest
{
	@BeforeClass
	public static void startup()
	{
		UCM.setContext( UCM.ContextType.CLEARTOOL );
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
