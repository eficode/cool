package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.util.debug.Logger;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class Setup extends CLI {
	private static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws ClearCaseException, IOException, Exception {
		Setup s = new Setup();
        s.perform( args );
	}

	public void perform( String[] args ) throws ClearCaseException, IOException, Exception {

		Options o = new Options( "1.0.0" );

		Option ofile = new Option( "file", "f", true, 1, "XML file describing setup" );

		o.setOption( ofile );

		o.setDefaultOptions();

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.error( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}


		File file = new File( ofile.getString() );
		logger.verbose( "Parsing " + file.getAbsolutePath() );
		EnvironmentParser parser = new EnvironmentParser( file );
		parser.parse();
	}
}
