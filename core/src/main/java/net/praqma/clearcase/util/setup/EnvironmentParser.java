package net.praqma.clearcase.util.setup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.util.xml.XML;

public class EnvironmentParser extends XML {
	
	private static Map<String, AbstractTask> map = new HashMap<String, AbstractTask>();
	
	public static File context = null;
	
	static {
		map.put( "pvob", new PVobTask() );
		map.put( "hlink", new HLinkTask() );
	}
	
	public static void setContext( File context ) {
		EnvironmentParser.context = context;
	}
	
	public static File getContext() {
		return context;
	}

	public EnvironmentParser( File file ) throws IOException {
		super( file );
	}
	
	public boolean parse() {
		
		Element env = getFirstElement( "ccenv" );
		
		List<Element> elements = getElements( env );
		
		for( Element e : elements ) {
			String tag = e.getTagName();
			try {
				map.get( tag ).parse( e );
			} catch( ClearCaseException e1 ) {
				ExceptionUtils.print( e1, System.out, true );
				return false;
			}
		}
		
		return true;
	}
	
	
}
