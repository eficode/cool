package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Folder;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.util.setup.EnvironmentParser.Context;

import org.w3c.dom.Element;

public class FolderTask extends AbstractTask {

	@Override
	public void parse( Element e, Context context ) throws ClearCaseException {
		String name = getValue( "name", e, context );
		String in = e.getAttribute( "in" );
		PVob pvob = new PVob( getValue( "pvob", e, context ) );
		String comment = UCMEntity.getargComment( e.getAttribute( "comment" ) );
		
		Folder.create( name, pvob, in, comment );
	}

}
