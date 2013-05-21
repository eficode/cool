package net.praqma.clearcase.container;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Label;
import net.praqma.clearcase.ucm.entities.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cwolfgang
 */
public class LabelsForVersion {

    private Version version;
    private List<Label> labels = new ArrayList<Label>();

    public LabelsForVersion( Version version ) {
        this.version = version;
    }

    /**
     * Get the final {@link Branch} of the branch path.
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
