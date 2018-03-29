package com.github.lstephen.ootp.extract.html.loader;

public class PageLoaderException extends RuntimeException {
  public PageLoaderException() {
    super();
  }

  public PageLoaderException(String msg) {
    super(msg);
  }

  public PageLoaderException(Throwable cause) {
    super(cause);
  }

  public PageLoaderException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
