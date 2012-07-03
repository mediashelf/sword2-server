
package org.swordapp.server;

public class SwordConfigurationDefault implements SwordConfiguration {

    @Override
    public boolean returnDepositReceipt() {
        return true;
    }

    @Override
    public boolean returnStackTraceInError() {
        return true;
    }

    @Override
    public boolean returnErrorBody() {
        return true;
    }

    @Override
    public String generator() {
        return "http://www.swordapp.org/";
    }

    @Override
    public String generatorVersion() {
        return "2.0";
    }

    @Override
    public String administratorEmail() {
        return null;
    }

    @Override
    public String getAuthType() {
        return "Basic";
    }

    @Override
    public boolean storeAndCheckBinary() {
        return false;
    }

    @Override
    public String getTempDirectory() {
        return null;
    }

    @Override
    public int getMaxUploadSize() {
        return -1;
    }

    @Override
    public String getAlternateUrl() {
        return null;
    }

    @Override
    public String getAlternateUrlContentType() {
        return null;
    }
}
