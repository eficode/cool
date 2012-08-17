package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.logging.Config;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Site extends Cool {

	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private String name;

	public Site( String name ) {
		tracer.entering(Site.class.getSimpleName(), "Site", name);
		this.name = name;
		tracer.exiting(Site.class.getSimpleName(), "Site");
	}

	public List<Vob> getVobs( Region region ) throws CleartoolException {
		tracer.entering(Site.class.getSimpleName(), "getVobs", region);
		
		String cmd = "lsvob -s" + ( region != null ? " -region " + region.getName() : "" );
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		List<Vob> vobs = new ArrayList<Vob>();
		
		try {
			CmdResult cr = Cleartool.run( cmd );

			tracer.finest("Adding Vobs to list.");
			
			for( String s : cr.stdoutList ) {
				vobs.add( new Vob( s ) );
			}
			
			
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to get vobs from region " + region.getName() + ": " + e.getMessage() );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Site.class.getSimpleName(), "getVobs", vobs);
		return vobs;
	}
}
