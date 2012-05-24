package net.praqma.clearcase.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;

public class TestStream extends CoolTestCase {

	@Test
	public void testFoundationBaselines() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", getPVob() ).load();
		
		System.out.println( "Foundation baselines:" + stream.getFoundationBaselines() );
		
		assertEquals( "_System_1.0", stream.getFoundationBaselines().get( 0 ).getShortname() );

		assertTrue( true );
	}
	
	@Test
	public void testCreateStream() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream parent = context.streams.get( 0 );
		
		Stream nstream = Stream.create( parent, "new-stream", false, new ArrayList<Baseline>() );
		
		assertNotNull( nstream );
		assertEquals( "stream:new-stream@" + getPVob(), nstream.getFullyQualifiedName() );
	}
	
	@Test
	public void testCreateIntegrationStream() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Project project = Project.create( "test-project", null, getPVob(), 0, null, true, new ArrayList<Component>() );
		
		Stream istream = Stream.createIntegration( "test-int", project, context.baselines.get( 0 ) );
		
		assertNotNull( istream );
		assertEquals( "stream:test-int@" + getPVob(), istream.getFullyQualifiedName() );
		
		istream.load();
		
		assertEquals( istream.getFoundationBaseline(), context.baselines.get( 0 ) );
	}
	
	@Test
	public void testGetChildStreamsNoMultisite() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Stream> childs = istream.getChildStreams( false );
		
		assertEquals( 1, childs.size() );		
	}
	
	@Test
	public void testGetPostedDelivery() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Baseline> baselines = istream.getPostedBaselines( context.components.get( 0 ), PromotionLevel.INITIAL );
		
		assertEquals( 0, baselines.size() );
	}
	
	
	@Test
	public void testHasPostedDelivery() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		boolean has = istream.hasPostedDelivery();
		
		assertFalse( has );
	}
	
	@Test
	public void testGetSiblingStream() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Stream> siblings = istream.getSiblingStreams();
		
		System.out.println( "SIBLINGS: " + siblings );
		
		assertEquals( 1, siblings.size() );
	}
	
	@Test
	public void testStreamExists() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		assertTrue( istream.exists() );		
	}
	
	@Test
	public void testGetRecommendedBaselines() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Baseline> baselines = istream.getRecommendedBaselines();
		
		System.out.println( "RECOMMENDED BASELINES: " + baselines );
		
		assertEquals( 1, baselines.size() );
	}
	
	@Test
	public void testGenerate() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		istream.generate();
	}
	
	@Test
	public void testRecommendBaseline() throws Exception {
		
		String uniqueTestVobName = "cool" + uniqueTimeStamp;
		variables.put( "vobname", uniqueTestVobName );
		variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
		
		bootStrap( defaultSetup );
		
		String viewtag = uniqueTestVobName + "_one_int";
		System.out.println( "VIEW: " + context.views.get( viewtag ) );
		File path = new File( context.views.get( viewtag ).getPath() );
		
		System.out.println( "PATH: " + path );
		
		addNewContent( context.components.get( 0 ), path, "test.txt" );
	}

}
