package net.praqma.cli;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@SuppressFBWarnings("")
public class RemoveViews extends CLI {
	private static Logger logger = Logger.getLogger( RemoveViews.class.getName() );

	public static void main( String[] args ) throws Exception {
        RemoveViews s = new RemoveViews();
        s.perform( args );
	}

    @Override
    public void perform( String[] arguments ) throws Exception {
        Options o = new Options( "1.0.0" );

        Option oregex = new Option( "regex", "r", true, 1, "Regular expression" );
        Option oregion = new Option( "region", "r", false, 1, "Name of the region" );
        Option odryrun = new Option( "dry-run", "d", false, 0, "Dry run" );

        o.setOption( oregex );
        o.setOption( oregion );
        o.setOption( odryrun );

        o.setDefaultOptions();

        o.parse( arguments );

        try {
            o.checkOptions();
        } catch( Exception e ) {
            logger.severe( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( odryrun.isUsed() ) {
            logger.info( "Dry run" );
        }

        Site site = new Site( "My site" );
        Region region = new Region( oregion.getString(), site );

        List<UCMView> views;

        if( oregion.isUsed() ) {
            views = region.getViews();
        } else {
            views = UCMView.getViews();
        }

        for( UCMView view : views ) {
            logger.finest( "Checking " + view );
            if( view.getViewtag().matches( oregex.getString() ) ) {
                try {
                    logger.info( "Removing " + view.getViewtag() );
                    if( !odryrun.isUsed() ) {
                        view.remove();
                    }
                } catch( ClearCaseException e ) {
                    logger.warning( "Failed to remove " + view.getViewtag() );
                }
            } else {
                /* Did match criteria */
            }
        }
    }
}
