package net.praqma.clearcase.ucm.view;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCM;

public class UCMView extends UCM
{
	public static SnapshotView GetSnapshotView( File viewroot )
	{
		return new SnapshotView( viewroot );
	}
}