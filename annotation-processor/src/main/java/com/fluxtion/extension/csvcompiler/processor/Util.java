/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler.processor;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.joor.CompileOptions;
import org.joor.Reflect;

import java.util.ArrayList;
import java.util.List;
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
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED ")
                                .options("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
                )
                .get();
        return classT.getDeclaredConstructor().newInstance();
    }

    public static <E> List<E> listOf(E... input) {
        @SuppressWarnings("unchecked")
        List<E> target = new ArrayList<>(input.length);
        for (int i = 0; i < input.length; i++) {
            target.add(input[i]);
        }
        return target;
    }
}
