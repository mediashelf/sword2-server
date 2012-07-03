
package org.swordapp.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import javax.activation.DataSource;

public class StringDataSource implements DataSource {

    private final String contentType, name;

    private final InputStream stream;

    public StringDataSource(InputStream is, String contentType, String name)
            throws UnsupportedEncodingException {
        this(convertStreamToString(is), contentType, name);
    }

    public StringDataSource(String source, String contentType, String name)
            throws UnsupportedEncodingException {
        this.contentType = contentType;
        this.name = name;

        stream = new ByteArrayInputStream(source.getBytes("UTF-8"));
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    private static String convertStreamToString(InputStream is) {
        try {
            return new Scanner(is, "utf-8").useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

}
