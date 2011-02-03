package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import net.praqma.clearcase.ucm.UCMException;

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
		String baseline = "baseline:CHW_BASELINE_51_111@\\Cool_PVOB";
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
	public void testStringify()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testBaseline()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetEntity()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetPromotionLevel()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testPromote()
	{
		fail( "Not yet implemented" );
	}

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

}
