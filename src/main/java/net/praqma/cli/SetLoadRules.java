package net.praqma.cli;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.ConfigSpec;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;


@SuppressFBWarnings("")
public class SetLoadRules extends CLI {
	private static Logger logger = Logger.getLogger( SetLoadRules.class.getName() );
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		SetLoadRules s = new SetLoadRules();
		s.perform( args );
	}

	public void perform( String[] args ) throws ClearCaseException, IOException {

		Options o = new Options( "1.0.0" );

		Option otag = new Option( "rules", "r", true, -1, "A list of load rules" );

		o.setOption( otag );

		o.setDefaultOptions();

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.severe( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

        logger.info( "Generating new config spec" );
        ConfigSpec cs = new ConfigSpec( new File( System.getProperty( "user.dir" ) ) );
        for( String c : otag.getStrings() ) {
            cs.addLoadRule( c );
        }
        cs.generate().appy();
	}
}
