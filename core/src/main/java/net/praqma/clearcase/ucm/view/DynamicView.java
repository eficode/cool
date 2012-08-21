package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.debug.Logger;

public class DynamicView extends UCMView {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	private static Logger logger = Logger.getLogger();

	public DynamicView() {
tracer.entering(DynamicView.class.getSimpleName(), "DynamicView");

tracer.exiting(DynamicView.class.getSimpleName(), "DynamicView");
	}

	public DynamicView( String path ) {
tracer.entering(DynamicView.class.getSimpleName(), "DynamicView", new Object[]{path});
		super( path );
		this.dynamic = true;
tracer.exiting(DynamicView.class.getSimpleName(), "DynamicView");
	}

	public DynamicView( String path, String viewtag ) {
tracer.entering(DynamicView.class.getSimpleName(), "DynamicView", new Object[]{path, viewtag});
		super( path, viewtag );
		this.dynamic = true;
tracer.exiting(DynamicView.class.getSimpleName(), "DynamicView");
	}

	public DynamicView( String path, String viewtag, Stream stream ) {
tracer.entering(DynamicView.class.getSimpleName(), "DynamicView", new Object[]{path, viewtag, stream});
		super( path, viewtag, stream );
		this.dynamic = true;
tracer.exiting(DynamicView.class.getSimpleName(), "DynamicView");
	}

	/**
	 * Creates a dynamic view in the given path. If path is null -auto is used
	 * 
	 * @param tagTag
	 *            The view tag
	 * @param stgloc
	 *            The path
	 * @return An instance of DynamicView
	 * @throws ViewException
	 */
	public static DynamicView create( String stgloc, String tagTag, Stream stream ) throws ViewException {
tracer.entering(DynamicView.class.getSimpleName(), "create", new Object[]{stgloc, tagTag, stream});
		//context.createView(tag, path, false, stream);
		UCMView.create( tagTag, stgloc, false, stream );
		DynamicView view = new DynamicView( stgloc, tagTag, stream );
tracer.exiting(DynamicView.class.getSimpleName(), "create", view);
		return view;
	}

	public void startView() throws ViewException {
tracer.entering(DynamicView.class.getSimpleName(), "startView");
		try {
			Cleartool.run( "startview " + getViewtag() );
		} catch( Exception e ) {
			throw new ViewException( "Could not start view " + getViewtag(), path, Type.START_VIEW_FAILED, e );
		}
tracer.exiting(DynamicView.class.getSimpleName(), "startView");
	}
}
