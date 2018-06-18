package net.praqma.clearcase.container;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Label;
import net.praqma.clearcase.ucm.entities.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cwolfgang
 */
@SuppressFBWarnings("")
public class LabelsForVersion {

    private Version version;
    private List<Label> labels = new ArrayList<Label>();

    public LabelsForVersion( Version version ) {
        this.version = version;
    }

    /**
     * Get the final {@link Branch} of the branch path.
     * @return The {@link Branch}
     */
    public Branch getBranch() {
        return version.getUltimateBranch();
    }

    public int getRevision() {
        return version.getRevision();
    }

    public LabelsForVersion addLabels( Collection<Label> labels ) {
        this.labels.addAll( labels );

        return this;
    }

    public List<Label> getLabels() {
        return labels;
    }
}
