package com.builder.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

public class PageUtil {

    @FunctionalInterface
    public interface GetClassValue {
        void execute() throws MirroredTypesException;
    }

    public static List<? extends TypeMirror> getTypeMirrorFromAnnotationValue(GetClassValue clazz) {
        try {
            clazz.execute();
        } catch (MirroredTypesException ex) {
            return ex.getTypeMirrors();
        }
        return Collections.emptyList();
    }

    public static String getClassSimpleName(GetClassValue clazz) {

        String packageName = getTypeMirrorFromAnnotationValue(clazz).get(0).toString();

        try {
            return Class.forName(packageName).getSimpleName();
        } catch (ClassNotFoundException classNotFoundException) {
            return Stream.of(packageName).flatMap(Pattern.compile(".*(?=\\.).")::splitAsStream).parallel().filter(s -> !s.isEmpty()).iterator().next();
        }

    }
}
