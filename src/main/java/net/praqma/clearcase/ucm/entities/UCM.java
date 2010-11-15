package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyXML;
import net.praqma.utils.Debug;

public abstract class UCM
{
	/* Make sure, that we're using the same instance of the context! */
	protected static UCMContext context = new UCMContext( new UCMStrategyXML() );
	
	protected static Debug logger = Debug.GetLogger( false );
	
	protected static final String filesep = System.getProperty( "file.separator" );
	protected static final String linesep = System.getProperty( "line.separator" );
	public static final String delim      = "::";
}