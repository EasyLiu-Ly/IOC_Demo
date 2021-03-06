package com.easyliu.test.ioc_processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author easyliu
 */
public class BindViewProxyClassInfo {
    private String mPackageName;
    private String mProxyClassName;
    private TypeElement mTypeElement;

    Map<Integer, VariableElement> mIntegerVariableElementMap = new HashMap<>();

    public static final String PROXY = "ViewInject";

    public BindViewProxyClassInfo(Elements elementsUtils, TypeElement typeElement) {
        mTypeElement = typeElement;
        PackageElement packageElement = elementsUtils.getPackageOf(typeElement);
        String packageName = packageElement.getQualifiedName().toString();
        String className = getClassName(typeElement, packageName);
        mPackageName = packageName;
        mProxyClassName = className + "$" + PROXY;
    }

    public String getProxyClassFullName() {
        return mPackageName + "." + mProxyClassName;
    }

    public TypeElement getTypeElement() {
        return mTypeElement;
    }

    public String generateJavaCode() {
        StringBuilder builder = new StringBuilder();
        builder.append("// Auto generated code. Do not modify!\n");
        builder.append("package ").append(mPackageName).append(";\n\n");
        builder.append("import com.easyliu.test.annotation.*;\n");
        builder.append('\n');

        builder.append("public class ")
                .append(mProxyClassName)
                .append(" implements " + BindViewProxyClassInfo.PROXY + "<" + mTypeElement.getQualifiedName() + ">");
        builder.append(" {\n");
        generateMethods(builder);
        builder.append("}\n");
        return builder.toString();
    }

    private void generateMethods(StringBuilder builder) {
        builder.append("@Override\n ");
        builder.append(
                "public void inject(" + mTypeElement.getQualifiedName() + " host, Object source ) {\n");
        builder.append(" if(source instanceof android.app.Activity){\n");
        for (int id : mIntegerVariableElementMap.keySet()) {
            VariableElement element = mIntegerVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            builder.append("(" + type + ")(((android.app.Activity)source).findViewById( " + id + "));\n");
        }
        builder.append("}else if(source instanceof android.view.View){\n");
        int count = 0;
        for (int id : mIntegerVariableElementMap.keySet()) {
            count++;
            VariableElement element = mIntegerVariableElementMap.get(id);
            String name = element.getSimpleName().toString();
            String type = element.asType().toString();
            builder.append("host." + name).append(" = ");
            if (count == mIntegerVariableElementMap.size()) {
                builder.append("(" + type + ")(((android.view.View)source).findViewById( " + id + "));");
            } else {
                builder.append("(" + type + ")(((android.view.View)source).findViewById( " + id + "));\n");
            }
        }
        builder.append("\n}");
        builder.append("  }\n");
    }

    private String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }
}
