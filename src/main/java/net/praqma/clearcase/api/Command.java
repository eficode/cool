package net.praqma.clearcase.api;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

import java.io.File;

/**
 * @author cwolfgang
 */
public abstract class Command<RT extends Object> {

    protected File root;

    protected CmdResult runCommand() throws CleartoolException {
        String cmd = getCommandLine();

        try {
            return Cleartool.run( cmd, root );
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Error while finding", e );
        }
    }

    public abstract RT execute() throws CleartoolException;

    public abstract String getCommandLine();
}
