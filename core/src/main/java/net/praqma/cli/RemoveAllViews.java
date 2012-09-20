package net.praqma.cli;

import java.util.List;
import java.util.logging.Logger;

import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class RemoveAllViews {
	private static Logger logger = Logger.getLogger( RemoveAllViews.class.getName() );

	public static void main( String[] args ) throws CleartoolException {

		Options o = new Options( "1.0.0" );

		Option oregion = new Option( "region", "r", true, 1, "Name of the region" );

		o.setOption( oregion );

		o.setDefaultOptions();

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.severe( "Incorrect option: " + e.getMessage() );
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
