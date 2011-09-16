package net.praqma.clearcase.ucm;

import net.praqma.clearcase.Cool;
import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
public class UCMException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5325867242379727760L;
	transient private static Logger logger = Logger.getLogger();
	public UCMType type = UCMType.DEFAULT;

	public String stdout = null;

	public enum UCMType {
		DEFAULT, 
		ENTITY_ERROR, 
		ENTITY_NAME_ERROR, 
		EXISTENCE, 
		HLINK_ZERO_MATCHES, 
		LOAD_FAILED, 
		TAG_CREATION_FAILED, 
		UNKNOWN_HLINK_TYPE, 
		VIEW_ERROR, 
		CREATION_FAILED,
		DELIVER_REQUIRES_REBASE,
		INTERPROJECT_DELIVER_DENIED,
		MERGE_ERROR,
		DELIVER_IN_PROGRESS,
		UNKOWN_VOB,
		UNKNOWN_USER,
		ENTITY_NOT_FOUND
	}

	public UCMException() {
		super();
	}

	public UCMException(String s) {
		super(s);
	}

	public UCMException(String s, String stdout) {
		super(s);
		this.stdout = stdout;
	}

	public UCMException(String s, UCMType type) {
		super(s);

		this.type = type;
	}

	public UCMException(String s, String stdout, UCMType type) {
		super(s);

		this.type = type;
		this.stdout = stdout;
	}

}