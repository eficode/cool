package net.praqma.jenkins.utils.remoting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity.LabelStatus;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.Baselines;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.Logger.LogLevel;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.FileAppender;
import net.praqma.util.debug.appenders.StreamAppender;

import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

/**
 * List baselines
 * 
 * @author wolfgang
 *
 */
public class GetBaselines implements FileCallable<List<Baseline>> {

	private TaskListener listener;
	private Component component;
	private Stream stream;
	private PromotionLevel plevel;
	
	private int max;

	private Date date;
	private Baseline after;
	
	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;
		
		this.max = max;
	}

	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max, Date date ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.date = date;
		
		this.max = max;
	}

	/**
	 * Retrieve a list of {@link Baseline}s after a given {@link Baseline} in chronological order
	 * @param listener
	 * @param component
	 * @param stream
	 * @param plevel
	 * @param max
	 * @param after
	 */
	public GetBaselines( TaskListener listener, Component component, Stream stream, PromotionLevel plevel, int max, Baseline after ) {
		this.listener = listener;

		this.component = component;
		this.stream = stream;
		this.plevel = plevel;

		this.after = after;
		this.max = max;
	}

	@Override
	public List<Baseline> invoke( File workspace, VirtualChannel channel ) throws IOException, InterruptedException {
		PrintStream out = listener.getLogger();
		
		try {
			return Baselines.get( component, stream, plevel, max, after );
		} catch( Exception e ) {
			throw new IOException( "Unable to get baselines", e );
		}

	}

}
