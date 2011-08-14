package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Stream;

public class DynamicView extends UCMView {
	
	public DynamicView() {
		
	}
	
	public DynamicView( String path ) {
		super(path);
		this.dynamic = true;
	}
	
	public DynamicView( String path, String viewtag ) {
		super(path, viewtag);
		this.dynamic = true;
	}
	
	public DynamicView( String path, String viewtag, Stream stream ) {
		super(path, viewtag, stream);
		this.dynamic = true;
	}
	
	/**
	 * Creates a dynamic view in the given path. If path is null -auto is used
	 * @param tag The view tag
	 * @param path The path
	 * @return An instance of DynamicView
	 * @throws UCMException
	 */
	public static DynamicView create( String path, String tag, Stream stream ) throws UCMException {
		context.createView(tag, path, false, stream);
		return new DynamicView(path, tag, stream);
	}
	
	
	public void startView() throws UCMException {
		context.startView( this );
	}
}
