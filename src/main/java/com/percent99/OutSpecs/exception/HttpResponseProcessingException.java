package com.percent99.OutSpecs.exception;

/**
 * HTTP 응답을 처리하는 과정에서 발생하는 에러에 대한 exception.
 */
public class HttpResponseProcessingException extends RuntimeException {
  public HttpResponseProcessingException(Throwable e){
    super(e);
  }
}
