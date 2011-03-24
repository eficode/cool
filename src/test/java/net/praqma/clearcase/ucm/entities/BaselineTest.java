package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaselineTest
{
	@BeforeClass
	public static void startup()
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
	}

	@Test
	public void testLoad()
	{
		String baseline = "baseline:CHW_BASELINE_51@\\Cool_PVOB";
		Baseline bl = null;
		try
		{
			bl = UCMEntity.GetBaseline( baseline, false );
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
			bl = UCMEntity.GetBaseline( baseline, false );
			fail( "Could not get baseline" );
		}
		catch ( UCMException e )
		{
			
		}
	}

	@Test
	public void testStringify() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		assertNotNull( bl.Stringify() );
	}

	@Test
	public void testBaseline() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		assertNotNull( bl.Stringify() );
		
		assertEquals( "baseline:CHW_BASELINE_51@\\Cool_PVOB", bl.GetFQName() );
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
		Baseline bl = UCMEntity.GetBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		
		assertEquals( "TESTED", bl.getPromotionLevel( true ).toString() );
		
		assertEquals( "TESTED", bl.getPromotionLevel( false ).toString() );
	}

	@Test
	public void testPromote() throws UCMException
	{
		Baseline bl = UCMEntity.GetBaseline( "baseline:CHW_BASELINE_51@\\Cool_PVOB", true );
		
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
