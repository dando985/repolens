package com.dando.repolens.chunking;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeChunkerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsOneChunkForEachMethod() throws IOException {
        String sourceCode = """
                public class Calculator {
                
                    public int add(int first, int second) {
                        return first + second;
                    }
                
                    public int subtract(int first, int second) {
                        return first - second;
                    }
                }
                """;

        Path sourcePath = temporaryDirectory.resolve("Calculator.java");

        Files.writeString(sourcePath, sourceCode);

        JavaSourceFile sourceFile = new JavaSourceFile(sourcePath, sourceCode);

        CodeChunker chunker = new CodeChunker();
        List<CodeChunk> chunks = chunker.createChunks(sourceFile);
        assertEquals(2, chunks.size());

        CodeChunk firstChunk = chunks.get(0);
        assertEquals("Calculator", firstChunk.getClassName());
        assertEquals("add", firstChunk.getMethodName());
        assertEquals(3, firstChunk.getStartLine());
        assertEquals(5, firstChunk.getEndLine());
        assertTrue(firstChunk.getContent().contains("return first + second;"));

        CodeChunk secondChunk = chunks.get(1);
        assertEquals("Calculator", secondChunk.getClassName());
        assertEquals("subtract", secondChunk.getMethodName());
        assertEquals(7, secondChunk.getStartLine());
        assertEquals(9, secondChunk.getEndLine());
    }
}