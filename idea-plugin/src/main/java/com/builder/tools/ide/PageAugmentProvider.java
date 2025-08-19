package com.builder.tools.ide;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.augment.PsiAugmentProvider;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PageAugmentProvider extends PsiAugmentProvider {

  private static final String PAGE_BUILDER_ANN = "com.builder.annotations.PageBuilder";
  private static final String PAGE_ANN = "com.builder.annotations.component.Page";
  private static final String GENERATE_METHODS_ANN = "com.builder.annotations.GenerateMethods";
  private static final String WEB_ELEMENT_FQN = "org.openqa.selenium.WebElement";

  @Override
  public <Psi extends PsiElement> @NotNull List<Psi> getAugments(@NotNull PsiElement element,
      @NotNull Class<Psi> type,
      String nameHint) {
    if (type != PsiMethod.class) return List.of();
    if (!(element instanceof PsiClass psiClass)) return List.of();

    PsiModifierList mods = psiClass.getModifierList();
    if (mods == null) return List.of();
    boolean hasPageBuilder = getAnnotation(mods, PAGE_BUILDER_ANN) != null;
    boolean hasPage = getAnnotation(mods, PAGE_ANN) != null;
    if (!hasPageBuilder && !hasPage) return List.of();

    List<PsiMethod> methods = new ArrayList<>();
    Project project = psiClass.getProject();
    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    PsiElementFactory factory = facade.getElementFactory();

    for (PsiField field : psiClass.getAllFields()) {
      PsiModifierList fmods = field.getModifierList();
      PsiAnnotation gm = fmods != null ? getAnnotation(fmods, GENERATE_METHODS_ANN) : null;

      String fieldName = field.getName();
      if (fieldName == null || fieldName.isEmpty()) continue;
      String cap = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

      boolean click = false;
      boolean sendKeys = false;
      boolean isDisplayed = false;
      boolean getText = false;
      boolean selectByText = false;
      boolean selectByIndex = false;

      if (gm != null) {
        click = getBool(gm, "click");
        sendKeys = getBool(gm, "sendKeys");
        isDisplayed = getBool(gm, "isDisplayed");
        getText = getBool(gm, "getText");
        selectByText = getBool(gm, "selectByText");
        selectByIndex = getBool(gm, "selectByIndex");
      } else {
        // Fallback for compiled PSI where SOURCE-retention annotations are not present
        PsiType fType = field.getType();
        if (fType != null && fType.equalsToText(WEB_ELEMENT_FQN)) {
          click = true; // at least provide click for WebElement fields
        }
      }

      if (!(click || sendKeys || isDisplayed || getText || selectByText || selectByIndex)) {
        continue;
      }

      PsiType thisType = factory.createType(psiClass);
      PsiType returnPageType = gm != null ? getReturnPageType(gm, project, psiClass) : null;

      if (click && !methodExists(psiClass, "click" + cap)) {
        methods.add(lightMethod(psiClass, "click" + cap,
            returnPageType != null ? returnPageType : thisType));
      }
      if (sendKeys && !methodExists(psiClass, "sendKeys" + cap)) {
        LightMethodBuilder m = lightMethod(psiClass, "sendKeys" + cap,
            returnPageType != null ? returnPageType : thisType);
        m.addParameter("text",
            PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope()));
        methods.add(m);
      }
      if (isDisplayed && !methodExists(psiClass, "isDisplayed" + cap)) {
        methods.add(lightMethod(psiClass, "isDisplayed" + cap, PsiType.BOOLEAN));
      }
      if (getText && !methodExists(psiClass, "getText" + cap)) {
        methods.add(lightMethod(psiClass, "getText" + cap,
            PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope())));
      }
      if (selectByText && !methodExists(psiClass, "selectByText" + cap)) {
        LightMethodBuilder m = lightMethod(psiClass, "selectByText" + cap, thisType);
        m.addParameter("text",
            PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope()));
        methods.add(m);
      }
      if (selectByIndex && !methodExists(psiClass, "selectByIndex" + cap)) {
        LightMethodBuilder m = lightMethod(psiClass, "selectByIndex" + cap, thisType);
        m.addParameter("index", PsiType.INT);
        methods.add(m);
      }
    }

    @SuppressWarnings("unchecked")
    List<Psi> result = (List<Psi>) (List<?>) methods;
    return result;
  }

  private static boolean methodExists(PsiClass owner, String name) {
    return owner.findMethodsByName(name, true).length > 0;
  }

  private static LightMethodBuilder lightMethod(PsiClass owner, String name, PsiType returnType) {
    LightMethodBuilder m = new LightMethodBuilder(owner.getManager(), name);
    m.setContainingClass(owner);
    m.setMethodReturnType(returnType);
    m.addModifier(PsiModifier.PUBLIC);
    m.setNavigationElement(owner);
    return m;
  }

  private static PsiAnnotation getAnnotation(PsiModifierList mods, String fqn) {
    for (PsiAnnotation a : mods.getAnnotations()) {
      if (fqn.equals(a.getQualifiedName())) return a;
    }
    return null;
  }

  private static boolean getBool(PsiAnnotation ann, String attr) {
    PsiAnnotationMemberValue v = ann.findDeclaredAttributeValue(attr);
    if (v == null) return false;
    String text = v.getText();
    return "true".equals(text) || "Boolean.TRUE".equals(text);
  }

  private static PsiType getReturnPageType(PsiAnnotation ann, Project project, PsiClass context) {
    PsiAnnotationMemberValue v = ann.findDeclaredAttributeValue("returnPage");
    if (v == null) return null;
    if (v instanceof PsiClassObjectAccessExpression classObj) {
      PsiType operandType = classObj.getOperand().getType();
      if (operandType instanceof PsiClassType clsType) {
        PsiClass resolved = clsType.resolve();
        if (resolved != null) {
          return JavaPsiFacade.getElementFactory(project)
              .createTypeByFQClassName(resolved.getQualifiedName(), GlobalSearchScope.allScope(project));
        }
      }
    }
    return null;
  }
} 