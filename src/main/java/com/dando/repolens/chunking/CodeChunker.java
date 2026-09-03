package com.dando.repolens.chunking;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import org.springframework.stereotype.Service;

import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Utility class that takes a Java source file and creates a list of code chunks from its methods
@Service
public class CodeChunker {

    // Creates a list of code chunks from a source file
    public List<CodeChunk> createChunks(JavaSourceFile sourceFile) throws IOException {
        List<CodeChunk> chunks = new ArrayList<>();

        // Parse source file and create list of its methods
        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile.getPath());
        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            if (method.getRange().isEmpty()) {
                continue;
            }
            // Determine the length of the method
            Range range = method.getRange().get();
            // Get method name
            String methodName = method.getNameAsString();
            // Find the class belonging to the method
            String className = method
                    .findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .orElse("UnknownClass");

            CodeChunk chunk = new CodeChunk(
                    sourceFile.getPath(),
                    className,
                    methodName,
                    range.begin.line,
                    range.end.line,
                    method.toString()
            );

            chunks.add(chunk);
        }

        return chunks;
    }
}