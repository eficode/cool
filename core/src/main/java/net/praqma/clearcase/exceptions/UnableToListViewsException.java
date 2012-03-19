package net.praqma.clearcase.exceptions;

import java.io.File;

public class UnableToListViewsException extends ClearCaseException {

	private String viewtag;
	private File context;
	
	public UnableToListViewsException( String viewtag, File context, Exception e ) {
		super( e );
		this.viewtag = viewtag;
		this.context = context;
	}

	public File getContext() {
		return context;
	}
	
	public String getViewtag() {
		return viewtag;
	}
}