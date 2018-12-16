package com.easyliu.test.ioc;

public interface IViewInject<T> {
  void inject(T t, Object source);
}
