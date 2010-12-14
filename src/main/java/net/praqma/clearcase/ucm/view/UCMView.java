package net.praqma.clearcase.ucm.view;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;

public class UCMView extends UCM
{
	public static SnapshotView GetSnapshotView( File viewroot ) throws UCMException
	{
		return new SnapshotView( viewroot );
	}
	
	public static boolean ViewExists( String viewtag )
	{
		boolean b = context.ViewExists( viewtag );
		logger.debug( "The view " + viewtag + " exists: " + b );
		return b;
	}

}