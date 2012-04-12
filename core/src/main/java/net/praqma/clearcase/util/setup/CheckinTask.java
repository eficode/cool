package net.praqma.clearcase.util.setup;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;
import net.praqma.util.debug.Logger;

import org.w3c.dom.Element;

public class CheckinTask extends AbstractTask {
	
	private static Logger logger = Logger.getLogger();

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String filename = e.getAttribute( "file" );
		File file = null;
		if( filename.length() > 0 ) {
			file = new File( filename );
		}
		String comment = e.getAttribute( "comment" ).length() > 0 ? e.getAttribute( "comment" ) : null;
		
		if( file != null ) {
			Version.checkIn( file, false, context.path, comment );
		} else {
			try {
				List<File> files = Version.getUncheckedIn( context.path );
				for( File f : files ) {
					logger.debug( "Checking in " + f );
					try {
						Version.checkIn( f, false, context.path );
					} catch( CleartoolException e1 ) {
						logger.debug( "Unable to checkin " + f );
						/* No op */
					}
				}
			} catch( CleartoolException e1 ) {
				logger.error( e1.getMessage() );				
			}
		}
	}

}
