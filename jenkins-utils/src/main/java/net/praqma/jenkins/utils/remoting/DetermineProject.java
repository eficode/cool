package net.praqma.jenkins.utils.remoting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Project;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

public class DetermineProject implements FileCallable<Project> {
	
	private List<String> projects;
	private PVob pvob;
	
	public DetermineProject( List<String> projects, PVob pvob ) {
		this.projects = projects;
		this.pvob = pvob;
	}

	@Override
	public Project invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
		for( String project : projects ) {
			try {
				Project ucmproject = Project.get( project, pvob, false );
				return ucmproject;
			} catch( ClearCaseException e ) {
				/* Not a valid project */
			} catch( NullPointerException e ) {
				/* project was probably null, which is allowable */
			}
		}		
		
		throw new IOException( "No such project" );
	}

}
