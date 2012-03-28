package net.praqma.clearcase.util.setup;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Folder;
import net.praqma.clearcase.ucm.entities.UCMEntity;

import org.w3c.dom.Element;

public class FolderTask extends AbstractTask {

	@Override
	public void parse( Element e, File context ) throws ClearCaseException {
		String name = e.getAttribute( "name" );
		String in = e.getAttribute( "in" );
		PVob pvob = getPVob(  e.getAttribute( "pvob" ) );
		String comment = UCMEntity.getargComment( e.getAttribute( "comment" ) );
		
		Folder.create( name, pvob, in, comment );
	}

}
