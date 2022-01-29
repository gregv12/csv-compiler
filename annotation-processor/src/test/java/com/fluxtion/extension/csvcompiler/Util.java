package com.fluxtion.extension.csvcompiler;

import com.fluxtion.extension.csvcompiler.processor.CsvMarshallerGenerator;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.joor.CompileOptions;
import org.joor.Reflect;

import java.util.Objects;

public class Util {

    @SneakyThrows
    public static <T> @NotNull T compileInstance(String fqn, String content) {
        Objects.requireNonNull(fqn);
        Objects.requireNonNull(content);

        Class<T> classT = Reflect.compile(
                        fqn, content,
                        new CompileOptions()
                                .processors(new CsvMarshallerGenerator())
                                .options("-source", "11")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED ")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED ")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
                )
                .get();
        return classT.getDeclaredConstructor().newInstance();
    }
}
