
package org.swordapp.server;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Text;
import org.apache.abdera.model.Workspace;

public class SwordWorkspace {

    private final Workspace workspace;

    public SwordWorkspace() {
        Abdera abdera = new Abdera();
        workspace = abdera.getFactory().newWorkspace();
    }

    public Workspace getWrappedWorkspace() {
        return workspace;
    }

    public Workspace getAbderaWorkspace() {
        // at the moment, this doesn't need to clone anything
        return workspace;
    }

    public void addCollection(SwordCollection collection) {
        // FIXME: or should collections be managed internally until getAbderaWorkspace is called
        Collection abderaCollection = collection.getAbderaCollection();
        workspace.addCollection(abderaCollection);
    }

    public Text setTitle(String title) {
        return workspace.setTitle(title);
    }
}
