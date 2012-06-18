package net.praqma.clearcase.exceptions;

import net.praqma.util.execute.AbnormalProcessTerminationException;

public class NoLicenseServerException extends AbnormalProcessTerminationException {
	
	public NoLicenseServerException( String s, AbnormalProcessTerminationException e ) {
		super( s, e.getCommand(), e.getExitValue(), e );
	}

}