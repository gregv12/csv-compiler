/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joor;

/* [java-8] */

import javax.annotation.processing.Processor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Lukas Eder
 */
public final class CompileOptions {

    final List<? extends Processor> processors;
    final List<String> options;
    final ClassLoader classLoader;
    final String targetDirectory;

    public CompileOptions() {
        this(
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                null
        );
    }

    public CompileOptions(List<? extends Processor> processors, List<String> options, ClassLoader classLoader, String targetDirectory) {
        this.processors = processors;
        this.options = options;
        this.classLoader = classLoader;
        this.targetDirectory = targetDirectory;
    }

    public final CompileOptions processors(Processor... newProcessors) {
        return processors(Arrays.asList(newProcessors));
    }

    public final CompileOptions processors(List<? extends Processor> newProcessors) {
        return new CompileOptions(newProcessors, options, classLoader, targetDirectory);
    }

    public final CompileOptions options(String... newOptions) {
        return options(Arrays.asList(newOptions));
    }

    public final CompileOptions options(List<String> newOptions) {
        return new CompileOptions(processors, newOptions, classLoader, targetDirectory);
    }

    public CompileOptions saveClassesTo(String targetDirectory) {
        return new CompileOptions(processors, options, classLoader, targetDirectory);
    }

    public boolean writeToFile(){
        return targetDirectory != null;
    }

    final boolean hasOption(String opt) {
        for (String option : options)
            if (option.equalsIgnoreCase(opt))
                return true;

        return false;
    }

    public final CompileOptions classLoader(ClassLoader newClassLoader) {
        return new CompileOptions(processors, options, newClassLoader, targetDirectory);
    }
}
/* [/java-8] */
