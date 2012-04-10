package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.util.debug.Logger;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class TearDown extends CLI {
	private static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		TearDown s = new TearDown();
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
			logger.error( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

		PVob pvob = PVob.get( otag.getString() );
		SetupUtils.tearDown( pvob );
	}
}
