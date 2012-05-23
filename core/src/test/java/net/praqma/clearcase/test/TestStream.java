package net.praqma.clearcase.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
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
		
		Stream istream = Stream.createIntegration( "test-int", context.projects.get( 0 ), context.baselines.get( 0 ) );
		
		assertNotNull( istream );
		assertEquals( "stream:test-intm@" + getPVob(), istream.getFullyQualifiedName() );
		
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

}
