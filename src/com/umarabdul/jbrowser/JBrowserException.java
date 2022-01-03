package com.umarabdul.jbrowser;

/**
* Exception class for JBrowser.
*/

public class JBrowserException extends RuntimeException{

  private String cause;
  
  public JBrowserException(String cause){
    this.cause = cause;
  }

  @Override
  public String getMessage(){
    return cause;
  }

  @Override
  public String toString(){
    return cause;
  }
}
