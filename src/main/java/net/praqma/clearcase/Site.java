package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public class Site extends Cool {

	private String name;

	public Site( String name ) {
		this.name = name;
	}

	public List<Vob> getVobs( Region region ) throws CleartoolException {
		String cmd = "lsvob -s" + ( region != null ? " -region " + region.getName() : "" );
		try {
			CmdResult cr = Cleartool.run( cmd );

			List<Vob> vobs = new ArrayList<Vob>();
			for( String s : cr.stdoutList ) {
				vobs.add( new Vob( s ) );
			}

			return vobs;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get vobs from region " + region.getName() + ": " + e.getMessage() );
		}
	}
}
