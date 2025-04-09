package net.thenextlvl.worlds.generator;

import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@NullMarked
public abstract class Generator {
    protected final String className;
    protected final String packageName;

    protected Generator(String packageName, String className) {
        this.className = className;
        this.packageName = packageName;
    }

    protected abstract TypeSpec generate();

    protected abstract JavaFile.Builder file(JavaFile.Builder builder);

    public void writeToFile(Path parent) throws IOException {
        file(JavaFile.builder(this.packageName, this.generate()))
                .indent("    ")
                .skipJavaLangImports(true)
                .build()
                .writeTo(parent, StandardCharsets.UTF_8);
    }
}
