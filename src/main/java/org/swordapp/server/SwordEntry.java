
package org.swordapp.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

public class SwordEntry {

    public Entry entry;

    public SwordEntry(Entry entry) {
        this.entry = entry;
    }

    public String getTitle() {
        return entry.getTitle();
    }

    public String getSummary() {
        return entry.getSummary();
    }

    public Entry getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            entry.writeTo(baos);
            return baos.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // put the code in for getting a dublin core record out
    public Map<String, List<String>> getDublinCore() {
        Map<String, List<String>> dc = new HashMap<String, List<String>>();
        List<Element> extensions = entry.getExtensions();
        for (Element element : extensions) {
            if (UriRegistry.DC_NAMESPACE.equals(element.getQName()
                    .getNamespaceURI())) {
                // we have a dublin core extension
                String field = element.getQName().getLocalPart();
                String value = element.getText();

                if (dc.containsKey(field)) {
                    dc.get(field).add(value);
                } else {
                    ArrayList<String> values = new ArrayList<String>();
                    values.add(value);
                    dc.put(field, values);
                }
            }
        }
        return dc;
    }
}
