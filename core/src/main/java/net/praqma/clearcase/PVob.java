package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.List;
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
	
	public List<UCMView> getViews() throws CleartoolException {
		String cmd = "lsstream -fmt %[views]p -invob " + this;
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views", e );
		}
		
		List<UCMView> views = new ArrayList<UCMView>();
		
		for( String l : list ) {
			logger.debug( "l: " + l );
			try {
				views.add( UCMView.getView( l ) );
			} catch( ViewException e ) {
				logger.warning( "Unable to get " + l + ": " + e.getMessage() );
			}
		}
		
		return views;
	}
	
	public static final Pattern rx_find_vob = Pattern.compile( "" );
	
	public List<Vob> getVobs() throws CleartoolException {
		String cmd = "lscomp -fmt %[root_dir]p -invob " + this;
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list vobs", e );
		}
		
		List<Vob> vobs = new ArrayList<Vob>();
		
		for( String l : list ) {
			String[] s = l.split( Pattern.quote( Cool.filesep ) );
			vobs.add( Vob.get( Cool.filesep + s[1] ) );
		}
		
		return vobs;
	}

}
