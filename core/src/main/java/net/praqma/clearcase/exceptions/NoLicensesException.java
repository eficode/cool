package net.praqma.clearcase.exceptions;

import net.praqma.util.execute.AbnormalProcessTerminationException;

public class NoLicensesException extends AbnormalProcessTerminationException {
	
	public NoLicensesException( String s, AbnormalProcessTerminationException e ) {
		super( s, e.getCommand(), e.getExitValue(), e );
	}

}