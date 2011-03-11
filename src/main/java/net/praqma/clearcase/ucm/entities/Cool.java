package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public abstract class Cool
{
	public static Logger logger = PraqmaLogger.getLogger( false );
	public static void setLogger( Logger logger )
	{
		Cool.logger = PraqmaLogger.getLogger( logger );
		Cleartool.setCLILogger( Cool.logger );
	}
}
