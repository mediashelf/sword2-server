
package org.swordapp.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

public class DepositReceipt {

    private List<String> packagingFormats = new ArrayList<String>();

    private IRI editIRI = null;

    private IRI seIRI = null;

    private IRI emIRI = null;

    private IRI feedIRI = null;

    private IRI location = null;

    private final Entry entry;

    private final Map<String, String> statements =
            new HashMap<String, String>();

    private String treatment = "no treatment information available";

    private String verboseDescription = null;

    private String splashUri = null;

    private String originalDepositUri = null;

    private String originalDepositType = null;

    private Map<String, String> derivedResources =
            new HashMap<String, String>();

    private boolean empty = false;

    private Date lastModified = null;

    public DepositReceipt() {
        Abdera abdera = new Abdera();
        entry = abdera.newEntry();
    }

    public Entry getWrappedEntry() {
        return entry;
    }

    public Entry getAbderaEntry() {
        Entry abderaEntry = (Entry) entry.clone();

        // use the edit iri as the id
        abderaEntry.setId(editIRI.toString());

        // add the Edit IRI Link
        if (editIRI != null) {
            abderaEntry.addLink(editIRI.toString(), "edit");
        }

        // add the Sword Edit IRI link
        if (seIRI != null) {
            abderaEntry.addLink(seIRI.toString(), UriRegistry.REL_SWORD_EDIT);
        }

        // add the atom formatted feed
        if (feedIRI != null) {
            Link fl = abderaEntry.addLink(feedIRI.toString(), "edit-media");
            fl.setMimeType("application/atom+xml;type=feed");
        }

        // add the edit-media link
        if (emIRI != null) {
            abderaEntry.addLink(emIRI.toString(), "edit-media");
        }

        // add the packaging formats
        for (String pf : packagingFormats) {
            abderaEntry.addSimpleExtension(UriRegistry.SWORD_PACKAGING, pf);
        }

        // add the statement URIs
        for (String statement : statements.keySet()) {
            Link link =
                    abderaEntry.addLink(statement, UriRegistry.REL_STATEMENT);
            link.setMimeType(statements.get(statement));
        }

        if (treatment != null) {
            abderaEntry.addSimpleExtension(UriRegistry.SWORD_TREATMENT,
                    treatment);
        }

        if (verboseDescription != null) {
            abderaEntry.addSimpleExtension(
                    UriRegistry.SWORD_VERBOSE_DESCRIPTION, verboseDescription);
        }

        if (splashUri != null) {
            abderaEntry.addLink(splashUri, "alternate");
        }

        if (originalDepositUri != null) {
            Link link =
                    abderaEntry.addLink(originalDepositUri,
                            UriRegistry.REL_ORIGINAL_DEPOSIT);
            if (originalDepositType != null) {
                link.setMimeType(originalDepositType);
            }
        }

        for (String uri : derivedResources.keySet()) {
            Link link =
                    abderaEntry.addLink(uri, UriRegistry.REL_DERIVED_RESOURCE);
            if (derivedResources.get(uri) != null) {
                link.setMimeType(derivedResources.get(uri));
            }
        }

        return abderaEntry;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setMediaFeedIRI(IRI feedIRI) {
        this.feedIRI = feedIRI;
    }

    public void setEditMediaIRI(IRI emIRI) {
        this.emIRI = emIRI;
    }

    public void setEditIRI(IRI editIRI) {
        this.editIRI = editIRI;

        // set the SE-IRI as the same if it has not already been set
        if (seIRI == null) {
            seIRI = editIRI;
        }
    }

    public IRI getLocation() {
        return location == null ? editIRI : location;
    }

    public void setLocation(IRI location) {
        this.location = location;
    }

    public IRI getEditIRI() {
        return editIRI;
    }

    public IRI getSwordEditIRI() {
        return seIRI;
    }

    public void setSwordEditIRI(IRI seIRI) {
        this.seIRI = seIRI;

        // set the Edit-IRI the same if it has not already been set
        if (editIRI == null) {
            editIRI = seIRI;
        }
    }

    public void setContent(IRI href, String mediaType) {
        entry.setContent(href, mediaType);
    }

    public void addEditMediaIRI(IRI href) {
        entry.addLink(href.toString(), "edit-media");
    }

    public void addEditMediaIRI(IRI href, String mediaType) {
        Abdera abdera = new Abdera();
        Link link = abdera.getFactory().newLink();
        link.setHref(href.toString());
        link.setRel("edit-media");
        link.setMimeType(mediaType);
        entry.addLink(link);
    }

    public void addEditMediaFeedIRI(IRI href) {
        this.addEditMediaIRI(href, "application/atom+xml;type=feed");
    }

    public void setPackaging(List<String> packagingFormats) {
        this.packagingFormats = packagingFormats;
    }

    public void addPackaging(String packagingFormat) {
        packagingFormats.add(packagingFormat);
    }

    public void setOREStatementURI(String statement) {
        setStatementURI("application/rdf+xml", statement);
    }

    public void setAtomStatementURI(String statement) {
        setStatementURI("application/atom+xml;type=feed", statement);
    }

    public void setStatementURI(String type, String statement) {
        statements.put(statement, type);
    }

    public Element addSimpleExtension(QName qname, String value) {
        return entry.addSimpleExtension(qname, value);
    }

    public Element addDublinCore(String element, String value) {
        return entry.addSimpleExtension(new QName(UriRegistry.DC_NAMESPACE,
                element), value);
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public void setVerboseDescription(String verboseDescription) {
        this.verboseDescription = verboseDescription;
    }

    public void setSplashUri(String splashUri) {
        this.splashUri = splashUri;
    }

    public void setOriginalDeposit(String originalDepositUri,
            String originalDepositType) {
        this.originalDepositUri = originalDepositUri;
        this.originalDepositType = originalDepositType;
    }

    public void setDerivedResources(Map<String, String> derivedResources) {
        this.derivedResources = derivedResources;
    }

    public void addDerivedResource(String resourceUri, String resourceType) {
        derivedResources.put(resourceUri, resourceType);
    }
}
