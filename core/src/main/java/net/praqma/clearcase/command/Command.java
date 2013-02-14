package net.praqma.clearcase.command;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: cwolfgang
 * Date: 09-11-12
 * Time: 10:58
 */
public abstract class Command<T> {

    protected StringBuilder cmd = new StringBuilder();
    protected List<String> cmdList = new ArrayList<String>();

    protected File path;
    protected boolean merge = false;
    protected boolean ignore = true;

    /* Results */
    protected CmdResult result;

    public File getPath() {
        return path;
    }

    public Command<T> setPath( File path ) {
        this.path = path;

        return this;
    }

    public boolean doMerge() {
        return merge;
    }

    public Command<T> setMerge( boolean merge ) {
        this.merge = merge;

        return this;
    }

    public boolean doIgnore() {
        return ignore;
    }

    public Command<T> setIgnore( boolean ignore ) {
        this.ignore = ignore;

        return this;
    }

    public abstract T get();

    public String getCommand() {
        StringBuilder sb = new StringBuilder();

        if( cmdList.size() > 1 ) {
            for( int i = 0 ; i < cmdList.size() ; i++ ) {
                String s = cmdList.get( i );
                sb.append( s );
            }
        }

        return sb.toString();
    }

    public Command<T> execute() {
        result = Cleartool.run( cmd.toString(), path, merge, ignore );

        return this;
    }
}
