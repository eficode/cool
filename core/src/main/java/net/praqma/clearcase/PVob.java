package net.praqma.clearcase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

public class PVob extends Vob {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();

	private String localPath;
	private String globalPath;
	
	public PVob( String name ) {
		super( name );
		tracer.entering(this.getClass().getSimpleName(), "PVob", name);

		this.projectVob = true;
		tracer.exiting(this.getClass().getSimpleName(), "PVob");
	}
	
	public static PVob create( String name, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
		tracer.entering(PVob.class.getSimpleName(), "create", new Object[]{name, path, comment});
		
		Vob.create( name, true, path, comment );
		PVob pvob = new PVob( name );
		pvob.storageLocation = path;
		
		tracer.finest("Checking if path exits.");
		if( path == null ) {
			tracer.finest("path exits.");
			pvob.load();
		}
		tracer.exiting(PVob.class.getSimpleName(), "create", pvob);
		return pvob;
	}

	public static PVob get( String pvobname ) {
		tracer.entering(PVob.class.getSimpleName(), "create", pvobname);
		
		tracer.finest("Attempting to load PVob...");
		try {
			PVob pvob = new PVob( pvobname );
			pvob.load();
			tracer.finest("PVob loaded successfully");
			tracer.exiting(PVob.class.getSimpleName(), "create");
			
			return pvob;
		} catch( Exception e ) {
			tracer.finest("Could not load PVob.");
			tracer.exiting(PVob.class.getSimpleName(), "create", null);
			return null;
		}
	}
	
	public Set<UCMView> getViews() throws CleartoolException {
		tracer.entering(PVob.class.getSimpleName(), "getViews");
		
		String cmd = "lsstream -fmt {%[views]p} -invob " + this;
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		List<String> lines = null;
		try {
			lines = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Unable to list views", e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		
		logger.debug( "OUT IS: " + lines );
		
		Set<UCMView> views = new HashSet<UCMView>();
		
		tracer.finest(String.format("Parsing Cleartool output: %s", lines));
		
		for( String l : lines ) {
			tracer.finest(String.format("Checking if output line %s matches regular expression %s", l, "\"^\\s*$\""));
			
			if( !l.matches( "^\\s*$" ) ) {
				tracer.finest("Line matches regular expression.");
				Matcher m = rx_find_component.matcher( l );
				
				tracer.finest(String.format("Finding %s in line.", rx_find_component));
				while( m.find() ) {
					
					tracer.finest("Checking if line is not a root-less component.");
					/* Don't include root-less components */
					if( !m.group( 1 ).equals( "" ) ) {
						tracer.finest("Line is not a root-less component");
						
						String[] vs = m.group( 1 ).trim().split( "\\s+" );
						
						for( String v : vs ) {
							tracer.finest(String.format("Attempting to add View: %s", v));
							try {
								views.add( UCMView.getView( v.trim() ) );
								tracer.finest("Successfully added view.");
							} catch( ViewException e ) {
								logger.warning( "Unable to get " + m.group( 1 ) + ": " + e.getMessage() );
								tracer.finest("Could not add view.");
							}
						}
					}
				}
			}
		}
		tracer.exiting(PVob.class.getSimpleName(), "getViews", views);
		
		return views;
	}
	
	public static final Pattern rx_find_component = Pattern.compile( "\\{(.*?)\\}" );
	public static final Pattern rx_find_vob = Pattern.compile( "^(.*?)" + Cool.filesep + "[\\S&&[^"+Cool.filesep+"]]+$" );
	
	public Set<Vob> getVobs() throws CleartoolException {
		tracer.entering(PVob.class.getSimpleName(), "getVobs");
		
		String cmd = "lscomp -fmt {%[root_dir]p} -invob " + this;
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		List<String> list = null;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Unable to list vobs", e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		
		Set<Vob> vobs = new HashSet<Vob>();
		
		logger.debug( "OUT IS: " + list );
		
		tracer.finest(String.format("Parsing Cleartool output: %s", list));
		
		for( String l : list ) {
			tracer.finest(String.format("Checking if output line %s matches regular expression %s", l, "\"^\\s*$\""));
			
			if( !l.matches( "^\\s*$" ) ) {
				tracer.finest("Line matches regular expression.");
				
				Matcher m = rx_find_component.matcher( l );
				
				tracer.finest(String.format("Finding %s in line.", rx_find_component));
				
				while( m.find() ) {
					
					tracer.finest("Checking if line is not a root-less component.");
					/* Don't include root-less components */
					if( !m.group( 1 ).equals( "" ) ) {
						tracer.finest("Line is not a root-less component");
						
						Matcher mvob = PVob.rx_find_vob.matcher( m.group( 1 ) );
						
						tracer.finest(String.format("Checking if %s was found in line.", rx_find_vob));
						if( mvob.find() ) {
							tracer.finest("Found match in line");
							tracer.finest(String.format("Attempting to add Vob."));
							try {
								vobs.add( Vob.get( mvob.group( 1 ) ) );
								
								tracer.finest("Vob was successfully added.");
							} catch( ArrayIndexOutOfBoundsException e ) {
								logger.warning( l + " was not a VOB" );
								tracer.finest("Could not add Vob");
							}
						}
					}
				}
			}
		}
		
		logger.debug( "Vobs: " + vobs );
		
		tracer.exiting(PVob.class.getSimpleName(), "getVobs", vobs);
		
		return vobs;
	}

}
