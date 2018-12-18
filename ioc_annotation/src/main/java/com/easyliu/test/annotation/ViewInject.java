package com.easyliu.test.annotation;

/**
 * @author easyliu
 */
public interface ViewInject<T> {
  void inject(T t, Object source);
}
