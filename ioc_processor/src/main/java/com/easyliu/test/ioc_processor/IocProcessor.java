package com.easyliu.test.ioc_processor;

import com.easyliu.test.annotation.BindView;
import com.google.auto.service.AutoService;

import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * @author easyliu
 */
@AutoService(Processor.class)
public class IocProcessor extends AbstractProcessor {

    private Map<String, BindViewProxyClassInfo> mStringProxyInfoMap = new HashMap<>();

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("start process");
        mStringProxyInfoMap.clear();
        /*
         * 收集信息
         */
        Set<? extends Element> withBind = roundEnv.getElementsAnnotatedWith(BindView.class);
        note("遍历所有的element");
        //遍历所有的element
        for (Element element : withBind) {
            //判断element是否是合法的
            if (checkAnnotationValid(element, BindView.class)) {
                //由于这个注解是作用到成员变量上面的，因此强转为VariableElement
                VariableElement variableElement = (VariableElement) element;
                //得到类Element
                TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
                //得到类的全名
                String fullClassName = typeElement.getQualifiedName().toString();
                //通过类名找到代理类的信息
                BindViewProxyClassInfo bindViewProxyClassInfo = mStringProxyInfoMap.get(fullClassName);
                //代理类为空就重新创建一个
                if (bindViewProxyClassInfo == null) {
                    bindViewProxyClassInfo = new BindViewProxyClassInfo(processingEnv.getElementUtils(), typeElement);
                    //把代理类放入数据
                    mStringProxyInfoMap.put(fullClassName, bindViewProxyClassInfo);
                }
                //得到BindView这个注解
                BindView bindView = variableElement.getAnnotation(BindView.class);
                //把注解中的id值跟成员变量映射起来
                bindViewProxyClassInfo.mIntegerVariableElementMap.put(bindView.value(), variableElement);
            }
        }
        /*
         * 写文件,遍历数据，把每个代理类写成一个单独的类文件
         */
        for (String key : mStringProxyInfoMap.keySet()) {
            note("写文件,遍历数据，把每个代理类写成一个单独的类文件");
            BindViewProxyClassInfo bindViewProxyClassInfo = mStringProxyInfoMap.get(key);
            try {
                JavaFileObject javaFileObject = processingEnv.getFiler()
                        .createSourceFile(bindViewProxyClassInfo.getProxyClassFullName(), bindViewProxyClassInfo.getTypeElement());
                Writer writer = javaFileObject.openWriter();
                writer.write(bindViewProxyClassInfo.generateJavaCode());
                writer.flush();
                writer.close();
            } catch (Exception e) {
                error(bindViewProxyClassInfo.getTypeElement(), "Unable to write injector for type %s: %s",
                        bindViewProxyClassInfo.getTypeElement(), e.getMessage());
            }
        }
        note("end process");
        return true;
    }

    private boolean checkAnnotationValid(Element annotatedElement, Class clazz) {
        if (annotatedElement.getKind() != ElementKind.FIELD) {
            error(annotatedElement, "%s must be declared on field.", clazz.getSimpleName());
            return false;
        }
        if (isPrivate(annotatedElement)) {
            error(annotatedElement, "%s() must can not be private.", annotatedElement.getSimpleName());
            return false;
        }
        return true;
    }

    private boolean isPrivate(Element annotatedClass) {
        return annotatedClass.getModifiers().contains(PRIVATE);
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void note(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    private void note(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }
}
