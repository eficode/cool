package net.praqma.clearcase.command;

import net.praqma.clearcase.Vob;

import java.util.logging.Logger;

/**
 * User: cwolfgang
 * Date: 09-11-12
 * Time: 11:02
 */
public class UnmountVob extends Command<Boolean> {

    private static Logger logger = Logger.getLogger( UnmountVob.class.getName() );

    private Vob vob;

    public UnmountVob( Vob vob ) {
        this.vob = vob;
    }

    public UnmountVob() {
        this.cmd.append( "umount " + vob );
    }

    @Override
    public Boolean get() {
        return true;
    }
}
