package net.praqma.clearcase.command;

import net.praqma.clearcase.Vob;

import java.util.logging.Logger;

/**
 * User: cwolfgang
 * Date: 09-11-12
 * Time: 11:02
 */
public class RemoveVob extends Command<Boolean> {

    private static Logger logger = Logger.getLogger( RemoveVob.class.getName() );

    private Vob vob;

    public RemoveVob( Vob vob ) {
        this.vob = vob;
    }

    public RemoveVob() {
        this.cmd.append( "rmvob -force " + vob.getStorageLocation() );
    }

    @Override
    public Boolean get() {
        return true;
    }
}
