package net.praqma.clearcase.ucm.utils;

import static org.junit.Assert.*;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;

import org.junit.BeforeClass;
import org.junit.Test;

public class BuildNumberTest
{
	
	@BeforeClass
	public static void startup()
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
	}

	@Test
	public void testCreateBuildNumber()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testStampFromComponent()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testStampIntoCode()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetNextBuildSequence() throws UCMException
	{
		Project project = UCMEntity.GetProject( "project:bn_project@\\Cool_PVOB", true );
		
		BuildNumber.getNextBuildSequence( project );
	}

	@Test
	public void testGetBuildNumber() throws UCMException
	{
		Project project = UCMEntity.GetProject( "project:bn_project@\\Cool_PVOB", true );
		
		BuildNumber.getBuildNumber( project );
		
	}

}
