package net.praqma.clearcase.exceptions;

import net.praqma.util.execute.AbnormalProcessTerminationException;

public class CleartoolNotInstalledException extends AbnormalProcessTerminationException {
	
	public CleartoolNotInstalledException( String s, AbnormalProcessTerminationException e ) {
		super( s, e.getCommand(), e.getExitValue(), e );
	}

}