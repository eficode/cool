package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.ArrayList;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.utils.BaselineDiff;

import net.praqma.clearcase.ucm.view.SnapshotView;

public class Baseline extends UCMEntity {
    /* Baseline specific fields */

    private Component component = null;
    private Project.Plevel plevel = Project.Plevel.INITIAL;
    private Stream stream = null;
    private ArrayList<Activity> activities = null;

    /**
     * Nested public class for easy compilation and access of the differences Activities and Versions.
     * @author wolfgang
     *
     */
    Baseline() {
    }

    /**
     * Load the Baseline into memory from ClearCase.<br>
     * This function is automatically called when needed by other functions.
     */
    public void load() throws UCMException {
        String[] rs = context.loadBaseline(this);

        /* Component */
        String c = (rs[1].matches("^component:.*$") ? "" : "component:") + (rs[1].matches(".*@\\\\.*$") ? rs[1] : rs[1] + "@" + this.pvob);
        logger.debug("Component = " + c);
        /* Stream */
        if (rs[2].trim().length() > 0) {
            String s = (rs[2].matches("^stream:.*$") ? "" : "stream:") + (rs[2].matches(".*@\\\\.*$") ? rs[2] : rs[2] + "@" + this.pvob);
            logger.debug("Stream = " + s);
            this.stream = (Stream) UCMEntity.getEntity(s);
        } else {
            logger.warning("The stream was not set. Propably because the baseline was INITIAL.");
        }

        /* Now with factory creation! */
        this.component = (Component) UCMEntity.getEntity(c);
        this.plevel = Project.getPlevelFromString(rs[3]);
        this.user = rs[4];

        activities = new ArrayList<Activity>();

        this.loaded = true;
    }

    /**
     * Given a baseline basename, a component and a view, the baseline is created.
     * @param basename The basename of the Baseline. Without the vob.
     * @param component
     * @param view
     * @param incremental
     * @param identical
     * @return Baseline
     * @throws UCMException
     */
    public static Baseline create(String basename, Component component, File view, boolean incremental, boolean identical) throws UCMException {
        return create( basename, component, view, incremental, identical, null );
    }
    
    public static Baseline create(String basename, Component component, File view, boolean incremental, boolean identical, Component[] depends) throws UCMException {
        if (basename.toLowerCase().startsWith("baseline:")) {
            logger.warning("The baseline name should not be prefixed with \"baseline:\", removing it");
            basename = basename.replaceFirst("baseline:", "");
        }

        context.createBaseline(basename, component, view, incremental, identical, depends);

        return UCMEntity.getBaseline(basename + "@" + component.getPvobString(), true);
    }

    /**
     * This method is only available to the package, because only ClearcaseEntity should
     * be allowed to call it.
     * @return A new Baseline Entity
     */
    static Baseline getEntity() {
        return new Baseline();
    }

    /**
     * Return the promotion level of a baseline. <br>
     * If <code>cached</code> is not set, the promotion level is loaded from ClearCase.
     * @param cached Whether to use the cached promotion level or not
     * @return The promotion level of the Baseline
     */
    public Project.Plevel getPromotionLevel(boolean cached) throws UCMException {
        if (!loaded) {
            this.load();
        }
        //TODO if !loaded return this.plevel DONE.....
        if (cached) {
            return this.plevel;
        } else {
            /* TODO Get from clear case, uses cached value */
            /* If different from cached, cache the new */
            return this.plevel;
        }
    }

    /**
     * Promote the Baseline.
     * <ul>
     * <li><code>INITIAL -> BUILT</code></li>
     * <li><code>BUILD&nbsp;&nbsp; -> TESTED</code></li>
     * <li><code>TESTED&nbsp; -> RELEASED</code></li>
     * </ul>
     *
     * If the promotion level is not set, it is set to <code>INITAL</code>.
     * @return The new promotion level.
     */
    public Project.Plevel promote() throws UCMException {
        if (!loaded) {
            this.load();
        }

        if (this.plevel.equals(Plevel.REJECTED)) {
            throw new UCMException("Cannot promote from REJECTED");
        }

        this.plevel = Project.promoteFrom(this.plevel);

        context.setPromotionLevel(this);

        return this.plevel;
    }

    /**
     * Demotes the Baseline to <code>REJECTED</code>.
     */
    public Project.Plevel demote() throws UCMException {
        if (!loaded) {
            this.load();
        }

        this.plevel = Project.Plevel.REJECTED;

        context.setPromotionLevel(this);

        return Project.Plevel.REJECTED;
    }

    public void setPromotionLevel(Project.Plevel plevel) {
        this.plevel = plevel;
    }

    /**
     * Get the differences between two Baselines.<br>
     * Currently this method only support the previous Baseline and with -nmerge set.<br>
     * @return A BaselineDiff object containing a set of Activities.
     */
    public BaselineDiff getDifferences(SnapshotView view) throws UCMException {
        return new BaselineDiff(view, this);
    }

    public Component getComponent() throws UCMException {
        if (!loaded) {
            load();
        }
        return this.component;
    }

    public Stream getStream() throws UCMException {
        if (!loaded) {
            load();
        }
        return this.stream;
    }

    public boolean deliverForced(Stream stream, Stream target, File viewcontext, String viewtag) throws UCMException {
        //logger.info( "Trying to deliver the Baseline " + this.GetFQName() + " from " + stream.GetFQName() + " to " + target.GetFQName() );

        try {
            context.deliver(this, stream, target, viewcontext, viewtag, true, true, true);
        } catch (UCMException e) {
            logger.warning("Could not deliver baseline: " + e.getMessage());
            throw e;
        }

        return true;
    }

    /**
     * Deliver a {@link Baseline} from a {@link Stream} stream to a {@link Stream} target.
     * @param stream The source {@link Stream}
     * @param target The target {@link Stream}
     * @param viewcontext The view context as a {@link File}
     * @param viewtag A view tag
     * @param force
     * @param complete
     * @param abort
     * @return boolean
     * @throws UCMException
     */
    public boolean deliver(Stream stream, Stream target, File viewcontext, String viewtag, boolean force, boolean complete, boolean abort) throws UCMException {
        try {
            return context.deliver(this, stream, target, viewcontext, viewtag, force, complete, abort);
        } catch (UCMException e) {
            logger.warning("Could not deliver baseline: " + e.getMessage());
            logger.warning(e);
            throw e;
        }
    }

    public void cancel(File viewcontext) throws UCMException {
        try {
            context.cancelDeliver(viewcontext);
        } catch (UCMException ex) {
            logger.error(ex.getMessage());
            throw ex;
        }
    }

    public String stringify() throws UCMException {
        if (!this.loaded) {
            load();
        }

        StringBuffer sb = new StringBuffer();

        sb.append(super.stringify());

        sb.append("PLEVEL   : " + this.plevel + linesep);
        sb.append("Component: " + this.component.toString() + linesep);
        sb.append("Stream   : " + this.stream.toString() + linesep);

        return sb.toString();
    }
}
