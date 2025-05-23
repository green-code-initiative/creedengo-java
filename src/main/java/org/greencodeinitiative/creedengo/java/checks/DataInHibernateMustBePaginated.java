package org.greencodeinitiative.creedengo.java.checks;

import java.util.*;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.InputFileScannerContext;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.Tree.Kind;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;


import java.util.Collections;
import java.util.List;

import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.semantic.Type;

import java.util.Collections;
import java.util.List;

@Rule(key = "GCI1111")
public class DataInHibernateMustBePaginated extends IssuableSubscriptionVisitor {

    private static final String MESSAGE = "Hibernate queries must be paginated to avoid excessive data loading";

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Collections.singletonList(Kind.METHOD);
    }

    @Override
    public void visitNode(Tree tree) {
        MethodTree methodTree = (MethodTree) tree;
        ClassTree enclosingClass = getEnclosingClass(tree);

        if (enclosingClass == null || !isRepository(enclosingClass)) {
            return; // Ne rien faire si ce n’est pas un Repository
        }


        // Récupérer le nom de la méthode
        String methodName = methodTree.simpleName().name();

        // Récupérer le type de retour
        String returnType = methodTree.returnType().symbolType().toString();

        // Vérifier s’il retourne une collection entière (sans pagination)
        boolean returnsAllData = retrournAllData(returnType);

        // Vérifier s’il utilise la pagination
        boolean hasPaginationParam = isHasPaginationParam(methodTree);

        // Vérifier la présence de l’annotation @Query
        boolean usesQueryAnnotation = isUsesQueryAnnotation(methodTree);

        extracted(returnsAllData, hasPaginationParam, methodTree, usesQueryAnnotation);

    }

    private void extracted(boolean returnsAllData, boolean hasPaginationParam, MethodTree methodTree, boolean usesQueryAnnotation) {
        // Déclencher la règle
        if (returnsAllData && !hasPaginationParam) {
            reportIssue(methodTree.simpleName(),
                    MESSAGE);
        } else if (usesQueryAnnotation && returnsAllData && !hasPaginationParam) {
            reportIssue(methodTree.simpleName(),
                    MESSAGE);
        }
    }

    private static boolean isUsesQueryAnnotation(MethodTree methodTree) {
        boolean usesQueryAnnotation = methodTree.modifiers().annotations().stream()
                .anyMatch(ann -> ann.annotationType().toString().equals("Query"));
        return usesQueryAnnotation;
    }

    private static boolean isHasPaginationParam(MethodTree methodTree) {
        boolean hasPaginationParam = methodTree.parameters().stream()
                .anyMatch(param -> {
                    String type = param.type().toString();
                    return type.contains("Pageable") || type.contains("PageRequest");
                });
        return hasPaginationParam;
    }

    public boolean retrournAllData(String returnType) {
        return returnType.startsWith("List")
                || returnType.startsWith("Set")
                || returnType.startsWith("Collection")
                || returnType.startsWith("Iterable");
    }

    private ClassTree getEnclosingClass(Tree tree) {
        Tree parent = tree.parent();
        while (parent != null && !(parent instanceof ClassTree)) {
            parent = parent.parent();
        }
        return (ClassTree) parent;
    }

    private boolean isRepository(ClassTree classTree) {
        // Vérifie si la classe implémente une interface Repository (comme JpaRepository)
        Symbol.TypeSymbol symbol = classTree.symbol();
        if (symbol == null) {
            return false;
        }

        for (Type iface : symbol.type().symbol().interfaces()) {
            String ifaceName = iface.fullyQualifiedName();
            if (ifaceName.contains("JpaRepository") || ifaceName.contains("CrudRepository") || ifaceName.contains("PagingAndSortingRepository")) {
                return true;
            }
        }

        // Ou si l'annotation Repository est présente
        return classTree.modifiers().annotations().stream()
                .anyMatch(ann -> ann.annotationType().toString().endsWith("Repository"));
    }




}