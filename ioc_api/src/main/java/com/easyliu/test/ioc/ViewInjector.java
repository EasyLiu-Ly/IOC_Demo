package com.easyliu.test.ioc;

import android.app.Activity;
import android.view.View;

public class ViewInjector {

  private static final String SUFFIX = "$$ViewInject";

  public static void injectView(Activity activity) {
    IViewInject iViewInject = findProxyActivity(activity);
    iViewInject.inject(activity, activity);
  }

  public static void injectView(Object object, View view) {
    IViewInject iViewInject = findProxyActivity(object);
    iViewInject.inject(object, view);
  }

  private static IViewInject findProxyActivity(Object activity) {
    try {
      Class clazz = activity.getClass();
      Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
      return (IViewInject) injectorClazz.newInstance();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
    throw new RuntimeException(String.format("can not find %s , something when compiler.",
        activity.getClass().getSimpleName() + SUFFIX));
  }
}
