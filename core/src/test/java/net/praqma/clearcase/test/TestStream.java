package net.praqma.clearcase.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.clearcase.util.SetupUtils;

public class TestStream extends CoolTestCase {
	
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
	public void testFoundationBaselines() throws Exception {
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", getPVob() ).load();
		
		System.out.println( "Foundation baselines:" + stream.getFoundationBaselines() );
		
		assertEquals( "_System_1.0", stream.getFoundationBaselines().get( 0 ).getShortname() );

		assertTrue( true );
	}
	
	@Test
	public void testCreateStream() throws Exception {
		
		Stream parent = context.streams.get( uniqueTestVobName + "_one_dev" );
		
		Stream nstream = Stream.create( parent, "new-stream", false, new ArrayList<Baseline>() );
		
		assertNotNull( nstream );
		assertEquals( "stream:new-stream@" + getPVob(), nstream.getFullyQualifiedName() );
	}
	
	@Test
	public void testCreateIntegrationStream() throws Exception {
		
		Project project = Project.create( "test-project", null, getPVob(), 0, null, true, new ArrayList<Component>() );
		
		Stream istream = Stream.createIntegration( "test-int", project, context.baselines.get( "_System_1.0" ) );
		
		assertNotNull( istream );
		assertEquals( "stream:test-int@" + getPVob(), istream.getFullyQualifiedName() );
		
		istream.load();
		
		assertEquals( istream.getFoundationBaseline(), context.baselines.get( "_System_1.0" ) );
	}
	
	@Test
	public void testGetChildStreamsNoMultisite() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Stream> childs = istream.getChildStreams( false );
		
		assertEquals( 1, childs.size() );		
	}
	
	@Test
	public void testGetPostedDelivery() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Baseline> baselines = istream.getPostedBaselines( context.components.get( 0 ), PromotionLevel.INITIAL );
		
		assertEquals( 0, baselines.size() );
	}
	
	
	@Test
	public void testHasPostedDelivery() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		boolean has = istream.hasPostedDelivery();
		
		assertFalse( has );
	}
	
	@Test
	public void testGetSiblingStream() throws Exception {
		
		Project project1 = Project.create( "test-project1", null, getPVob(), 0, null, true, new ArrayList<Component>() );
		Stream istream1 = Stream.createIntegration( "test-int1", project1, context.baselines.get( "_System_1.0" ) );
		project1.setStream( istream1 );
		
		Project project2 = Project.create( "test-project2", null, getPVob(), 0, null, true, new ArrayList<Component>() );
		Stream istream2 = Stream.createIntegration( "test-int2", project2, context.baselines.get( "_System_1.0" ) );
		
		istream1.setDefaultTarget( istream2 );
		
		List<Stream> siblings = istream2.getSiblingStreams();
		
		System.out.println( "SIBLINGS: " + siblings );
		
		assertEquals( 1, siblings.size() );
	}
	
	@Test
	public void testStreamExists() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		assertTrue( istream.exists() );		
	}
	
	@Test
	public void testGetRecommendedBaselines() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		List<Baseline> baselines = istream.getRecommendedBaselines();
		
		System.out.println( "RECOMMENDED BASELINES: " + baselines );
		
		assertEquals( 1, baselines.size() );
	}
	
	@Test
	public void testGenerate() throws Exception {
		
		Stream istream = Stream.get( uniqueTestVobName + "_one_int", getPVob() );
		
		istream.generate();
	}
	
	@Test
	public void testRecommendBaseline() throws Exception {
		
		String viewtag = uniqueTestVobName + "_one_int";
		System.out.println( "VIEW: " + context.views.get( viewtag ) );
		//File path = new File( context.views.get( viewtag ).getPath() );
		File path = new File( context.mvfs + "/" + uniqueTestVobName + "_one_int/" + uniqueTestVobName );
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", pvob );
		
		System.out.println( "PATH: " + path );
		
		try {
			addNewContent( context.components.get( "Model" ), path, "test.txt" );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		
		Baseline rb = Baseline.create( "recommend-baseline", context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
		
		stream.recommendBaseline( rb );
	}
	
	
	@Test
	public void testLatestBaselines() throws Exception {
		
		String viewtag = uniqueTestVobName + "_one_int";
		System.out.println( "VIEW: " + context.views.get( viewtag ) );
		//File path = new File( context.views.get( viewtag ).getPath() );
		File path = new File( context.mvfs + "/" + uniqueTestVobName + "_one_int/" + uniqueTestVobName );
		
		Stream stream = Stream.get( uniqueTestVobName + "_one_int", pvob );
		
		List<Baseline> latest = stream.getLatestBaselines();
		
		System.out.println( "Latest baselines: " + latest );
		
		assertEquals( 6, latest.size() );
	}

}
