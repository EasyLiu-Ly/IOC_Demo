package com.easyliu.test.ioc_processor;

import com.easyliu.test.annotation.BindView;
import com.google.auto.service.AutoService;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class) public class IocProcessor extends AbstractProcessor {

  private Map<String, ProxyInfo> mStringProxyInfoMap = new HashMap<>();

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new HashSet<>();
    types.add(BindView.class.getCanonicalName());
    return types;
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    Messager messager = processingEnv.getMessager();
    messager.printMessage(Diagnostic.Kind.NOTE, "start process");
    mStringProxyInfoMap.clear();

    /*
     * 收集信息
     */
    Set<? extends Element> withBind = roundEnv.getElementsAnnotatedWith(BindView.class);
    for (Element element : withBind) {
      checkAnnotationValid(element, BindView.class);
      VariableElement variableElement = (VariableElement) element;
      TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
      String fullClassName = typeElement.getQualifiedName().toString();
      ProxyInfo proxyInfo = mStringProxyInfoMap.get(fullClassName);
      if (proxyInfo == null) {
        proxyInfo = new ProxyInfo(processingEnv.getElementUtils(), typeElement);
        mStringProxyInfoMap.put(fullClassName, proxyInfo);
      }
      BindView bindView = variableElement.getAnnotation(BindView.class);
      proxyInfo.mIntegerVariableElementMap.put(bindView.value(), variableElement);
    }

    /*
     * 写文件
     */
    for (String key : mStringProxyInfoMap.keySet()) {
      ProxyInfo proxyInfo = mStringProxyInfoMap.get(key);
      try {
        JavaFileObject javaFileObject = processingEnv.getFiler()
            .createClassFile(proxyInfo.getProxyClassFullName(), proxyInfo.getTypeElement());
        Writer writer = javaFileObject.openWriter();
        writer.write(proxyInfo.generateJavaCode());
        writer.flush();
        writer.close();
      } catch (Exception e) {
        error(proxyInfo.getTypeElement(), "Unable to write injector for type %s: %s",
            proxyInfo.getTypeElement(), e.getMessage());
      }
    }
    return true;
  }

  private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
    if (annotatedElement.getKind() != ElementKind.FIELD) {
      error(annotatedElement, "%s must be declared on field.", clazz.getSimpleName());
      return false;
    }
    if (ClassValidator.isPrivate(annotatedElement)) {
      error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
      return false;
    }

    return true;
  }

  private void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
  }
}
