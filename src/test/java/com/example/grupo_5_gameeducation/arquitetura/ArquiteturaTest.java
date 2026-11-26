package com.example.grupo_5_gameeducation.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Arrays;

/**
 * Experimento de MANUTENIBILIDADE (ASR do ADR-001, pedido no feedback da AC2): as fronteiras
 * do monolito modular e da arquitetura hexagonal sao verificadas pelo build. Se alguem quebrar
 * uma fronteira, o ./mvnw verify falha e o PR nao passa.
 *
 * Modulo = primeiro pacote abaixo de com.example.grupo_5_gameeducation (cursos, catalogo,
 * colaboradores, ranking...). Cada modulo tem api/ (contrato publico) e internal/ (implementacao).
 */
@AnalyzeClasses(packages = ArquiteturaTest.BASE, importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    static final String BASE = "com.example.grupo_5_gameeducation";

    /** Regra 1: um modulo nunca acessa o internal de outro modulo, so o api dele. */
    @ArchTest
    static final ArchRule modulo_nao_acessa_internal_de_outro_modulo = classes()
            .that().resideInAPackage(BASE + "..")
            .should(naoDependerDoInternalDeOutroModulo())
            .because("modulos conversam so pelos contratos publicos em <modulo>/api (ADR-001)");

    /** Regra 2: o contrato publico (api) nao depende da implementacao (internal). O sentido e internal -> api. */
    @ArchTest
    static final ArchRule api_nao_depende_de_internal = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..internal..")
            .because("o contrato publico nao pode vazar a implementacao interna (feedback da AC2)");

    /** Regra 3: o dominio e puro, sem framework (nucleo da arquitetura hexagonal). */
    @ArchTest
    static final ArchRule dominio_sem_framework = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..")
            .because("o dominio da AC1 e o nucleo da hexagonal e nao conhece Spring nem JPA (secao 4 do plano)");

    private static ArchCondition<JavaClass> naoDependerDoInternalDeOutroModulo() {
        return new ArchCondition<>("not depend on the internal package of another module") {
            @Override
            public void check(JavaClass origem, ConditionEvents events) {
                String moduloOrigem = modulo(origem.getPackageName());
                for (Dependency dependencia : origem.getDirectDependenciesFromSelf()) {
                    String pacoteAlvo = dependencia.getTargetClass().getPackageName();
                    if (!pacoteAlvo.startsWith(BASE + ".") || !ehInternal(pacoteAlvo)) {
                        continue;
                    }
                    if (!modulo(pacoteAlvo).equals(moduloOrigem)) {
                        events.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
                    }
                }
            }
        };
    }

    private static String modulo(String pacote) {
        if (!pacote.startsWith(BASE + ".")) {
            return "";
        }
        return pacote.substring(BASE.length() + 1).split("\\.")[0];
    }

    private static boolean ehInternal(String pacote) {
        return Arrays.asList(pacote.split("\\.")).contains("internal");
    }
}
