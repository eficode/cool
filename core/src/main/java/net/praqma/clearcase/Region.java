package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.logging.Config;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Region extends Cool {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static final Pattern __FIND_VIEW_ROOT = Pattern.compile( "^\\s*\\**\\s*([\\w\\.-]+)\\s*(.+)$" );

    private Site site;
    private String name;

    public Region( String name, Site site ) {
    	tracer.entering(Region.class.getSimpleName(), "Region", new Object[]{name, site});
        this.name = name;
        this.site = site;
        tracer.exiting(Region.class.getSimpleName(), "Region");
    }

    public List<Vob> getVobs() throws CleartoolException {
    	tracer.entering(Region.class.getSimpleName(), "getVobs");
    	
    	List<Vob> vobs = site.getVobs( this );
    	
    	tracer.exiting(Region.class.getSimpleName(), "getVobs", vobs);
    	
        return vobs;
    }

    public List<UCMView> getViews() throws CleartoolException {
    	tracer.entering(Region.class.getSimpleName(), "getViews");
    	
		String cmd = "lsview" + ( this != null ? " -region " + getName() : "" );
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		List<UCMView> views = new ArrayList<UCMView>();
		try {
			CmdResult cr = Cleartool.run( cmd );
			
			tracer.finest("Reading Cleartool output.");
			for( String s : cr.stdoutList ) {

				/* Pre process views */
				Matcher m = __FIND_VIEW_ROOT.matcher( s );
				
				tracer.finest("Checking if pattern matches Cleartool output.");
				
				if( m.find() ) {
					tracer.finest("Pattern matches Cleartool output.");
					
					views.add( new UCMView( m.group( 2 ).trim(), m.group( 1 ).trim() ) );
				}
			}

			
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to get views from " + getName() + ": " + e.getMessage() );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Region.class.getSimpleName(), "getViews", views);
		return views;
    }

    public String getName() {
    	tracer.entering(Region.class.getSimpleName(), "getName");
    	tracer.exiting(Region.class.getSimpleName(), "getName", name);
        return name;
    }
}
