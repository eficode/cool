package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToRemoveEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;

public class PVob extends Vob {
	
	private static Logger logger = Logger.getLogger();

	private String localPath;
	private String globalPath;
	
	public static final String rx_format = "\\S+";

	public PVob( String name ) {
		super( name );

		this.projectVob = true;
	}

	public static PVob create( String name, String path, String comment ) throws CleartoolException {
		Vob.create( name, true, path, comment );
		PVob pvob = new PVob( name );
		pvob.storageLocation = path;
		
		if( path == null ) {
			pvob.load();
		}

		return pvob;
	}

	public static PVob get( String pvobname ) {
		try {
			PVob pvob = new PVob( pvobname );
			pvob.load();
			return pvob;
		} catch( Exception e ) {
			return null;
		}
	}
	
	public Set<UCMView> getViews() throws CleartoolException {
		String cmd = "lsstream -fmt %[views]p\\n -invob " + this;
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views", e );
		}
		
		Set<UCMView> views = new HashSet<UCMView>();
		
		for( String l : list ) {
			if( !l.matches( "^\\s*$" ) ) {
				logger.debug( "l: " + l );
				try {
					views.add( UCMView.getView( l ) );
				} catch( ViewException e ) {
					logger.warning( "Unable to get " + l + ": " + e.getMessage() );
				}
			}
		}
		
		return views;
	}
	
	public static final Pattern rx_find_vob = Pattern.compile( "" );
	
	public Set<Vob> getVobs() throws CleartoolException {
		String cmd = "lscomp -fmt %[root_dir]p\\n -invob " + this;
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list vobs", e );
		}
		
		Set<Vob> vobs = new HashSet<Vob>();
		
		for( String l : list ) {
			if( !l.matches( "^\\s*$" ) ) {
				String[] s = l.split( Pattern.quote( Cool.filesep ) );
				try {
					vobs.add( Vob.get( Cool.filesep + s[1] ) );
				} catch( ArrayIndexOutOfBoundsException e ) {
					logger.warning( l + " was not a VOB" );
				}
			}
		}
		
		logger.debug( "Vobs: " + vobs );
		
		return vobs;
	}

}
