package net.praqma.cli;

import java.util.List;

import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.StreamAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class RemoveAllViews {
	private static Logger logger = Logger.getLogger();
	private static StreamAppender app = new StreamAppender( System.out );

	public static void main( String[] args ) throws CleartoolException {

		Options o = new Options( "1.0.0" );

		Option oregion = new Option( "region", "r", true, 1, "Name of the region" );

		o.setOption( oregion );

		o.setDefaultOptions();

		o.parse( args );

		app.setTemplate( "[%level]%space %message%newline" );
		Logger.addAppender( app );

		if( o.isVerbose() ) {
			app.setMinimumLevel( LogLevel.DEBUG );
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

		Site site = new Site( "My site" );
		Region region = new Region( oregion.getString(), site );

		List<UCMView> views = region.getViews();

		for( UCMView view : views ) {
			try {
				logger.info( "Removing " + view.getViewtag() );
				view.remove();
			} catch( ClearCaseException e ) {
				logger.warning( "Failed to remove " + view.getViewtag() );
			}
		}
	}
}
