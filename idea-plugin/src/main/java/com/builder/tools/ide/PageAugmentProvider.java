package com.builder.tools.ide;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.util.PsiTypesUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PageAugmentProvider extends PsiAugmentProvider {
    
    private static final String GENERATE_METHODS_ANNOTATION = "com.builder.annotations.GenerateMethods";
    private static final String PAGE_BUILDER_ANNOTATION = "com.builder.annotations.PageBuilder";
    
    @Override
    protected @NotNull <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
        if (type != PsiMethod.class) {
            return Collections.emptyList();
        }
        
        if (!(element instanceof PsiClass psiClass)) {
            return Collections.emptyList();
        }
        
        if (DumbService.isDumb(psiClass.getProject())) {
            return Collections.emptyList();
        }
        
        // Check if class has @PageBuilder annotation
        if (!hasPageBuilderAnnotation(psiClass)) {
            return Collections.emptyList();
        }
        
        return RecursionManager.doPreventingRecursion(psiClass, true, () -> {
            List<PsiMethod> methods = new ArrayList<>();
            
            // Find fields with @GenerateMethods annotation
            for (PsiField field : psiClass.getFields()) {
                PsiAnnotation generateMethodsAnnotation = field.getAnnotation(GENERATE_METHODS_ANNOTATION);
                if (generateMethodsAnnotation == null) {
                    continue;
                }
                
                String fieldName = field.getName();
                if (fieldName == null) continue;
                
                String capitalizedFieldName = capitalize(fieldName);
                
                // Generate methods based on annotation attributes
                if (getBooleanValue(generateMethodsAnnotation, "click")) {
                    methods.add(createClickMethod(psiClass, fieldName, capitalizedFieldName));
                }
                
                if (getBooleanValue(generateMethodsAnnotation, "sendKeys")) {
                    methods.add(createSendKeysMethod(psiClass, fieldName, capitalizedFieldName));
                }
                
                if (getBooleanValue(generateMethodsAnnotation, "isDisplayed")) {
                    methods.add(createIsDisplayedMethod(psiClass, fieldName, capitalizedFieldName));
                }
                
                if (getBooleanValue(generateMethodsAnnotation, "getText")) {
                    methods.add(createGetTextMethod(psiClass, fieldName, capitalizedFieldName));
                }
                
                if (getBooleanValue(generateMethodsAnnotation, "selectByText")) {
                    methods.add(createSelectByTextMethod(psiClass, fieldName, capitalizedFieldName));
                }
                
                if (getBooleanValue(generateMethodsAnnotation, "selectByIndex")) {
                    methods.add(createSelectByIndexMethod(psiClass, fieldName, capitalizedFieldName));
                }
            }
            
            return (List<Psi>) methods;
        });
    }
    
    private boolean hasPageBuilderAnnotation(PsiClass psiClass) {
        return psiClass.getAnnotation(PAGE_BUILDER_ANNOTATION) != null;
    }
    
    private LightMethodBuilder createClickMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "click" + capitalizedFieldName;
        PsiType returnType = PsiTypesUtil.getClassType(psiClass);
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(returnType)
            .addModifier(PsiModifier.PUBLIC);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private LightMethodBuilder createSendKeysMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "sendKeys" + capitalizedFieldName;
        PsiType returnType = PsiTypesUtil.getClassType(psiClass);
        PsiType stringType = PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope());
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(returnType)
            .addModifier(PsiModifier.PUBLIC)
            .addParameter("text", stringType);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private LightMethodBuilder createIsDisplayedMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "isDisplayed" + capitalizedFieldName;
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(PsiType.BOOLEAN)
            .addModifier(PsiModifier.PUBLIC);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private LightMethodBuilder createGetTextMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "getText" + capitalizedFieldName;
        PsiType stringType = PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope());
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(stringType)
            .addModifier(PsiModifier.PUBLIC);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private LightMethodBuilder createSelectByTextMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "selectByText" + capitalizedFieldName;
        PsiType returnType = PsiTypesUtil.getClassType(psiClass);
        PsiType stringType = PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope());
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(returnType)
            .addModifier(PsiModifier.PUBLIC)
            .addParameter("text", stringType);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private LightMethodBuilder createSelectByIndexMethod(PsiClass psiClass, String fieldName, String capitalizedFieldName) {
        String methodName = "selectByIndex" + capitalizedFieldName;
        PsiType returnType = PsiTypesUtil.getClassType(psiClass);
        
        LightMethodBuilder method = new LightMethodBuilder(psiClass.getManager(), methodName)
            .setContainingClass(psiClass)
            .setMethodReturnType(returnType)
            .addModifier(PsiModifier.PUBLIC)
            .addParameter("index", PsiType.INT);
        method.setNavigationElement(psiClass);
        return method;
    }
    
    private boolean getBooleanValue(PsiAnnotation annotation, String attributeName) {
        PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
        if (value instanceof PsiLiteral literal && literal.getValue() instanceof Boolean) {
            return (Boolean) literal.getValue();
        }
        return false;
    }
    
    private String capitalize(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
