package net.praqma.clearcase.test;

import java.io.File;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.NothingNewException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.debug.Logger;

import org.junit.ClassRule;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeliverTest {
	private static Logger logger = Logger.getLogger();

	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-deliver" );
	
	private class Content {
		Baseline baseline;
		String viewtag;
		File path;
	}
	
	@Test
	public void basic() throws ClearCaseException {
		Content c = createBaseline( "one" );
		Stream source = ccenv.context.streams.get( "one_dev" );
		Stream target = ccenv.context.streams.get( "one_int" );
		
		Deliver deliver = new Deliver( c.baseline, source, target, c.path, c.viewtag );
		
		assertTrue( deliver.deliver( true, true, true, false ) );
	}
	
	protected Content createBaseline( String name ) throws ClearCaseException {
		String viewtag = ccenv.getVobName() + "_one_dev";
		System.out.println( "VIEW: " + ccenv.context.views.get( viewtag ) );
		File path = new File( ccenv.context.getMvfs() + "/" + viewtag + "/" + ccenv.getVobName() );
		
		System.out.println( "PATH: " + path );
		
		try {
			ccenv.addNewContent( ccenv.context.components.get( "Model" ), path, "test.txt" );
		} catch( ClearCaseException e ) {
			ExceptionUtils.print( e, System.out, true );
		}
		Content c = new Content();
		Baseline b = Baseline.create( name, ccenv.context.components.get( "_System" ), path, LabelBehaviour.FULL, false );
		
		c.baseline = b;
		c.viewtag = viewtag;
		c.path = path;
		
		return c;
	}
	
}
