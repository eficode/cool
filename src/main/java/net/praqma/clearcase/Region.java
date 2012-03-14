package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Region extends Cool {
	
	private static final Pattern __FIND_VIEW_ROOT = Pattern.compile( "^\\s*\\**\\s*([\\w\\.-]+)\\s*(.+)$" );

    private Site site;
    private String name;

    public Region( String name, Site site ) {
        this.name = name;
        this.site = site;
    }

    public List<Vob> getVobs() throws CleartoolException {
        return site.getVobs( this );
    }

    public List<UCMView> getViews() throws CleartoolException {
		String cmd = "lsview" + ( this != null ? " -region " + getName() : "" );
		try {
			CmdResult cr = Cleartool.run( cmd );

			List<UCMView> views = new ArrayList<UCMView>();
			for( String s : cr.stdoutList ) {

				/* Pre process views */
				Matcher m = __FIND_VIEW_ROOT.matcher( s );
				if( m.find() ) {
					views.add( new UCMView( m.group( 2 ).trim(), m.group( 1 ).trim() ) );
				}
			}

			return views;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get views from " + getName() + ": " + e.getMessage() );
		}
    }

    public String getName() {
        return name;
    }
}
