package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.debug.Logger;
import net.praqma.util.xml.XML;

public class EnvironmentParser extends XML {
	
	private static Logger logger = Logger.getLogger();
	
	private static Map<String, AbstractTask> map = new HashMap<String, AbstractTask>();
	
	static {
		map.put( "baseline", new BaselineTask() );
		map.put( "component", new ComponentTask() );
		map.put( "context", new ContextTask() );
		map.put( "folder", new FolderTask() );
		map.put( "hlink", new HLinkTask() );
		map.put( "project", new ProjectTask() );
		map.put( "vob", new VobTask() );
		map.put( "stream", new StreamTask() );
		map.put( "var", new VariableTask() );
		map.put( "view", new ViewTask() );
	}
	
	public static class Context {
		public File path;
		public Map<String, String> variables = new HashMap<String, String>();
		
		public List<PVob> pvobs = new ArrayList<PVob>();
	}
	
	public EnvironmentParser( File file ) throws IOException {
		super( file );
	}
	
	public Context parse() {
		
		Element env = getRoot();
		
		List<Element> elements = getElements( env );
		
		Context context = new Context();
		
		for( Element e : elements ) {
			String tag = e.getTagName();
			logger.verbose( "Parsing " + tag );
			try {
				map.get( tag ).parse( e, context );
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
