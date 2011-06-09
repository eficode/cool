package net.praqma.clearcase.ucm.entities;

import static org.junit.Assert.*;

import org.junit.*;


import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class ComponentTest
{
	
	private static Logger logger = PraqmaLogger.getLogger( false );
	
	@BeforeClass
	public static void startup()
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
	}

	
	@Test
	public void testLoad() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System@\\Cool_PVOB", false );
		
		assertNotNull( component );
	}
	
	@Test
	public void testLoadNotExists() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System_no@\\Cool_PVOB", false );
		
		assertNotNull( component );
	}

	@Test
	public void testComponent() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System@\\Cool_PVOB", false );
		
		assertNotNull( component );
		assertEquals( "component:_System@\\Cool_PVOB", component.GetFQName() );
	}


	@Test
	public void testGetRootDir() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System@\\Cool_PVOB", false );
		
		component.GetRootDir();
	}

	@Test
	public void testGetBaselinesStreamZeroSize() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System@\\Cool_PVOB", true );
		Stream    stream    = UCMEntity.GetStream( "Server_int@\\Cool_PVOB", true );
		
		BaselineList list = component.GetBaselines( stream );
		
		assertTrue( list.size() == 0 );
	}

	@Test
	public void testGetBaselinesStreamPlevelZeroSize() throws UCMException
	{
		Component component = UCMEntity.GetComponent( "_System@\\Cool_PVOB", true );
		Stream    stream    = UCMEntity.GetStream( "Server_int@\\Cool_PVOB", true );
		
		BaselineList list = component.GetBaselines( stream, Plevel.INITIAL );
		
		assertTrue( list.size() == 0 );
	}

}
