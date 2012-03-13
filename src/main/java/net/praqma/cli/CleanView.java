package net.praqma.cli;

import java.io.File;
import java.util.Map;

import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class CleanView {
	
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
		Options o = new Options();

		Option opath = new Option( "path", "p", false, 1, "ClearCase view to be cleaned" );
		Option oroot = new Option( "root", "r", false, 0, "Clean root directory" );

		o.setOption( opath );
		o.setOption( oroot );

        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        o.setDefaultOptions();
        
        o.parse( args );
        
        if( o.isVerbose() ) {
        	app.setMinimumLevel( LogLevel.VERBOSE );
        } else {
        	app.setMinimumLevel( LogLevel.INFO );
        }

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.error( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

		/* Do the ClearCase thing... */
		UCM.setContext( UCM.ContextType.CLEARTOOL );
		
		File viewroot = null;
		if( opath.isUsed() ) {
			viewroot = new File( opath.getString() );
		} else {
			viewroot = new File( System.getProperty( "user.dir" ) );
		}
		
		boolean exclude = true;
		if( oroot.isUsed() ) {
			exclude = false;
		}

		SnapshotView view = SnapshotView.getSnapshotViewFromPath( viewroot );
		Map<String, Integer> info = view.swipe( exclude );
		
		logger.info( "Removed " + info.get( "files_deleted" ) + " file" + ( info.get( "files_deleted" ) == 1 ? "" : "s" ) );
		logger.info( "Removed " + info.get( "dirs_deleted" ) + " director" + ( info.get( "dirs_deleted" ) == 1 ? "y" : "ies" ) );		
	}

}
