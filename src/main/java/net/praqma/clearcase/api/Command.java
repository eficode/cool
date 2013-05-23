package net.praqma.clearcase.api;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

import java.io.File;

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
}
