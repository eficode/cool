package net.praqma.clearcase.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.api.RemoveView;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;

public class SetupUtils {

    private static Logger logger = Logger.getLogger(SetupUtils.class.getName());

    public static void tearDown(PVob pvob) throws CleartoolException, UnableToInitializeEntityException, ViewException {
        logger.info("Tearing down " + pvob);

        /* The pvob needs to be loaded */
        pvob.load();

        List<UCMView> views = new LinkedList<UCMView>();

        for (Stream stream : pvob.getStreams()) {
            for (UCMView view : stream.getViews()) {
                logger.info("Removing " + view + " from " + stream.getNormalizedName());

                try {
                    view.load();
                    view.end();
                    view.remove();
                    views.add(view);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Unable to remove " + view, e);
                }

                try {
                    if (view.exists()) {
                        logger.info("The view was not removed, trying ....");
                        new RemoveView().all().setTag(view.getViewtag()).execute();
                        views.add(view);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Unable to remove(second attempt) " + view, e);
                }
            }
        }

        logger.fine("Removed views: " + views);

        Set<Vob> vobs = pvob.getVobs();

        logger.info("Removing vobs");
        for (Vob vob : vobs) {
            logger.info("Removing " + vob);
            try {
                vob.unmount();
                vob.remove();
            } catch (CleartoolException e) {
                ExceptionUtils.log(e, true);
            }
        }

        logger.info("Removing pvob");
        pvob.unmount();
        logger.info("Unmounted pvob");
        try {
            Thread.sleep(5000); // Give time for process to end
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Interrup catched in thread sleep, logging exception", e);
        }
        pvob.remove();
        logger.info("Removal of pvob completed");
    }
}
