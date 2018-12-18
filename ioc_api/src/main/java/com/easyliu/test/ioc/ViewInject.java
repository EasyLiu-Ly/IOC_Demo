package com.easyliu.test.ioc;

public interface ViewInject<T> {
  void inject(T t, Object source);
}
