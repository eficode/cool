package net.praqma.cli;

import java.io.IOException;
import java.util.logging.Logger;

import net.praqma.clearcase.PVob;
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

		Option otag = new Option( "tag", "t", true, 1, "UCM Project VOB tag" );

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

		PVob pvob = PVob.get( otag.getString() );
		SetupUtils.tearDown( pvob );
	}
}
