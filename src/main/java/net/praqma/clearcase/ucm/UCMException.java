package net.praqma.clearcase.ucm;

import net.praqma.clearcase.Cool;

//public class UCMException extends RuntimeException
public class UCMException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5325867242379727760L;
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
		DELIVER_REQUIRES_REBASE
	}

	public UCMException() {
		super();

		Cool.logger.exceptionWarning("Unnamed UCMException thrown");
	}

	public UCMException(String s) {
		super(s);

		Cool.logger.exceptionWarning(s);
	}

	public UCMException(String s, String stdout) {
		super(s);
		this.stdout = stdout;

		Cool.logger.exceptionWarning(s);
	}

	public UCMException(String s, UCMType type) {
		super(s);

		Cool.logger.exceptionWarning(s);

		this.type = type;
	}

	public UCMException(String s, String stdout, UCMType type) {
		super(s);

		Cool.logger.exceptionWarning(s);

		this.type = type;
		this.stdout = stdout;
	}

}