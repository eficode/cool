package net.praqma.clearcase.ucm.utils;

import static org.junit.Assert.*;

import java.io.File;


import net.praqma.clearcase.Cool;
import net.praqma.clearcase.cleartool.CommandLineMock;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;
import net.praqma.util.structure.Tuple;


import org.junit.BeforeClass;
import org.junit.Test;

public class BuildNumberTest extends Cool
{	
	@BeforeClass
	public static void startup()
	{
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		File f = new File( BuildNumberTest.class.getClassLoader().getResource( "version.h" ).getFile() );
		CommandLineMock.setVersionDotH( f );
	}
	
	
	
	@Test
	public void testCreateBuildNumber() throws UCMException
	{		
		Component component = UCMEntity.getComponent( "System@\\Cool_PVOB" );
		File view = new File( "c:\\" );
		Tuple<Baseline, String[]> result = BuildNumber.createBuildNumber( "bls__1_2_3_123", component, view );
		
		assertEquals( "Major", result.t2[0], "1" );
		assertEquals( "Minor", result.t2[1], "2" );
		assertEquals( "Patch", result.t2[2], "3" );
		assertEquals( "Sequence", result.t2[3], "123" );
		
		assertEquals( "Baseline", result.t1.getFullyQualifiedName(), "baseline:bls__1_2_3_123@\\Cool_PVOB" );
		
	}
	
	@Test
	public void testStampIntoCodeBaseline() throws UCMException
	{
		Baseline blbn = UCMEntity.getBaseline( "bn__1_2_3_1234@\\Cool_PVOB" );
		
		BuildNumber.stampIntoCode( blbn );
	}

	
	@Test
	public void testStampFromComponent() throws UCMException
	{
		Component component = UCMEntity.getComponent( "_System@\\Cool_PVOB" );
		File view = new File( "c:\\" );
		
		Tuple<Baseline, String[]> result = BuildNumber.createBuildNumber( "bls__1_2_3_123", component, view );
		
		BuildNumber.stampFromComponent( component, view, result.t2[0], result.t2[1], result.t2[2], result.t2[3], false );
	}
	
	@Test
	public void testStampFromComponentNoBuildNumberFile() throws UCMException
	{
		Component component = UCMEntity.getComponent( "_System_no@\\Cool_PVOB" );
		File view = new File( "c:\\" );
		
		Tuple<Baseline, String[]> result = BuildNumber.createBuildNumber( "bls__1_2_3_123", component, view );
		
		try
		{
			BuildNumber.stampFromComponent( component, view, result.t2[0], result.t2[1], result.t2[2], result.t2[3], false );
		}
		catch( UCMException e )
		{
			if( e.type != UCMType.HLINK_ZERO_MATCHES )
			{
				fail( "Did not find zero hlinks...." );
			}
		}
	}
	
	
	@Test
	public void testStampIntoCode()
	{
		assertTrue( true );
	}
	

	@Test
	public void testGetNextBuildSequence() throws UCMException
	{
		Project project = UCMEntity.getProject( "project:bn_project@\\Cool_PVOB", true );
		
		int seq = BuildNumber.getNextBuildSequence( project );
		
		assertTrue( seq == 1235 );
	}
	
	@Test
	public void testGetNextBuildSequenceNoSequence() throws UCMException
	{
		Project project = UCMEntity.getProject( "project:bn_project_no@\\Cool_PVOB", true );
		
		try
		{
			BuildNumber.getNextBuildSequence( project );
		}
		catch( UCMException e )
		{
			assertTrue( true );
		}
	}

	@Test
	public void testGetBuildNumber() throws UCMException
	{
		Project project = UCMEntity.getProject( "project:bn_project@\\Cool_PVOB", true );
		
		String bn = BuildNumber.getBuildNumber( project );
		
		assertEquals( "__1_2_3_1235", bn );
		
	}

}
