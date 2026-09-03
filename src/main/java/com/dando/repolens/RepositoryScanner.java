package com.dando.repolens;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RepositoryScanner {

    List<JavaSourceFile> scan(Path repositoryPath)
            throws IOException;
}