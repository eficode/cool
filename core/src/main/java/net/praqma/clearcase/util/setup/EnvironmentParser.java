package net.praqma.clearcase.util.setup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;

public class EnvironmentParser extends XML {
	
	private static Logger logger = Logger.getLogger();
	
	private static Map<String, AbstractTask> map = new HashMap<String, AbstractTask>();
	
	static {
		map.put( "activity", new ActivityTask() );
		map.put( "add", new AddElementTask() );
		map.put( "baseline", new BaselineTask() );
		map.put( "checkin", new CheckinTask() );
		map.put( "checkout", new CheckoutTask() );
		map.put( "component", new ComponentTask() );
		map.put( "content", new ContentTask() );
		map.put( "context", new ContextTask() );
		map.put( "folder", new FolderTask() );
		map.put( "hlink", new HLinkTask() );
		map.put( "project", new ProjectTask() );
		map.put( "vob", new VobTask() );
		map.put( "setactivity", new SetActivityTask() );
		map.put( "stream", new StreamTask() );
		map.put( "var", new VariableTask() );
		map.put( "view", new ViewTask() );
	}
	
	public static class Context {
		public File path;
		public Map<String, Value> variables = new HashMap<String, Value>();
		
		public List<PVob> pvobs = new ArrayList<PVob>();
		
		/* Streams */
		public List<Stream> streams = new ArrayList<Stream>();
		public List<Stream> integrationStreams = new ArrayList<Stream>();
		
		public List<Project> projects = new ArrayList<Project>();
		
		public List<Component> components = new ArrayList<Component>();
		
		public List<Baseline> baselines = new ArrayList<Baseline>();
		
		public Map<String, UCMView> views = new HashMap<String, UCMView>();
		
		public void put( String key, String value ) {
			if( variables.containsKey( key ) ) {
				/* Only overwrite if not fixed */
				if( !variables.get( key ).fixed ) {
					variables.put( key, new Value( value ) );
				}					
			} else {
				variables.put( key, new Value( value ) );
			}
		}
	}
	
	public static class Value {
		public String value;
		public boolean fixed = false;
		
		public Value( String value ) {
			this.value = value;
		}
		
		public Value( String value, boolean fixed ) {
			this.value = value;
			this.fixed = fixed;
		}
		
		public String toString() {
			return value + "(" + fixed + ")";
		}
	}
	
	public EnvironmentParser( File file ) throws IOException {
		super( file );
	}
	
	public EnvironmentParser( String xml ) throws IOException {
		super( new ByteArrayInputStream( xml.getBytes() ) );
	}
	
	private void insertVariables( Context context, Map<String, String> variables ) {
		Set<String> keys = variables.keySet();
		for( String key : keys ) {
			context.variables.put( key, new Value( variables.get( key ), true ) );
		}
	}
	
	public Context parse() {
		return parse( new HashMap<String, String>() );
	}
	
	public Context parse( Map<String, String> variables ) {
		
		Element env = getRoot();
		
		List<Element> elements = getElements( env );
		
		Context context = new Context();
		
		insertVariables( context, variables );
		
		for( Element e : elements ) {
			String tag = e.getTagName();
			logger.verbose( "Parsing " + tag );
			try {
				if( tag != null && tag.length() > 0 ) {
					map.get( tag ).parse( e, context );
				} else {
					logger.debug( "Not handling anonymous tags" );
				}
			} catch( ClearCaseException e1 ) {
				ExceptionUtils.print( e1, System.out, true );
				return null;
			} catch( Exception e1 ) {
				logger.fatal( "Failed to parse: " + e1.getMessage() );
				return null;
			}
		}
		
		return context;
	}
	
	
}
