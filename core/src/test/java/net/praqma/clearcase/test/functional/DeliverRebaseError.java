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

public class DeliverRebaseError {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver-rebase" );
	
	@Test
	public void basicRebase() throws ClearCaseException {
		
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		/* Target */
		String tviewtag = ccenv.getUniqueName() + "_one_int";
		File tpath = new File( ccenv.context.mvfs + "/" + tviewtag + "/" + ccenv.getVobName() );
		
		String viewtag = ccenv.getUniqueName() + "_one_dev";
		
		Baseline latest = ccenv.context.baselines.get( "model-3" );

		/* Setup deliver */
		File path = setActivity( "basic-rebase" );
		Baseline b = getNewBaseline( path, "basic-rebase.txt", "five" );
		
		/* Setup rebase */
		Rebase rebase = new Rebase( source, new DynamicView( "", viewtag ), latest );
		rebase.rebase( false );
		
		Deliver deliver = new Deliver( b, source, target, tpath, tviewtag );
		try { 
			deliver.deliver( true, false, true, false );
			fail( "Deliver should fail" );
		} catch( DeliverException e ) {
			if( !e.getType().equals( Type.REBASE_IN_PROGRESS ) ) {
				fail( "Should be REBASE IN PROGRESS" );
			}
		}
		
		/* Deliver should not be started */
		DeliverStatus status = deliver.getDeliverStatus();
		assertFalse( status.busy() );
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
