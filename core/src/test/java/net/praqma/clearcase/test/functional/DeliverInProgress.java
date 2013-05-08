package net.praqma.clearcase.test.functional;

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

public class DeliverInProgress {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver-in-progress", "setup-no-baselines.xml" );
	
	@Test
	public void deliverInProgress() throws ClearCaseException {
		Stream dev1 = ccenv.context.streams.get( "one_dev" );
		Stream dev2 = ccenv.context.streams.get( "two_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		/* Target */
		String tviewtag = ccenv.getUniqueName() + "_one_int";
		File tpath = new File( ccenv.context.mvfs + "/" + tviewtag + "/" + ccenv.getVobName() );
		
		/* Set deliver one up */
		String d1viewtag = ccenv.getUniqueName() + "_one_dev";
		File d1path = ccenv.setDynamicActivity( dev1, d1viewtag, "dip1" );
		Baseline bl1 = getNewBaseline( d1path, "dip1.txt", "dip1" );
		
		/* Do not complete deliver */
		Deliver deliver1 = new Deliver( bl1, dev1, target, tpath, tviewtag );
		deliver1.deliver( true, false, true, false );
		
		/* Set deliver two up */
		String d2viewtag = ccenv.getUniqueName() + "_two_dev";
		File d2path = ccenv.setDynamicActivity( dev2, d2viewtag, "dip2" );
		Baseline bl2 = getNewBaseline( d2path, "dip2.txt", "dip2" );
		
		Deliver deliver2 = new Deliver( bl2, dev2, target, tpath, tviewtag );
		try { 
			deliver2.deliver( true, true, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			if( !e.getType().equals( Type.DELIVER_IN_PROGRESS ) ) {
				fail( "Should be DELIVER IN PROGRESS" );
			}
		}
		
		/* Deliver should NOT be started */
		DeliverStatus status = deliver2.getDeliverStatus();
		assertFalse( status.busy() );
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
