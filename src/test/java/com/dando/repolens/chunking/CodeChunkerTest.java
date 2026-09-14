package com.dando.repolens.chunking;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeChunkerTest {

    @Test
    void createsOneChunkForEachMethod() {
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

        Path sourcePath = Path.of("Calculator.java");

        JavaSourceFile sourceFile = new JavaSourceFile(sourcePath, sourceCode);
        CodeChunker chunker = new CodeChunker();

        // Check that chunker creates two chunks from the test source code
        List<CodeChunk> chunks = chunker.createChunks(sourceFile);
        assertEquals(2, chunks.size());

        // Check that first chunk corresponds to add method
        CodeChunk firstChunk = chunks.get(0);
        assertEquals(sourcePath, firstChunk.getFilePath());
        assertEquals("Calculator", firstChunk.getClassName());
        assertEquals("add", firstChunk.getMethodName());
        assertEquals(3, firstChunk.getStartLine());
        assertEquals(5, firstChunk.getEndLine());
        assertTrue(firstChunk.getContent().contains("return first + second;"));

        // Check that second chunk corresponds to subtract method
        CodeChunk secondChunk = chunks.get(1);
        assertEquals(sourcePath, secondChunk.getFilePath());
        assertEquals("Calculator", secondChunk.getClassName());
        assertEquals("subtract", secondChunk.getMethodName());
        assertEquals(7, secondChunk.getStartLine());
        assertEquals(9, secondChunk.getEndLine());
        assertTrue(secondChunk.getContent().contains("return first - second;"));
    }
}