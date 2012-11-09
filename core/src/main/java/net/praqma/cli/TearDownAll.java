package net.praqma.cli;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Region;
import net.praqma.clearcase.Site;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class TearDownAll extends CLI {
	private static Logger logger = Logger.getLogger( TearDownAll.class.getName() );
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		TearDownAll s = new TearDownAll();
		s.perform( args );
	}

	public void perform( String[] args ) throws ClearCaseException, IOException {

		Options o = new Options( "1.0.0" );

		o.setDefaultOptions();

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.severe( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

        for( Vob pvob : PVob.getPVobs() ) {
            logger.info( "Removing " + pvob );
            try {
		        SetupUtils.tearDown( (PVob) pvob );
            } catch( Exception e ) {
                logger.log( Level.WARNING, "Unable to remove " + pvob, e );
            }
        }

        /* Remaining Vobs */
        for( Vob vob : PVob.list( false ) ) {
            vob.remove( true );
        }
	}
}
