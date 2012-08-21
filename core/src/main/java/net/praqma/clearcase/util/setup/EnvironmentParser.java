package net.praqma.clearcase.util.setup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class EnvironmentParser extends XML {

	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

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

		map.put( "mkelem", new MakeElementTask() );
		map.put( "mkdir", new MakeDirectoryTask() );
	}

	public static class Context {
		public File path;

		public String mvfs;

		public Map<String, Value> variables = new HashMap<String, Value>();

		public Map<String, PVob> pvobs = new HashMap<String, PVob>();

		/* Streams */
		public Map<String, Stream> streams = new HashMap<String, Stream>();
		public Map<String, Stream> integrationStreams = new HashMap<String, Stream>();

		public Map<String, Project> projects = new HashMap<String, Project>();

		public Map<String, Component> components = new HashMap<String, Component>();

		public Map<String, Baseline> baselines = new HashMap<String, Baseline>();

		public Map<String, UCMView> views = new HashMap<String, UCMView>();

		public void put( String key, String value ) {
			tracer.entering(Context.class.getSimpleName(), "put", new Object[]{key, value});
			if( variables.containsKey( key ) ) {
				/* Only overwrite if not fixed */
				if( !variables.get( key ).fixed ) {
					variables.put( key, new Value( value ) );
				}					
			} else {
				variables.put( key, new Value( value ) );
			}
			tracer.exiting(Context.class.getSimpleName(), "put");
		}

		public String getVariable( String key ) {
			tracer.entering(Context.class.getSimpleName(), "getVariable", new Object[]{key});
			if( variables.containsKey( key ) ) {
				tracer.exiting(Context.class.getSimpleName(), "getVariable", variables.get( key ).value);
				return variables.get( key ).value;
			} else {
				tracer.exiting(Context.class.getSimpleName(), "getVariable", null);
				return null;
			}
		}

		public String getMvfs() {
			tracer.entering(Context.class.getSimpleName(), "getMvfs");
			tracer.exiting(Context.class.getSimpleName(), "getMvfs", mvfs);
			return mvfs;
		}

	}

	public static class Value {
		public String value;
		public boolean fixed = false;

		public Value( String value ) {
			tracer.entering(Value.class.getSimpleName(), "Value", new Object[]{value});
			this.value = value;
			tracer.exiting(Value.class.getSimpleName(), "Value");
		}

		public Value( String value, boolean fixed ) {
			tracer.entering(Value.class.getSimpleName(), "Value", new Object[]{value, fixed});
			this.value = value;
			this.fixed = fixed;
			tracer.exiting(Value.class.getSimpleName(), "Value");
		}

		public String toString() {
			return value + "(" + fixed + ")";
		}
	}

	public EnvironmentParser( File file ) throws IOException {
		super( file, true );
		tracer.entering(EnvironmentParser.class.getSimpleName(), "EnvironmentParser", new Object[]{file});
		tracer.exiting(EnvironmentParser.class.getSimpleName(), "EnvironmentParser");
	}

	public EnvironmentParser( String xml ) throws IOException {
		super( new ByteArrayInputStream( xml.getBytes() ), true );
		tracer.entering(EnvironmentParser.class.getSimpleName(), "EnvironmentParser", new Object[]{xml});
		tracer.exiting(EnvironmentParser.class.getSimpleName(), "EnvironmentParser");
	}

	private void insertVariables( Context context, Map<String, String> variables ) {
		tracer.entering(EnvironmentParser.class.getSimpleName(), "insertVariables", new Object[]{context, variables});
		Set<String> keys = variables.keySet();
		for( String key : keys ) {
			context.variables.put( key, new Value( variables.get( key ), true ) );
		}
		tracer.exiting(EnvironmentParser.class.getSimpleName(), "insertVariables");
	}

	public Context parse() throws Exception {
		tracer.entering(EnvironmentParser.class.getSimpleName(), "parse");
		tracer.exiting(EnvironmentParser.class.getSimpleName(), "parse", parse( new HashMap<String, String>() ));
		return parse( new HashMap<String, String>() );
	}

	public static final String ENVIRONMENTS_TAG = "ccenvs";
	public static final String ENVIRONMENT_TAG = "ccenv";

	public Context parse( Map<String, String> variables ) throws Exception {
		tracer.entering(EnvironmentParser.class.getSimpleName(), "parse", new Object[]{variables});

		Element rootenv = getRoot();

		List<Element> environments = new ArrayList<Element>();

		/* There are multiple environment tags, typically for including purposes */
		if( rootenv.getTagName().equals( ENVIRONMENTS_TAG ) ) {
			List<Element> envs = getElements( rootenv, ENVIRONMENT_TAG );
			environments.addAll( envs );
		} else {
			environments.add( rootenv );
		}

		Context context = new Context();

		/* For each environment */
		for( Element environment : environments ) {
			String ename = environment.getAttribute( "name" );
			logger.verbose( "Parsing environment " + ename );

			List<Element> elements = getElements( environment );

			insertVariables( context, variables );

			for( Element e : elements ) {
				String tag = e.getTagName();
				logger.verbose( "Parsing <" + tag + ">" );
				try {
					if( tag != null && tag.length() > 0 ) {
						map.get( tag ).parse( e, context );
					} else {
						logger.debug( "Not handling anonymous tags" );
					}
				} catch( ClearCaseException e1 ) {
					ExceptionUtils.print( e1, System.out, true );
					print( e, System.out );
					throw e1;
				} catch( Exception e1 ) {
					logger.fatal( "Failed to parse: " + e1.getMessage() );
					print( e, System.out );
					throw e1;
				}
			}
		}

		tracer.exiting(EnvironmentParser.class.getSimpleName(), "parse", context);
		return context;
	}

	private void print( Element e, PrintStream out ) {
		tracer.entering(EnvironmentParser.class.getSimpleName(), "print", new Object[]{e, out});
		out.println( "Name: " + e.getTagName() );

		NamedNodeMap map = e.getAttributes();

		out.println( "Attributes:" );
		for( int i = 0 ; i < map.getLength() ; ++i ) {
			out.println( " * [" + i + "] " + map.item( i ).getNodeName() + " = " + map.item( i ).getNodeValue() );
		}

		if( e.getTextContent() != null && e.getTextContent().length() > 0 ) {
			out.println( "Content: " + e.getTextContent() );
		} else {
			out.println( "Content: [Empty]" );
		}

		NodeList childs = e.getChildNodes();

		if( childs.getLength() > 0 ) {
			out.println( "Child nodes:" );
			for( int i = 0 ; i < childs.getLength() ; ++i ) {
				if( childs.item( i ).getNodeType() == Element.ELEMENT_NODE ) {
					print( (Element)childs.item( i ), out );
				}
			}
		} else {
			out.println( "Child nodes: [Empty]" );
		}

		out.println( "" );
		tracer.exiting(EnvironmentParser.class.getSimpleName(), "print");
	}


}
