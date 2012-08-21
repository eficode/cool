package net.praqma.clearcase.ucm.view;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CmdResult;

public class UCMView extends ClearCase implements Serializable {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public static final Pattern rx_view_get_path = Pattern.compile( "^\\s*Global path:\\s*(.*?)\\s*$" );

	transient private static Logger logger = Logger.getLogger();
	
	private static Map<String, UCMView> createdViews = new HashMap<String, UCMView>();

	protected String path;
	protected String viewtag = "";
	protected Stream stream = null;
	
	protected String storageLocation = null;
	
	protected boolean dynamic = false;

	public UCMView() {
tracer.entering(UCMView.class.getSimpleName(), "UCMView");
tracer.exiting(UCMView.class.getSimpleName(), "UCMView");
	}

	public UCMView(String path) {
tracer.entering(UCMView.class.getSimpleName(), "UCMView", new Object[]{path});
		this.path = path;
tracer.exiting(UCMView.class.getSimpleName(), "UCMView");
	}
	
	public UCMView(String path, String viewtag) {
tracer.entering(UCMView.class.getSimpleName(), "UCMView", new Object[]{path, viewtag});
		this.path = path;
		this.viewtag = viewtag;
tracer.exiting(UCMView.class.getSimpleName(), "UCMView");
	}
	
	public UCMView(String path, String viewtag, Stream stream) {
tracer.entering(UCMView.class.getSimpleName(), "UCMView", new Object[]{path, viewtag, stream});
		this.path = path;
		this.viewtag = viewtag;
		this.stream = stream;
tracer.exiting(UCMView.class.getSimpleName(), "UCMView");
	}

	public static boolean viewExists(String viewtag) {
tracer.entering(UCMView.class.getSimpleName(), "viewExists", new Object[]{viewtag});
		//boolean b = context.viewExists(viewtag);
		
		logger.debug( viewtag );

		String cmd = "lsview " + viewtag;

		try {
			String s = Cleartool.run( cmd ).stdoutBuffer.toString();
			logger.debug( viewtag + " exists" );
tracer.exiting(UCMView.class.getSimpleName(), "viewExists", true);
			return true;
		} catch( Exception e ) {
			logger.debug( "View check failed: " + e.getMessage() );
tracer.exiting(UCMView.class.getSimpleName(), "viewExists", false);
			return false;
		}
	}

	public void setPath(String path) {
tracer.entering(UCMView.class.getSimpleName(), "setPath", new Object[]{path});
		this.path = path;
tracer.exiting(UCMView.class.getSimpleName(), "setPath");
	}

	public String getPath() {
tracer.entering(UCMView.class.getSimpleName(), "getPath");
tracer.exiting(UCMView.class.getSimpleName(), "getPath", path);
		return path;
	}
	
	public String getViewtag() {
tracer.entering(UCMView.class.getSimpleName(), "getViewtag");
tracer.exiting(UCMView.class.getSimpleName(), "getViewtag", this.viewtag);
		return this.viewtag;
	}
	
	public void remove() throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "remove");
		//context.removeView( this );
		String cmd = "rmview -force " + ( isDynamicView() ? "-tag " + viewtag : ( storageLocation != null ? storageLocation : "-tag " + viewtag ) );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new ViewException( "Unable to remove " + this, path, Type.REMOVE_FAILED, e );
		}
tracer.exiting(UCMView.class.getSimpleName(), "remove");
	}
	
	public boolean isDynamicView() {
tracer.entering(UCMView.class.getSimpleName(), "isDynamicView");
tracer.exiting(UCMView.class.getSimpleName(), "isDynamicView", this.dynamic);
		return this.dynamic;
	}
	
	public UCMView load() throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "load");
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
		
tracer.exiting(UCMView.class.getSimpleName(), "load", this);
		return this;
	}
	
	public String getStorageLocation() {
tracer.entering(UCMView.class.getSimpleName(), "getStorageLocation");
tracer.exiting(UCMView.class.getSimpleName(), "getStorageLocation", this.storageLocation);
		return this.storageLocation;
	}
	
	public Stream getStream() throws UnableToInitializeEntityException, CleartoolException, ViewException, IOException {
tracer.entering(UCMView.class.getSimpleName(), "getStream");
tracer.exiting(UCMView.class.getSimpleName(), "getStream", stream);
		return stream;
	}
	
	public UCMView end() throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "end");
		end( viewtag );
tracer.exiting(UCMView.class.getSimpleName(), "end", this);
		return this;
	}
	
	public static void end( String viewtag ) throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "end", new Object[]{viewtag});
		try {
			Cleartool.run( "endview -server " + viewtag );
		} catch( Exception e ) {
			throw new ViewException( "Could not end view " + viewtag, null, Type.END_VIEW_FAILED, e );
		}
tracer.exiting(UCMView.class.getSimpleName(), "end");
	}
	
	public UCMView start() throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "start");
		start( viewtag );
tracer.exiting(UCMView.class.getSimpleName(), "start", this);
		return this;
	}
	
	public static void start( String viewtag ) throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "start", new Object[]{viewtag});
		try {
			Cleartool.run( "startview " + viewtag );
		} catch( Exception e ) {
			throw new ViewException( "Could not start view " + viewtag, null, Type.END_VIEW_FAILED, e );
		}
tracer.exiting(UCMView.class.getSimpleName(), "start");
	}
	
	public static void getViews( Project project ) {
tracer.entering(UCMView.class.getSimpleName(), "getViews", new Object[]{project});
		// cleartool lsstream -in project:ava2@\chw_PVOB
		// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_lsstream.htm
tracer.exiting(UCMView.class.getSimpleName(), "getViews");
	}
	
	public static UCMView getView( String viewTag ) throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "getView", new Object[]{viewTag});
		if( viewTag.trim().equals( "" ) ) {
			throw new ViewException( "View tag cannot be empty", "", Type.EMPTY );
		}
		
		UCMView v = new UCMView();
		v.viewtag = viewTag;
		
tracer.exiting(UCMView.class.getSimpleName(), "getView", v);
		return v;
	}
	
	
	public String toString() {
tracer.entering(UCMView.class.getSimpleName(), "toString");
tracer.exiting(UCMView.class.getSimpleName(), "toString", viewtag);
		return viewtag;
	}
	
	protected static void create( String tag, String stgloc, boolean snapshotView, Stream stream ) throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "create", new Object[]{tag, stgloc, snapshotView, stream});
		logger.debug( "Creating " + tag );

		String cmd = "mkview -tag " + tag + ( snapshotView ? " -snapshot" : "" ) + ( stream != null ? " -stream " + stream.getFullyQualifiedName() : "" ) + " -stgloc " + ( stgloc != null ? stgloc : "-auto" );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new ViewException( "Unable to create view " + tag, stgloc, Type.CREATION_FAILED, e );
		}
tracer.exiting(UCMView.class.getSimpleName(), "create");
	}
	
	public static void setActivity( Activity activity, File context, String viewtag, String comment ) throws ViewException {
tracer.entering(UCMView.class.getSimpleName(), "setActivity", new Object[]{activity, context, viewtag, comment});
		String cmd = "setactivity " + ( comment != null ? "-c \"" + comment + "\" " : "-nc " ) +
									  ( viewtag != null ? "-viewtag " + viewtag + " " : "" ) + 
									  ( activity != null ? activity.getNormalizedName() : "-none" );

		try {
			Cleartool.run( cmd, context );
		} catch( Exception e ) {
			throw new ViewException( "Unable to set activity " + activity, context.toString(), Type.ACTIVITY_FAILED, e );
		}
tracer.exiting(UCMView.class.getSimpleName(), "setActivity");
	}
	
	@Override
	public boolean equals( Object other ) {
tracer.entering(UCMView.class.getSimpleName(), "equals", new Object[]{other});
		if( other instanceof UCMView ) {
tracer.exiting(UCMView.class.getSimpleName(), "equals", ((UCMView)other).viewtag.equals( viewtag ));
			return ((UCMView)other).viewtag.equals( viewtag );
		} else {
tracer.exiting(UCMView.class.getSimpleName(), "equals", false);
			return false;
		}
	}
	
	@Override
	public int hashCode() {
tracer.entering(UCMView.class.getSimpleName(), "hashCode");
		int hash = 7;
		hash = 31 * hash + ( null == viewtag ? 0 : viewtag.hashCode() );
tracer.exiting(UCMView.class.getSimpleName(), "hashCode", hash);
		return hash;
	}

	@Override
	public String getFullyQualifiedName() {
tracer.entering(UCMView.class.getSimpleName(), "getFullyQualifiedName");
tracer.exiting(UCMView.class.getSimpleName(), "getFullyQualifiedName", viewtag);
		return viewtag;
	}

}