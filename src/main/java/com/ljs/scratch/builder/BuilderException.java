package com.ljs.scratch.builder;

/**
 *
 * @author lstephen
 */
public class BuilderException extends RuntimeException {

    public BuilderException() {
        super();
    }

    public BuilderException(String msg) {
        super(msg);
    }

    public BuilderException(Throwable cause) {
        super(cause);
    }

    public BuilderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
