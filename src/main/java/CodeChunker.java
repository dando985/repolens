import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CodeChunker {

    public List<CodeChunk> createChunks(JavaSourceFile sourceFile) throws IOException {
        List<CodeChunk> chunks = new ArrayList<>();
        CompilationUnit compilationUnit = StaticJavaParser.parse(sourceFile.getPath());
        List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);

        for (MethodDeclaration method : methods) {
            if (method.getRange().isEmpty()) {
                continue;
            }

            Range range = method.getRange().get();
            String methodName = method.getNameAsString();
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