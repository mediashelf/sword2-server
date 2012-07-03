
package org.swordapp.server;

public class SwordServerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SwordServerException() {
        super();
    }

    public SwordServerException(String message) {
        super(message);
    }

    public SwordServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwordServerException(Throwable cause) {
        super(cause);
    }
}
