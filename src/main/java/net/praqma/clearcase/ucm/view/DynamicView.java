package net.praqma.clearcase.ucm.view;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Stream;


@SuppressFBWarnings("")
public class DynamicView extends UCMView {
    
	public DynamicView() { }

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
     * This is currently only used for testing purposes. 
	 * @param tagTag
	 *            The view tag
	 * @param stgloc
	 *            The path
     * @param stream The {@link Stream}
	 * @return An instance of DynamicView
	 * @throws ViewException Thrown when a ClearTool error occurs
	 */
	public static DynamicView create( String stgloc, String tagTag, Stream stream ) throws ViewException {
 
        try { 
            UCMView.create( tagTag, stgloc, false, stream );
        } catch (ViewException ex) {
            try {              
                Thread.sleep(10000);
                UCMView.create(tagTag, stgloc, false, stream);
            } catch (InterruptedException ex1) {
               throw ex;
            }
        } 
        
		DynamicView view = new DynamicView( stgloc, tagTag, stream );
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
