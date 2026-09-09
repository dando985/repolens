package com.dando.repolens.scanner;

import com.dando.repolens.model.JavaSourceFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RepositoryScanner {

    List<JavaSourceFile> scan(Path repositoryPath) throws IOException;

}