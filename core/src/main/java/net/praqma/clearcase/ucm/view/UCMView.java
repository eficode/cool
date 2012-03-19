package net.praqma.clearcase.ucm.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CmdResult;

public class UCMView extends UCM implements Serializable {

	public static final Pattern rx_view_get_path = Pattern.compile( "^\\s*Global path:\\s*(.*?)\\s*$" );

	transient private static Logger logger = Logger.getLogger();
	
	private static Map<String, UCMView> createdViews = new HashMap<String, UCMView>();

	protected String path;
	protected String viewtag = "";
	protected Stream stream = null;
	
	protected String storageLocation = null;
	
	protected boolean dynamic = false;

	public UCMView() {
	}

	public UCMView(String path) {
		this.path = path;
	}
	
	public UCMView(String path, String viewtag) {
		this.path = path;
		this.viewtag = viewtag;
	}
	
	public UCMView(String path, String viewtag, Stream stream) {
		this.path = path;
		this.viewtag = viewtag;
		this.stream = stream;
	}

	public static SnapshotView getSnapshotView(File viewroot) throws CleartoolException, ViewException, IOException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return new SnapshotView(viewroot);
	}

	public static boolean viewExists(String viewtag) {
		//boolean b = context.viewExists(viewtag);
		
		logger.debug( viewtag );

		String cmd = "lsview " + viewtag;

		try {
			String s = Cleartool.run( cmd ).stdoutBuffer.toString();
			logger.debug( viewtag + " exists" );
			return true;
		} catch( Exception e ) {
			logger.debug( "View check failed: " + e.getMessage() );
			return false;
		}
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public String getViewtag() {
		return this.viewtag;
	}
	
	public void remove() throws ViewException {
		//context.removeView( this );
		String cmd = "rmview -force " + ( isDynamicView() ? "-tag " + getViewtag() : getStorageLocation() );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new ViewException( "Unable to remove " + this, path, Type.REMOVE_FAILED, e );
		}
	}
	
	public boolean isDynamicView() {
		return this.dynamic;
	}
	
	public UCMView load() throws ViewException {
		//Map<String, String> options = context.loadView( this );
		
		logger.debug( "Loading view " + this );

		String cmd = "lsview -l " + getViewtag();

		Map<String, String> options = new HashMap<String, String>();

		try {
			CmdResult r = Cleartool.run( cmd );

			for( String s : r.stdoutList ) {
				if( s.contains( "Global path" ) ) {
					Matcher m = rx_view_get_path.matcher( s );
					if( m.find() ) {
						options.put( "pathname", m.group( 1 ) );
					}
				}
			}

		} catch( Exception e ) {
			throw new ViewException( "Unable to load " + this, this.path, Type.LOAD_FAILED, e );
		}

		try {
			this.storageLocation = options.get("pathname");
		} catch( NullPointerException e ) {
			throw new ViewException( "No storage location", this.path, Type.LOAD_FAILED, e );
		}
		
		return this;
	}
	
	public String getStorageLocation() throws ViewException {
		if( this.storageLocation == null ) {
			this.load();
		}
		
		return this.storageLocation;
	}
	
	public Stream getStream() throws CleartoolException, IOException, ViewException, UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return stream;
	}
	
	public void end() throws ViewException {
		end( viewtag );
	}
	
	public static void end( String viewtag ) throws ViewException {
		try {
			Cleartool.run( "endview -server " + viewtag );
		} catch( Exception e ) {
			throw new ViewException( "Could not end view " + viewtag, null, Type.END_VIEW_FAILED, e );
		}
	}
	
	public static void getViews( Project project ) {
		// cleartool lsstream -in project:ava2@\chw_PVOB
		// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_lsstream.htm
	}
	
	protected static void addView( String viewTag, UCMView view ) {
		createdViews.put( viewTag, view );
	}
	
	public static UCMView getView( String viewTag ) throws ViewException {
		if( createdViews.containsKey( viewTag ) ) {
			return createdViews.get( viewTag );
		} else {
			throw new ViewException( "", null, Type.DOES_NOT_EXIST, null );
		}
	}
	
	public static Map<String, UCMView> getViews() {
		return createdViews;
	}
	
	
	
	public String toString() {
		return viewtag;
	}
	
	protected static void create( String tag, String path, boolean snapshotView, Stream stream ) throws ViewException {
		logger.debug( "Creating " + tag );
		String cmd = "mkview -tag " + tag + ( snapshotView ? " -snapshot" : "" ) + ( stream != null ? " -stream " + stream.getFullyQualifiedName() : "" ) + " -stgloc " + ( path != null ? path : "-auto" );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new ViewException( "Unable to create view " + tag, path, Type.CREATION_FAILED, e );
		}
	}

}