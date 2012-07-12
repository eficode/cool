package net.praqma.clearcase.test;

import java.io.File;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.Deliver.DeliverStatus;
import net.praqma.clearcase.Rebase;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.DeliverException;
import net.praqma.clearcase.exceptions.DeliverException.Type;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.DynamicView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.debug.Logger;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeliverTest {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver" );
	
	@Test
	public void basic() throws ClearCaseException {
		File path = setActivity( "basic" );
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		String tviewtag = ccenv.getUniqueName() + "_one_int";
		File tpath = new File( ccenv.context.mvfs + "/" + tviewtag + "/" + ccenv.getVobName() );
		
		Baseline b = getNewBaseline( path, "f.txt", "one" );
		
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		
		assertTrue( deliver.deliver( true, true, true, false ) );
		
		String s = deliver.getStatus();
		
		logger.info( "STATUS: " + s );
		
		DeliverStatus st = deliver.getDeliverStatus();
		
		logger.info( "STATUS: " + st );
		
		assertEquals( DeliverStatus.NO_DELIVER_ON_STREAM, st );
		assertFalse( st.busy() );
	}
	
	@Test
	public void basicSplit() throws ClearCaseException {
		File path = setActivity( "basic-split" );
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		String tviewtag = ccenv.getUniqueName() + "_one_int";
		File tpath = new File( ccenv.context.mvfs + "/" + tviewtag + "/" + ccenv.getVobName() );
		
		Baseline b = getNewBaseline( path, "basic-split.txt", "two" );
		
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		boolean d = deliver.deliver( true, false, true, false );
		assertTrue( d );
		
		DeliverStatus st = deliver.getDeliverStatus();
		assertTrue( st.busy() );
		
		/* Complete */
		assertNotNull( deliver.complete() );
		st = deliver.getDeliverStatus();
		assertFalse( st.busy() );
	}
	
	@Test
	public void basicBusy() throws ClearCaseException {
		
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		/* Target */
		String tviewtag = ccenv.getUniqueName() + "_one_int";
		File tpath = new File( ccenv.context.mvfs + "/" + tviewtag + "/" + ccenv.getVobName() );
		
		/* Set deliver one up */
		File path = setActivity( "basic-busy1" );
		Baseline b = getNewBaseline( path, "basic-busy1.txt", "three" );
		
		/* Do not complete deliver */
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		deliver.deliver( true, false, true, false );
		
		DeliverStatus st = deliver.getDeliverStatus();
		assertTrue( st.busy() );
		
		/* Setup deliver two */
		File path2 = setActivity( "basic-busy2" );
		Baseline b2 = getNewBaseline( path2, "basic-busy2.txt", "four" );
		
		Deliver deliver2 = new Deliver( b2, source, target, tpath, tviewtag );
		try { 
			deliver.deliver( true, false, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			if( !e.getType().equals( Type.DELIVER_IN_PROGRESS ) ) {
				fail( "Should be DELIVER IN PROGRESS" );
			}
		}
		
		DeliverStatus st2 = deliver2.getDeliverStatus();
		assertTrue( st2.busy() );
		
		/* Complete */
		assertNotNull( deliver.complete() );
		st = deliver.getDeliverStatus();
		assertFalse( st.busy() );
	}
	
	
	protected File setActivity( String name ) throws ClearCaseException {
		/**/
		String viewtag = ccenv.getUniqueName() + "_one_dev";
		System.out.println( "VIEW: " + ccenv.context.views.get( viewtag ) );
		File path = new File( ccenv.context.mvfs + "/" + viewtag + "/" + ccenv.getVobName() );
				
		System.out.println( "PATH: " + path );
		
		Stream stream = Stream.get( "one_dev", ccenv.getPVob() );
		Activity activity = Activity.create( "deliver-" + name, stream, ccenv.getPVob(), true, "ccucm activity", null, path );
		UCMView.setActivity( activity, path, null, null );
		
		return path;
	}
	
	protected Baseline getNewBaseline( File path, String filename, String bname ) throws ClearCaseException {
		
		try {
			ccenv.addNewElement( ccenv.context.components.get( "Model" ), path, filename );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		return Baseline.create( bname, ccenv.context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
	}
	
}
