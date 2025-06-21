/*
 * creedengo - Java language - Provides rules to reduce the environmental footprint of your Java programs
 * Copyright © 2024 Green Code Initiative (https://green-code-initiative.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.greencodeinitiative.creedengo.java;

import java.util.Collections;
import java.util.List;

import org.greencodeinitiative.creedengo.java.checks.*;
import org.sonar.plugins.java.api.CheckRegistrar;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Provide the "checks" (implementations of rules) classes that are going be executed during
 * source code analysis.
 * <p>
 * This class is a batch extension by implementing the {@link org.sonar.plugins.java.api.CheckRegistrar} interface.
 */
@SonarLintSide
public class JavaCheckRegistrar implements CheckRegistrar {
    static final List<Class<? extends JavaCheck>> ANNOTATED_RULE_CLASSES = List.of(
            ArrayCopyCheck.class,
            IncrementCheck.class,
            AvoidUsageOfStaticCollections.class,
            AvoidGettingSizeCollectionInLoop.class,
            AvoidRegexPatternNotStatic.class,
            NoFunctionCallWhenDeclaringForLoop.class,
            AvoidStatementForDMLQueries.class,
            AvoidSpringRepositoryCallInLoopOrStreamCheck.class,
            AvoidSQLRequestInLoop.class,
            AvoidFullSQLRequest.class,
            OptimizeReadFileExceptions.class,
            InitializeBufferWithAppropriateSize.class,
            AvoidSetConstantInBatchUpdate.class,
            FreeResourcesOfAutoCloseableInterface.class,
            AvoidMultipleIfElseStatement.class,
            UseOptionalOrElseGetVsOrElse.class,
            MakeNonReassignedVariablesConstants.class
    );

    /**
     * Register the classes that will be used to instantiate checks during analysis.
     */
    @Override
    public void register(RegistrarContext registrarContext) {
        // Call to registerClassesForRepository to associate the classes with the correct repository key
        registrarContext.registerClassesForRepository(
            JavaRulesDefinition.REPOSITORY_KEY, 
            checkClasses(), 
            testCheckClasses()
        );
    }

    /**
     * Lists all the main checks provided by the plugin
     */
    public static List<Class<? extends JavaCheck>> checkClasses() {
        return ANNOTATED_RULE_CLASSES;
    }

    /**
     * Lists all the test checks provided by the plugin
     */
    public static List<Class<? extends JavaCheck>> testCheckClasses() {
        return Collections.emptyList();
    }
}
