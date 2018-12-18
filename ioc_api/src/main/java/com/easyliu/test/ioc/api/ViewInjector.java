package com.easyliu.test.ioc.api;

import android.app.Activity;
import android.view.View;

import com.easyliu.test.annotation.ViewInject;

/**
 * @author easyliu
 */
public class ViewInjector {

  private static final String SUFFIX = "$ViewInject";

  public static void injectView(Activity activity) {
    ViewInject viewInject = findProxyActivity(activity);
    viewInject.inject(activity, activity);
  }

  public static void injectView(Object object, View view) {
    ViewInject viewInject = findProxyActivity(object);
    viewInject.inject(object, view);
  }

  private static ViewInject findProxyActivity(Object activity) {
    try {
      Class clazz = activity.getClass();
      Class injectorClazz = Class.forName(clazz.getName() + SUFFIX);
      return (ViewInject) injectorClazz.newInstance();
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
