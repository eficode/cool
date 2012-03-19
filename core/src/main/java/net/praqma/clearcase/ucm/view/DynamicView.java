package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;

public class DynamicView extends UCMView {

	private static Logger logger = Logger.getLogger();

	public DynamicView() {

	}

	public DynamicView( String path ) {
		super( path );
		this.dynamic = true;
	}

	public DynamicView( String path, String viewtag ) {
		super( path, viewtag );
		this.dynamic = true;
	}

	public DynamicView( String path, String viewtag, Stream stream ) {
		super( path, viewtag, stream );
		this.dynamic = true;
	}

	/**
	 * Creates a dynamic view in the given path. If path is null -auto is used
	 * 
	 * @param tagTag
	 *            The view tag
	 * @param path
	 *            The path
	 * @return An instance of DynamicView
	 * @throws ViewException
	 */
	public static DynamicView create( String path, String tagTag, Stream stream ) throws ViewException {
		//context.createView(tag, path, false, stream);
		UCMView.create( tagTag, path, false, stream );
		DynamicView view = new DynamicView( path, tagTag, stream );
		addView( tagTag, view );
		return view;
	}

	public void startView() throws ViewException {
		try {
			Cleartool.run( "startview " + getViewtag() );
		} catch( Exception e ) {
			throw new ViewException( "Could not start view " + getViewtag(), path, Type.START_VIEW_FAILED, e );
		}
	}
}
