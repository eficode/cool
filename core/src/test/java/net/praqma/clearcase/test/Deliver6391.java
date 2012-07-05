package net.praqma.clearcase.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

public class Deliver6391 {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver", "setup-6391.xml" );
	
	@Test
	public void deliver6391() throws ClearCaseException {
		Stream dev = ccenv.context.streams.get( "two_dev" );
		Stream non_modifiable_int = ccenv.context.streams.get( "two_int" );
		Stream modifiable_int = ccenv.context.streams.get( "one_int" );
		
		/* Integration */
		String nmviewtag = ccenv.getVobName() + "_two_int";
		File nmpath = ccenv.getDynamicPath( nmviewtag );
		
		/* Rebase dev to new baseline */
		Baseline model_bl = ccenv.context.baselines.get( "model-1" );
		Baseline service_bl = Baseline.get( "Service_INITIAL", ccenv.getPVob() );
		Baseline client_bl = Baseline.get( "Clientapp_INITIAL", ccenv.getPVob() );
		List<Baseline> cfg = new ArrayList<Baseline>();
		cfg.add( client_bl );
		cfg.add( service_bl );
		cfg.add( model_bl );
		String devviewtag = ccenv.getVobName() + "_two_dev";
		Rebase rebase = new Rebase( dev, new UCMView( "", devviewtag ), cfg );
		rebase.rebase( true );
						
		Deliver deliver = new Deliver( dev, non_modifiable_int, nmpath, nmviewtag );
		try { 
			deliver.deliver( true, true, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			assertEquals( Type.MERGE_ERROR, e.getType() );
		}
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
