package com.practice.coding.sort;

import com.practice.coding.sort.SortOptionsConstants;
import com.practice.coding.utils.StringUtils;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class SortOptionsProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        var sortOptionsElementOptional = roundEnv.getRootElements()
                .stream()
                .filter(element -> element.getSimpleName().contentEquals(SortOptions.class.getSimpleName()))
                .findFirst();

        Trees trees = Trees.instance(processingEnv);

        if (sortOptionsElementOptional.isEmpty()) {
            return false;
        }

        var sortOptionsElement = sortOptionsElementOptional.get();
        final String LONG = "LONG";
        final String SHORT = "SHORT";

        try {
            var jfo = processingEnv.getFiler()
                            .createSourceFile("com.practice.coding.sort.SortOptionsConstants");
            try (Writer writer = jfo.openWriter()) {
                writer.write(String.format(
                        """
                        package com.practice.coding.sort;%n
                        public interface SortOptionsConstants {%n"""
                ));
                sortOptionsElement.getEnclosedElements()
                        .stream()
                        .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD)
                        .map(enclosedElement -> (VariableElement) enclosedElement)
                        .forEach((VariableElement enclosedElement) -> {
                            var elementTreePath = trees.getPath(enclosedElement);
                            final String[] longOption = {""};
                            final String[] shortOption = {""};
                            new TreePathScanner<Void, Void>() {
                                @Override
                                public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
                                    var arguments = node.getArguments();
                                    if (!arguments.isEmpty()) {
                                        longOption[0] = arguments.get(0).toString();
                                        shortOption[0] = arguments.get(1).toString();
                                    }
                                    return super.visitMethodInvocation(node, unused);
                                }
                            }.scan(elementTreePath, null);

                            var name = enclosedElement.getSimpleName().toString().toLowerCase();
                            var subTypeName = StringUtils.capitalize(name);
                            try {
                                writer.write(
                                        String.format("""
                                                interface %s {
                                                    String %s = %s;
                                                    String %s = %s;
                                                }
                                                """,
                                                subTypeName,
                                                LONG, longOption[0],
                                                SHORT, shortOption[0])
                                );
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                writer.write("}");
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
