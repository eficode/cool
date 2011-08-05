package net.praqma.clearcase.ucm.view;

import net.praqma.clearcase.ucm.UCMException;

public class DynamicView extends UCMView {
	
	public DynamicView() {
		
	}
	
	public DynamicView( String path ) {
		super(path);
	}
	
	/**
	 * Creates a dynamic view in the given path. If path is null -auto is used
	 * @param tag The view tag
	 * @param path The path
	 * @return An instance of DynamicView
	 * @throws UCMException
	 */
	public static DynamicView create( String tag, String path ) throws UCMException {
		context.createView(tag, path, false);
		return new DynamicView(path);
	}
}
