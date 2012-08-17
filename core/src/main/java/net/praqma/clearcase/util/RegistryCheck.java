package net.praqma.clearcase.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.logging.Config;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.structure.Printer;

public abstract class RegistryCheck {

	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
    /* Matches stranded vobs */
    private static final Pattern rx_stranded_vobs = Pattern.compile( "rgy_check: Error: This VOB object is stranded(.*?)$$^", Pattern.MULTILINE | Pattern.DOTALL );
    /* Matches stranded views */
    private static final Pattern rx_stranded_views = Pattern.compile( "rgy_check: Error: This view object is stranded(.*?)$$^", Pattern.MULTILINE | Pattern.DOTALL );
    /* Matches modifiers */
    private static final Pattern rx_modifiers = Pattern.compile( "^\\s*-(\\w+)\\s*=\\s*\"(.*?)\"\\s*$", Pattern.MULTILINE );
    
    public static void main( String[] args ) {
    	tracer.entering(RegistryCheck.class.getSimpleName(), "main", args);
    	tracer.finest("Checking Views and Vobs");
    	
        List<Map<String, String>> viewInfo = checkViews();
        List<Map<String, String>> vobInfo = checkVobs();
        
        tracer.finest("Printing information for Views and Vobs");
        System.out.println( "Listing stranded views:" );
        Printer.listMapPrinter( viewInfo );
        System.out.println( "Listing stranded vobs:" );
        Printer.listMapPrinter( vobInfo );
        
        tracer.exiting(RegistryCheck.class.getSimpleName(), "main");
    }
    
    public static List<Map<String, String>> checkVobs() {
    	tracer.entering(RegistryCheck.class.getSimpleName(), "checkVobs");
    	
        String cmd = "rgy_check -vobs";
        
        List<Map<String, String>> vobInfo = new ArrayList<Map<String, String>>();

        tracer.finest(String.format("Running command: %s", cmd));
        
        CmdResult result = CommandLine.getInstance().run( cmd, null, true, true );
        Matcher m = rx_stranded_vobs.matcher( result.stdoutBuffer.toString() );
        
        tracer.finest("Searching command output for stranded Vobs.");
        while( m.find() ) {
            Matcher mi = rx_modifiers.matcher( m.group(1) );
            
            Map<String, String> r = new HashMap<String, String>();
            
            tracer.finest("Searching for modifiers.");
            while( mi.find() ) {
                r.put( mi.group(1), mi.group(2) );
            }
            
            vobInfo.add( r );
        }
        
        tracer.exiting(RegistryCheck.class.getSimpleName(), "checkVobs", vobInfo);
        return vobInfo;
    }
    
    public static List<Map<String, String>> checkViews() {
    	tracer.entering(RegistryCheck.class.getSimpleName(), "checkViews");
        String cmd = "rgy_check -views";
        
        List<Map<String, String>> viewInfo = new ArrayList<Map<String, String>>();
        
        tracer.finest(String.format("Running command: %s", cmd));

        CmdResult result = CommandLine.getInstance().run( cmd, null, true, true );
        Matcher m = rx_stranded_views.matcher( result.stdoutBuffer.toString() );
        
        tracer.finest("Searching command output for stranded Views.");
        while( m.find() ) {
            Matcher mi = rx_modifiers.matcher( m.group(1) );
            
            Map<String, String> r = new HashMap<String, String>();
            
            tracer.finest("Searching for modifiers.");
            while( mi.find() ) {
                r.put( mi.group(1), mi.group(2) );
            }
            
            viewInfo.add( r );
        }
        tracer.exiting(RegistryCheck.class.getSimpleName(), "checkViews", viewInfo);
        return viewInfo;
    }
}


/*

rgy_check: Error: This VOB object is stranded (it has no tags
in any region):
        -hostname = "pumpkin"
        -local_path = "D:\vobs\JBR_PVOB.2.vbs"
        -vob_replica = "37c67daf.c16645be.b961.9d:d3:e2:50:65:ec"
        -vob_family = "4b4fbe43.27be42de.9c51.4d:5c:53:84:ce:f5"
        -attributes = "sumvob"
 */