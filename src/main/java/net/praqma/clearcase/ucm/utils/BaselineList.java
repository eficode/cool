package net.praqma.clearcase.ucm.utils;

import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import org.apache.commons.lang.SystemUtils;

@SuppressFBWarnings("")
public class BaselineList extends ArrayList<Baseline> {

    private static Logger logger = Logger.getLogger(BaselineList.class.getName());
    private List<BaselineFilter> filters = new ArrayList<BaselineFilter>();
    private Comparator<Baseline> sorter;
    private boolean load = false;
    private Stream stream;
    private Component component;
    private PromotionLevel level;
    private boolean multisitePolling;
    private int limit = 0;
    private List<Baseline> required = new LinkedList<Baseline>();

    public BaselineList() { }

    public BaselineList(Stream stream, Component component, PromotionLevel plevel) {
        this(stream, component, plevel, false);
    }

    public BaselineList(Stream stream, Component component, PromotionLevel plevel, boolean multisitePolling) {
        this.stream = stream;
        this.component = component;
        this.level = plevel;
        this.multisitePolling = multisitePolling;
    }

    /**
     * Create a {@link BaselineList} object from a list of {@link Baseline}s
     *
     * @param baselines - A list of {@link Baseline}s
     */
    public BaselineList(List<Baseline> baselines) {
        this.addAll(baselines);
    }

    /**
     * Apply all the given filters and rules to this
     *
     * @return The {@link BaselineList}
     * @throws UnableToInitializeEntityException Thrown when ClearCase reports errors 
     * @throws UnableToListBaselinesException Thrown when ClearCase reports errors 
     */
    public BaselineList apply() throws UnableToInitializeEntityException, UnableToListBaselinesException {

        /* Printing info for debug */
        logger.fine(" --- Get baselines information --- ");
        logger.fine(String.format("Component: %s", component.getNormalizedName()));
        logger.fine(String.format("Stream   : %s", stream.getNormalizedName()));
        logger.fine(String.format("Level    : %s", level));
        logger.fine(String.format("Limit    : %s", limit));
        logger.fine(String.format("Filters  : %s", filters));
        logger.fine(String.format("Multisite: %s", multisitePolling));
        logger.fine(String.format("Requiring: %s", required));

        //Asking for posted deliveries only makes sense when you have multisite enabled.
        if(multisitePolling) {
            if(stream.hasPostedDelivery()) {                    
                this.addAll(stream.getPostedBaselines(component, level));            
            } else {
                this.addAll(_get());
            }
        } else {
            this.addAll(_get());
        }

        logger.fine("Pre filter steps");
        for (BaselineFilter filter : filters) {
            filter.preFilter(this);
        }

        if (required.size() > 0) {
            for (Baseline b : required) {
                if (!this.contains(b)) {
                    this.add(b);
                }
            }
        }

        /* Sort the baselines */
        if (sorter != null) {
            Collections.sort(this, sorter);
        }

        logger.fine(" --- Bare retrieval --- ");
        logger.fine("Baselines: " + this);

        /* Do the filtering */
        int pruned = 0;
        for (BaselineFilter filter : filters) {
            logger.fine("Filter: " + filter.getName());
            pruned += filter.filter(this);
            logger.fine("Baselines: " + this);
        }

        /* Load em? */
        if (load) {
            Iterator<Baseline> it = this.iterator();
            while (it.hasNext()) {
                Baseline baseline = it.next();
                try {
                    baseline.load();
                } catch (Exception e) {
                    logger.warning("[ClearCase] Unable to load " + baseline.getNormalizedName() + ": " + e.getMessage());
                    it.remove();
                    pruned++;
                    continue;
                    /* Just continue */
                }
            }
        }

        if (pruned > 0) {
            logger.config("[ClearCase] Pruned " + pruned + " baselines");
        }

        /* Limit? 0 = unlimited */
        if (limit > 0 && this.size() > 0) {
            BaselineList n = new BaselineList();
            n.addAll(this.subList(0, limit));
            logger.fine("Final list of baselines: " + n);
            return n;
        } else {
            logger.fine("Final list of baselines: " + this);
            return this;
        }
    }

    /**
     * Ensure that the {@link Baseline} is in the list
     * @param baseline Adds the {@link Baseline} to the required baselines  
     * @return The {@link BaselineList}
     */
    public BaselineList ensureBaseline(Baseline baseline) {
        required.add(baseline);

        return this;
    }

    /**
     * Apply a single filter to the {@link BaselineList} after the list has been
     * generated.
     *
     * @param filter Apply the {@link BaselineFilter}
     * @return A {@link BaselineList}
     */
    public BaselineList applyFilter(BaselineFilter filter) {
        logger.fine("Filter: " + filter.getName());
        filter.filter(this);

        return this;
    }

    /**
     * Set a limit of how many {@link Baseline}s apply should return
     *
     * @param limit Limit to this number of baselines
     * @return A limited {@link BaselineList}
     */
    public BaselineList setLimit(int limit) {
        this.limit = limit;

        return this;
    }

    /**
     * Load the {@link Baseline}s
     *
     * @return A loaded {@link BaselineList}
     */
    public BaselineList load() {
        this.load = true;

        return this;
    }

    /**
     * Set the sorting of the {@link BaselineList}
     *
     * @param sorter - A {@link Comparator} of {@link Baseline}s
     * @return A sorted {@link BaselineList}
     */
    public BaselineList setSorting(Comparator<Baseline> sorter) {
        this.sorter = sorter;

        return this;
    }

    /**
     * Add a filter to apply
     *
     * @param filter Adds this filter to the {@link BaselineList}
     * @return The {@link BaselineList} with the new filter
     */
    public BaselineList addFilter(BaselineFilter filter) {
        this.filters.add(filter);

        return this;
    }

    private List<Baseline> _get() throws UnableToInitializeEntityException, UnableToListBaselinesException {
        List<String> bls_str = null;
        String cmd;

        if (SystemUtils.IS_OS_WINDOWS) {
            cmd = "lsbl -fmt %Xn::%Nd::%[label_status]p\\n -component " + component + " -stream " + stream + (level != null ? " -level " + level.toString() : "");
        } else {
            cmd = "lsbl -fmt %Xn::%Nd::%[label_status]p\\\\n -component " + component + " -stream " + stream + (level != null ? " -level " + level.toString() : "");
        }
        try {
            bls_str = Cleartool.run(cmd).stdoutList;
            logger.finest("The output: " + bls_str);
        } catch (AbnormalProcessTerminationException e) {
            logger.warning(e.getMessage());
            throw new UnableToListBaselinesException(stream, component, level, e);
        }

        logger.fine("I got " + bls_str.size() + " baselines.");
        List<Baseline> bls = new ArrayList<Baseline>();

        for (String bl : bls_str) {
            String[] split = bl.split("::");
            Baseline baseline = Baseline.get(split[0]);
            try {
                baseline.setDate(split[1]);
                baseline.setLabelStatusFromString(split[2]);
            } catch (ParseException e) {
                throw new UnableToInitializeEntityException(baseline.getClass(), e);
            }
            bls.add(baseline);
        }

        return bls;
    }

    public static class AscendingDateSort implements Comparator<Baseline>, Serializable {

        @Override
        public int compare(Baseline bl1, Baseline bl2) {
            if (bl2.getDate() == null) {
                return 1;
            }
            if (bl1.getDate() == null) {
                return -1;
            }

            return bl1.getDate().compareTo(bl2.getDate());
        }
    }

    public static class DescendingDateSort implements Comparator<Baseline>, Serializable {

        @Override
        public int compare(Baseline bl1, Baseline bl2) {
            if (bl2.getDate() == null) {
                return -1;
            }
            if (bl1.getDate() == null) {
                return 1;
            }
            return bl2.getDate().compareTo(bl1.getDate());
        }
    }
}
