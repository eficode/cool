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

public class NoInterprojectDeliver {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver", "setup-no-interproject-no-baselines.xml" );
	
	@Test
	public void rebaseBeforeDeliver() throws ClearCaseException {
		Stream target = ccenv.context.streams.get( "two_int" );
		Stream source = ccenv.context.streams.get( "one_int" );
		
		/* One */
		String viewtag = ccenv.getVobName() + "_one_int";
		File path = ccenv.setDynamicActivity( source, viewtag, "interproject-deliver-one" );
		Baseline b = getNewBaseline( path, "interproject-deliver.txt", "one" );
		source.recommendBaseline( b );
		
		/* Two */
		String tviewtag = ccenv.getVobName() + "_two_int";
		File tpath = ccenv.getDynamicPath( tviewtag );
				
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		try { 
			deliver.deliver( true, true, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			assertEquals( Type.INTERPROJECT_DELIVER_DENIED, e.getType() );
		}
		
		/* Deliver should not be started */
		DeliverStatus status = deliver.getDeliverStatus();
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
