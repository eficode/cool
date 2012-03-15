package net.praqma.cli;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Map;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class PostedDelivery {
	
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );
	
	public static void main( String[] args ) throws UCMException {
		try {
			run( args );
		} catch( UCMException e ) {
			System.err.println( UCM.getMessagesAsString() );
			throw e;
		}
	}

	public static void run( String[] args ) throws UCMException {

        //app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        app.setMinimumLevel( LogLevel.DEBUG );

		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );

		try {
			Baseline bl = null; //UCMEntity.getBaseline( "CHW_BASELINE_51_posted_delivery@\\Cool_PVOB", true );
			Stream source = UCMEntity.getStream("stream:moon-vobadmin_Client@\\Cool_PVOB");
			Stream target = null;
			//bl.deliver(source, target, (File) null, "viewtag", false, false, false);
			source.deliver(bl, target, (File) null, "night-vobadmin_Client_int", true, false, false);
		} catch( Exception e ) {
			logger.error( "Error: " + e.getMessage() );
			System.exit( 1 );
		}
		
		logger.debug( "Done");
	}

}
