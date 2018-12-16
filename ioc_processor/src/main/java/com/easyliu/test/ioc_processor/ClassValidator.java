package com.easyliu.test.ioc_processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.PRIVATE;

public class ClassValidator {

  static boolean isPrivate(Element annotatedClass) {
    return annotatedClass.getModifiers().contains(PRIVATE);
  }

  static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }
}
