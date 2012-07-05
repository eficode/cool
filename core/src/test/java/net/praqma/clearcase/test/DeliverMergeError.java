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

public class DeliverMergeError {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver", "setup-no-baselines.xml" );
	
	@Test
	public void mergeError() throws ClearCaseException {
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		/* Integration */
		String tviewtag = ccenv.getVobName() + "_one_int";
		File tpath = ccenv.setDynamicActivity( target, tviewtag, "strict-deliver" );
		Baseline tb = getNewBaseline( tpath, "merge.txt", "one" );
		target.recommendBaseline( tb );
		
		/* Development */
		String viewtag = ccenv.getVobName() + "_one_dev";
		File path = ccenv.setDynamicActivity( source, viewtag, "strict-deliver-dev" );
		Baseline b = getNewBaseline( path, "merge.txt", "two" );
				
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		try { 
			deliver.deliver( true, true, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			assertEquals( Type.MERGE_ERROR, e.getType() );
		}
		
		/* Deliver should be started */
		DeliverStatus status = deliver.getDeliverStatus();
		assertTrue( status.busy() );
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
