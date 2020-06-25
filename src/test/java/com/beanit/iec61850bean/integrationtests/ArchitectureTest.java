package com.beanit.iec61850bean.integrationtests;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.beanit")
public class ArchitectureTest {

  @ArchTest
  public static final ArchRule only_josistack_accesses_its_internal_pkg =
      classes()
          .that()
          .resideInAPackage("..josistack.internal..")
          .should()
          .onlyBeAccessed()
          .byAnyPackage("..josistack..");
}
