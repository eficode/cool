package net.praqma.clearcase.api;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Base implementation of a ClearCase api command.
 *
 * @author cwolfgang
 */
public abstract class Command<RT extends Object> {

    /**
     * The {@link File} path to where this command is executed.<br />
     * To change the path, it must be done in the actual implementation.
     */
    protected File root;

    protected CmdResult runCommand() throws CleartoolException {
        String cmd = getCommandLine();

        try {
            return Cleartool.run( cmd, root, false );
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Unable to run " + getCommand(), e );
        }
    }

    public abstract RT execute() throws CleartoolException;

    public abstract String getCommandLine();

    public abstract String getCommand();

    private static Pattern pattern = Pattern.compile( "\\s" );
    
    public static boolean hasSpecialCharacters( String str ) {
        return str.contains("&");
    }

    public static boolean hasWhiteSpaces( String str ) {
        Matcher matcher = pattern.matcher( str );
        return matcher.find();
    }
}
