package com.dando.repolens.service;

import com.dando.repolens.chunking.CodeChunker;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import com.dando.repolens.scanner.RepositoryScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryAnalysisServiceTest {

    @Mock
    RepositoryScanner repositoryScanner;

    @Mock
    CodeChunker codeChunker;

    @InjectMocks
    RepositoryAnalysisService analysisService;

    @TempDir
    Path repositoryPath;

    @Test
    void combinesChunksFromEveryScannedSourceFile() throws IOException {
        JavaSourceFile calculatorFile = new JavaSourceFile(repositoryPath.resolve("Calculator.java"), "public class Calculator {}");
        CodeChunk addChunk = createChunk(calculatorFile, "Calculator", "add");
        CodeChunk subtractChunk = createChunk(calculatorFile, "Calculator", "subtract");

        JavaSourceFile userServiceFile = new JavaSourceFile(repositoryPath.resolve("UserService.java"), "public class UserService {}");
        CodeChunk findUserChunk = createChunk(userServiceFile, "UserService", "findUser");

        when(repositoryScanner.scan(repositoryPath)).thenReturn(List.of(calculatorFile, userServiceFile));

        when(codeChunker.createChunks(calculatorFile)).thenReturn(List.of(addChunk, subtractChunk));
        when(codeChunker.createChunks(userServiceFile)).thenReturn(List.of(findUserChunk));

        List<CodeChunk> results = analysisService.findMethodChunks(repositoryPath);

        // Check that 3 code chunks are returned under one list
        assertEquals(3, results.size());
        assertSame(addChunk, results.get(0));
        assertSame(subtractChunk, results.get(1));
        assertSame(findUserChunk, results.get(2));

        verify(repositoryScanner).scan(repositoryPath);
        verify(codeChunker).createChunks(calculatorFile);
        verify(codeChunker).createChunks(userServiceFile);
    }

    @Test
    void returnsEmptyListWhenRepositoryContainsNoJavaFiles() throws IOException {
        when(repositoryScanner.scan(repositoryPath)).thenReturn(List.of());

        List<CodeChunk> results = analysisService.findMethodChunks(repositoryPath);

        assertTrue(results.isEmpty());

        verify(repositoryScanner).scan(repositoryPath);
        verifyNoInteractions(codeChunker);
    }

    @Test
    void propagatesScannerFailureWithoutCallingChunker() throws IOException {
        IOException expectedException = new IOException("Unable to read repository");

        when(repositoryScanner.scan(repositoryPath)).thenThrow(expectedException);

        IOException actualException = assertThrows(IOException.class,
                () -> analysisService.findMethodChunks(
                        repositoryPath
                )
        );

        assertSame(expectedException, actualException);

        verify(repositoryScanner).scan(repositoryPath);
        verifyNoInteractions(codeChunker);
    }

    // Helper function to create sample code chunk
    private CodeChunk createChunk(JavaSourceFile sourceFile, String className, String methodName) {
        return new CodeChunk(
                sourceFile.getPath(),
                className,
                methodName,
                1,
                3,
                "void " + methodName + "() {}"
        );
    }
}