package es.in2.issuer;


import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.assertj.core.api.Assertions.assertThat;

@AnalyzeClasses(packages = "es.in2.issuer")
class ArchUnitTest {
    private static final String BASE_PACKAGE = "es.in2.issuer";
    private static final Set<String> CONSTANTS_CLASSES =
            Set.of(
                    BASE_PACKAGE + ".shared.domain.util.Constants",
                    BASE_PACKAGE + ".shared.infrastructure.config.SwaggerConfig");

    @ArchTest
    static final ArchRule packageDependenciesAreRespected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            // Define layers
            .layer("Backend").definedBy(BASE_PACKAGE + ".backend..")
            .layer("OIDC4VCI").definedBy(BASE_PACKAGE + ".oidc4vci..")
            .layer("OIDC4VCI-Workflow").definedBy(BASE_PACKAGE + ".oidc4vci.application.workflow..")
            .layer("Shared").definedBy(BASE_PACKAGE + ".shared..")
            // Add constraints
            .whereLayer("Backend").mayOnlyAccessLayers("OIDC4VCI-Workflow", "Shared")
            .whereLayer("OIDC4VCI").mayOnlyAccessLayers("Shared")
            .whereLayer("Shared").mayNotAccessAnyLayer();

    // TODO: Enable this test when the test classes are moved to the same package as the implementation
    /*@ArchTest
    static final ArchRule testClassesShouldResideInTheSamePackageAsImplementation =
            GeneralCodingRules.testClassesShouldResideInTheSamePackageAsImplementation();*/

    @Test
    void classesInSharedMustBeUsedBySharedOrByBothBackendAndOidc4vci() {
        var classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

        Set<JavaClass> sharedClasses = classes.stream()
                .filter(javaClass -> javaClass.getPackageName().contains(".shared"))
                .collect(Collectors.toSet());

        Set<JavaClass> sharedClassesToCheck = sharedClasses
                .stream()
                .filter(javaClass -> !CONSTANTS_CLASSES.contains(javaClass.getName()))
                .filter(this::isNotTestClass)
                .collect(Collectors.toSet());

        Set<JavaClass> backendClasses = filterClassesByPackage(classes, ".backend");
        Set<JavaClass> oidcClasses = filterClassesByPackage(classes, ".oidc4vci");


        for (JavaClass sharedClass : sharedClassesToCheck) {
            boolean usedByBackend = backendClasses.stream().anyMatch(user -> usesClass(user, sharedClass));
            boolean usedByOidc4vci = oidcClasses.stream().anyMatch(user -> usesClass(user, sharedClass));
            boolean usedByShared = sharedClasses.stream().anyMatch(user -> usesClass(user, sharedClass));

            boolean isShared = ((usedByBackend && usedByOidc4vci) || usedByShared);
            assertThat(isShared)
                    .withFailMessage("The class " + sharedClass.getName() +
                            " is not used by both packages nor shared.")
                    .isTrue();
        }
    }

    private Set<JavaClass> filterClassesByPackage(JavaClasses classes, String packageName) {
        return classes.stream()
                .filter(javaClass -> javaClass.getPackageName().contains(packageName))
                .collect(Collectors.toSet());
    }

    private boolean usesClass(JavaClass javaClass, JavaClass sharedClass) {
        return javaClass.getDirectDependenciesFromSelf().stream()
                .anyMatch(dependency -> dependency.getTargetClass().equals(sharedClass));
    }

    private boolean isNotTestClass(JavaClass javaClass) {
        return !javaClass.getSimpleName().endsWith("Test")
                && !javaClass.getSimpleName().endsWith("IT");
    }
}