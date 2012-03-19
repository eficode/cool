package net.praqma.jenkins.utils.remoting;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.UCMEntity;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;

public class LoadEntity implements FileCallable<UCMEntity> {

	private UCMEntity entity;
	
	public LoadEntity( UCMEntity entity ) {
		this.entity = entity;
    }
    
    @Override
    public UCMEntity invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
        
    	try {
			entity.load();
		} catch (ClearCaseException e) {
        	throw new IOException( "Unable to load " + entity.getShortname() + ":" + e.getMessage(), e );
		}

    	return entity;
    }

}
