package net.praqma.clearcase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.command.ListVobs;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import org.apache.commons.lang.SystemUtils;

public class PVob extends Vob {
	
	private static Logger logger = Logger.getLogger( PVob.class.getName() );

	private String localPath;
	private String globalPath;
	
	public PVob( String name ) {
		super( name );

		this.projectVob = true;
	}
	
	public static PVob create( String name, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
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

    public List<Stream> getStreams() throws CleartoolException, UnableToInitializeEntityException {
        String cmd = null;
        
        if(SystemUtils.IS_OS_WINDOWS) {
            cmd = "lsstream -fmt %Xn\\n -invob " + this;
        } else {
            cmd = "lsstream -fmt %Xn\\\\n -invob " + this;
        }
        List<String> lines = null;
        try {
            lines = Cleartool.run( cmd ).stdoutList;
            logger.finest( "Output: " + lines );
        } catch( Exception e ) {
            throw new CleartoolException( "Unable to list views", e );
        }

        List<Stream> streams = new ArrayList<Stream>( lines.size() );

        for( String line : lines ) {
            streams.add( Stream.get( line.trim() ) );
        }

        return streams;
    }
	
	public Set<UCMView> getViews() throws CleartoolException {
		String cmd = "lsstream -fmt {%[views]p} -invob " + this;
		List<String> lines = null;
		try {
			lines = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views", e );
		}
		
		logger.finest( "OUT IS: " + lines );
		
		Set<UCMView> views = new HashSet<UCMView>();
		
		for( String l : lines ) {
			if( !l.matches( "^\\s*$" ) ) {
				Matcher m = rx_find_component.matcher( l );
				while( m.find() ) {
					/* Don't include root-less components */
					if( !m.group( 1 ).equals( "" ) ) {
						String[] vs = m.group( 1 ).trim().split( "\\s+" );
						for( String v : vs ) {
							try {
								views.add( UCMView.getView( v.trim() ) );
							} catch( ViewException e ) {
								logger.warning( "Unable to get " + m.group( 1 ) + ": " + e.getMessage() );
							}
						}
					}
				}
			}
		}
		
		return views;
	}
	
	public static final Pattern rx_find_component = Pattern.compile( "\\{(.*?)\\}" );
	public static final Pattern rx_find_vob = Pattern.compile( "^(.*?)" + Cool.qfs + "[\\S&&[^"+Cool.qfs+"]]+$" );
	
	public Set<Vob> getVobs() throws CleartoolException {
		String cmd = "lscomp -fmt {%[root_dir]p} -invob " + this;
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list vobs", e );
		}
		
		Set<Vob> vobs = new HashSet<Vob>();
		
		logger.fine( "OUT IS: " + list );
		
		for( String l : list ) {
			if( !l.matches( "^\\s*$" ) ) {
				Matcher m = rx_find_component.matcher( l );
				while( m.find() ) {
					/* Don't include root-less components */
					if( !m.group( 1 ).equals( "" ) ) {
						Matcher mvob = PVob.rx_find_vob.matcher( m.group( 1 ) );
						if( mvob.find() ) {
							try {
								vobs.add( Vob.get( mvob.group( 1 ) ) );
							} catch( ArrayIndexOutOfBoundsException e ) {
								logger.warning( l + " was not a VOB" );
							}
						}
					}
				}
			}
		}
		
		logger.fine( "Vobs: " + vobs );
		
		return vobs;
	}

    public static List<Vob> getPVobs() {
        return new ListVobs().execute().get();
    }
}
