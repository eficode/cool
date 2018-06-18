package net.praqma.clearcase.command;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.Vob;

import java.util.logging.Logger;

/**
 * User: cwolfgang
 * Date: 09-11-12
 * Time: 11:02
 */
@SuppressFBWarnings("")
public class RemoveVob extends Command<Boolean> {

    private static Logger logger = Logger.getLogger( RemoveVob.class.getName() );

    private Vob vob;

    public RemoveVob( Vob vob ) {
        this.vob = vob;

        this.cmd.append( "rmvob -force " + vob.getStorageLocation() );
    }

    @Override
    public Boolean get() {
        return true;
    }
}
