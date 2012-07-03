
package org.swordapp.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;

/**
 * <p>Utility class that implements {@link DataSource} 
 * to build {@link javax.mail.internet.MimeMultipart MimeMultipart}s from 
 * multipart HTTP requests.
 * 
 * <p>The contract of {@link DataSource#getInputStream()} 
 * requires a new {@link InputStream} object must be returned each time this 
 * method is called, so this class writes the {@link HttpServletRequest}'s 
 * input stream to a temporary file so that {@link #getInputStream()} can 
 * return a new FileInputStream on each call.
 * 
 * @author Edwin Shin
 *
 */
public class RequestDataSource implements DataSource {

    private final String contentType, name;

    private final File temp;

    private FileCleaningTracker tracker;

    public RequestDataSource(HttpServletRequest request)
            throws IOException {
        this.contentType = request.getContentType();
        this.name = request.getServletPath();

        temp = File.createTempFile("sword", null);
        FileUtils.copyInputStreamToFile(request.getInputStream(), temp);

        // Register this temp file with the reaper thread
        tracker =
                FileCleanerCleanup.getFileCleaningTracker(request.getSession()
                        .getServletContext());
        tracker.track(temp, this);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(temp);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
