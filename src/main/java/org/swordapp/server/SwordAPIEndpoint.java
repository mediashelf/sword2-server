
package org.swordapp.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.Parser;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class SwordAPIEndpoint {

    protected SwordConfiguration config;

    private static Logger log = Logger.getLogger(SwordAPIEndpoint.class);

    private static Pattern dispositionName = Pattern
            .compile(".*;\\s+name=(?:[\\\"\\'])?(\\w+)(?:[\\\"\\'])?.*");

    private static Pattern dispositionFilename = Pattern
            .compile(".*;\\s+filename=(?:[\\\"\\'])?(\\w+)(?:[\\\"\\'])?.*");

    protected SwordAPIEndpoint(SwordConfiguration config) {
        this.config = config;
    }

    public void get(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void post(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void put(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    public void delete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
    }

    protected AuthCredentials getAuthCredentials(HttpServletRequest request)
            throws SwordAuthException {
        return this.getAuthCredentials(request, false);
    }

    protected AuthCredentials getAuthCredentials(HttpServletRequest request,
            boolean allowUnauthenticated) throws SwordAuthException {
        // is the user authenticating?
        String authHeader = request.getHeader("Authorization");

        // is there an On-Behalf-Of header?
        String obo = request.getHeader("On-Behalf-Of");

        // which authentication scheme do we recognise (should only be Basic)
        String authType = config.getAuthType();
        boolean isBasic = "Basic".equals(authType);

        if (isBasic && (authHeader == null || "".equals(authHeader))) {
            if (!allowUnauthenticated) {
                throw new SwordAuthException(true);
            } else {
                log.debug("No Authentication Credentials supplied");
                return new AuthCredentials(null, null, obo);
            }
        }

        // decode the auth header and populate the authcredentials object for return
        String[] userPass = decodeAuthHeader(authHeader);
        AuthCredentials auth =
                new AuthCredentials(userPass[0], userPass[1], obo);
        return auth;
    }

    protected String[] decodeAuthHeader(String encodedHeader)
            throws SwordAuthException {
        // we have an authentication header, so parse it
        String[] authBits = encodedHeader.split(" ");

        // Auth header doesn't have 2 parts (Basic, [base 64 username/password])?
        if (authBits.length != 2) {
            log.fatal("Malformed Authorization header");
            throw new SwordAuthException("Malformed Authorization header");
        }

        // is this basic auth?  if not, we don't support it
        if (!"Basic".equalsIgnoreCase(authBits[0].trim())) {
            log.warn("Authentication method not supported: " + authBits[0]);
            throw new SwordAuthException(
                    "Authentication method not supported: " + authBits[0]);
        }

        // get the username and password out of the base64 encoded Basic auth string
        String unencodedCreds =
                new String(Base64.decodeBase64(authBits[1].trim().getBytes()));
        String[] userPass = unencodedCreds.split(":", 2);

        // did we get a username and password?
        if (userPass.length != 2) {
            log.fatal("Malformed Authorization header; unable to determine username/password boundary");
            throw new SwordAuthException(
                    "Malformed Authorization header; unable to determine username/password boundary");
        }

        return userPass;
    }

    protected String getFullUrl(HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        String q = req.getQueryString();
        if (q != null && !"".equals(q)) {
            url += "?" + q;
        }
        return url;
    }

    protected void storeAndCheckBinary(Deposit deposit,
            SwordConfiguration config) throws SwordServerException, SwordError {
        // we require an input stream for this to work
        if (deposit.getInputStream() == null) {
            throw new SwordServerException(
                    "Attempting to store and check deposit which has no input stream");
        }

        if (!config.storeAndCheckBinary()) {
            return;
        }

        String tempDirectory = config.getTempDirectory();
        if (tempDirectory == null) {
            throw new SwordServerException(
                    "Store and Check operation requested, but no tempDirectory specified in config");
        }

        String filename =
                tempDirectory + File.separator + "SWORD-" +
                        UUID.randomUUID().toString();

        try {
            InputStream inputstream = deposit.getInputStream();
            OutputStream outputstream =
                    new FileOutputStream(new File(filename));
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputstream.read(buf)) > 0) {
                    outputstream.write(buf, 0, len);
                }
            } finally {
                inputstream.close();
                outputstream.close();
            }
        } catch (IOException e) {
            throw new SwordServerException(e);
        }

        // Check the size is OK
        File file = new File(filename);
        // Set the file to be deposited
        deposit.setFile(file);

        long fLength = file.length(); // in bytes
        if (config.getMaxUploadSize() != -1 &&
                fLength > config.getMaxUploadSize()) {
            String msg =
                    "The uploaded file exceeded the maximum file size this server will accept (the file is " +
                            fLength +
                            " bytes but the server will only accept files as large as " +
                            config.getMaxUploadSize() + " bytes)";
            throw new SwordError(UriRegistry.ERROR_MAX_UPLOAD_SIZE_EXCEEDED,
                    msg);
        }

        try {
            // get the things we might want to compare
            String receivedMD5 = ChecksumUtils.generateMD5(filename);
            log.debug("Received filechecksum: " + receivedMD5);
            String md5 = deposit.getMd5();
            log.debug("Received file checksum header: " + md5);

            if (md5 != null && !md5.equals(receivedMD5)) {
                log.debug("Bad MD5 for file. Aborting with appropriate error message");
                String msg =
                        "The received MD5 checksum for the deposited file did not match the checksum sent by the deposit client";
                throw new SwordError(UriRegistry.ERROR_CHECKSUM_MISMATCH, msg);
            }
            log.debug("Package temporarily stored as: " + filename);
        } catch (NoSuchAlgorithmException e) {
            throw new SwordServerException(e);
        } catch (IOException e) {
            throw new SwordServerException(e);
        }
    }

    protected void addDepositPropertiesFromMultipart(Deposit deposit,
            HttpServletRequest req) throws ServletException, IOException,
            SwordError {

        try {
            MimeMultipart mp =
                    new MimeMultipart(new RequestDataSource(req));

            BodyPart bp;
            for (int i = 0; i < mp.getCount(); i++) {
                bp = mp.getBodyPart(i);
                String disposition = getHeader(bp, "Content-Disposition");
                String name = getName(disposition);

                if ("atom".equals(name)) {
                    InputStream entryPart = bp.getInputStream();
                    Abdera abdera = new Abdera();
                    Parser parser = abdera.getParser();
                    Document<Entry> entryDoc = parser.parse(entryPart);
                    Entry entry = entryDoc.getRoot();
                    deposit.setEntry(entry);
                } else if ("payload".equals(name)) {
                    String md5 = getHeader(bp, "Content-MD5");
                    String packaging = getHeader(bp, "Packaging");
                    String filename = getFilename(disposition);
                    if (filename == null || "".equals(filename)) {
                        throw new SwordError(UriRegistry.ERROR_BAD_REQUEST,
                                "Filename could not be extracted from Content-Disposition");
                    }
                    String ct = bp.getContentType();
                    String mimeType = "application/octet-stream";
                    if (ct != null) {
                        String[] bits = ct.split(";");
                        mimeType = bits[0].trim();
                    }
                    InputStream mediaPart = bp.getInputStream();
                    deposit.setFilename(filename);
                    deposit.setInputStream(mediaPart);
                    deposit.setMimeType(mimeType);
                    deposit.setMd5(md5);
                    deposit.setPackaging(packaging);
                }
            }
        } catch (MessagingException e) {
            throw new SwordError(e.getMessage(), e);
        }

        try {
            storeAndCheckBinary(deposit, config);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        }
    }

    protected void cleanup(Deposit deposit) {
        if (deposit == null) {
            return;
        }

        File tmp = deposit.getFile();
        if (tmp == null) {
            return;
        }

        tmp.delete();
        deposit.setFile(null);
    }

    protected Element getGenerator(SwordConfiguration config) {
        String generatorUri = config.generator();
        String generatorVersion = config.generatorVersion();
        String adminEmail = config.administratorEmail();
        if (generatorUri != null && !"".equals(generatorUri)) {
            Abdera abdera = new Abdera();
            Element generator = abdera.getFactory().newGenerator();
            generator.setAttributeValue("uri", generatorUri);
            if (generatorVersion != null) {
                generator.setAttributeValue("version", generatorVersion);
            }
            if (adminEmail != null && !"".equals(adminEmail)) {
                generator.setText(adminEmail);
            }
            return generator;
        }
        return null;
    }

    protected void addDepositPropertiesFromEntry(Deposit deposit,
            HttpServletRequest req) throws IOException {
        InputStream entryPart = req.getInputStream();
        Abdera abdera = new Abdera();
        Parser parser = abdera.getParser();
        Document<Entry> entryDoc = parser.parse(entryPart);
        Entry entry = entryDoc.getRoot();
        deposit.setEntry(entry);
    }

    protected void addDepositPropertiesFromBinary(Deposit deposit,
            HttpServletRequest req) throws ServletException, IOException,
            SwordError {
        String contentType = getContentType(req);
        String contentDisposition = req.getHeader("Content-Disposition");
        String md5 = req.getHeader("Content-MD5");
        String packaging = req.getHeader("Packaging");
        if (packaging == null || "".equals(packaging)) {
            packaging = UriRegistry.PACKAGE_BINARY;
        }
        InputStream file = req.getInputStream();

        // now let's interpret and deal with the headers that we have
        String filename = getFilename(contentDisposition);
        if (filename == null || "".equals(filename)) {
            throw new SwordError(UriRegistry.ERROR_BAD_REQUEST,
                    "Filename could not be extracted from Content-Disposition");
        }

        deposit.setFilename(filename);
        deposit.setMd5(md5);
        deposit.setPackaging(packaging);
        deposit.setInputStream(file);
        deposit.setMimeType(contentType);

        try {
            storeAndCheckBinary(deposit, config);
        } catch (SwordServerException e) {
            throw new ServletException(e);
        }
    }

    protected void swordError(HttpServletRequest req, HttpServletResponse resp,
            SwordError e) throws IOException, ServletException {
        try {
            if (!config.returnErrorBody() || !e.hasBody()) {
                ErrorDocument doc =
                        new ErrorDocument(e.getErrorUri(), e.getStatus());
                resp.setStatus(doc.getStatus());
                return;
            }

            // treatment is either the default value in the ErrorDocument OR the error message if it exists
            String treatment = e.getMessage();

            // verbose description is the stack trace if allowed, otherwise null
            String verbose = null;
            if (config.returnStackTraceInError()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                verbose = sw.getBuffer().toString();
            }

            ErrorDocument doc;
            if (treatment == null) {
                doc =
                        new ErrorDocument(e.getErrorUri(), e.getStatus(),
                                verbose);
            } else {
                doc =
                        new ErrorDocument(e.getErrorUri(), e.getStatus(),
                                treatment, verbose);
            }

            // now write the response
            resp.setStatus(doc.getStatus());
            resp.setHeader("Content-Type", "text/xml");

            doc.writeTo(resp.getWriter(), config);
            resp.getWriter().flush();
        } catch (SwordServerException sse) {
            throw new ServletException(sse);
        }
    }

    protected String getFilename(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }
        Matcher m = dispositionFilename.matcher(contentDisposition);
        if (m.matches()) {
            return m.group(1);
        }

        return null;
    }

    protected String getName(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }
        Matcher m = dispositionName.matcher(contentDisposition);
        if (m.matches()) {
            return m.group(1);
        }

        return null;
    }

    protected Map<String, String> getAcceptHeaders(HttpServletRequest req) {
        Map<String, String> acceptHeaders = new HashMap<String, String>();
        Enumeration<?> headers = req.getHeaderNames();
        while (headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            if (header.toLowerCase().startsWith("accept")) {
                acceptHeaders.put(header, req.getHeader(header));
            }
        }
        return acceptHeaders;
    }

    protected void copyInputToOutput(InputStream in, OutputStream out)
            throws IOException {
        final int BUFFER_SIZE = 1024 * 4;
        final byte[] buffer = new byte[BUFFER_SIZE];

        while (true) {
            final int count = in.read(buffer, 0, BUFFER_SIZE);

            if (-1 == count) {
                break;
            }

            // write out those same bytes
            out.write(buffer, 0, count);
        }
    }

    protected String getContentType(HttpServletRequest req) {
        String contentType = req.getHeader("Content-Type");
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }

    protected boolean getInProgress(HttpServletRequest req) throws SwordError {
        String iph = req.getHeader("In-Progress");
        boolean inProgress = false; // default value
        if (iph != null) {
            // first of all validate that the value is "true" or "false"
            if (!"true".equals(iph.trim()) && !"false".equals(iph.trim())) {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST,
                        "The In-Progress header MUST be 'true' or 'false'");
            }
            inProgress = "true".equals(iph.trim());
        }
        return inProgress;
    }

    protected boolean getMetadataRelevant(HttpServletRequest req)
            throws SwordError {
        String mdr = req.getHeader("Metadata-Relevant");
        boolean metadataRelevant = false; // default value
        if (mdr != null) {
            // first of all validate that the value is "true" or "false"
            if (!"true".equals(mdr.trim()) && !"false".equals(mdr.trim())) {
                throw new SwordError(UriRegistry.ERROR_BAD_REQUEST,
                        "The In-Progress header MUST be 'true' or 'false'");
            }
            metadataRelevant = "true".equals(mdr.trim());
        }
        return metadataRelevant;
    }

    private String getHeader(BodyPart bp, String name) throws SwordError {
        String[] headers;
        try {
            headers = bp.getHeader(name);
        } catch (MessagingException e) {
            throw new SwordError(e.getMessage(), e);
        }
        if (headers == null) {
            return null;
        } else {
            return headers[0];
        }
    }
}
