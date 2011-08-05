package net.praqma.clearcase.ucm.view;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;

public class UCMView extends UCM {

	private String path;

	public UCMView() {
	}

	public UCMView(String path) {
		this.path = path;
	}

	public static SnapshotView GetSnapshotView(File viewroot) throws UCMException {
		return new SnapshotView(viewroot);
	}

	public static boolean ViewExists(String viewtag) {
		boolean b = context.viewExists(viewtag);
		logger.debug("The view " + viewtag + " exists: " + b);
		return b;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

}