import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
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

            CodeChunk chunk = new CodeChunk(
                    sourceFile.getPath(),
                    range.begin.line,
                    range.end.line,
                    method.toString()
            );

            chunks.add(chunk);
        }

        return chunks;
    }
}