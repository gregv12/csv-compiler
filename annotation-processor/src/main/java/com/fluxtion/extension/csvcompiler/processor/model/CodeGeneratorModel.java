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

package com.fluxtion.extension.csvcompiler.processor.model;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public interface CodeGeneratorModel {

    List<CsvToFieldInfoModel> fieldInfoList();

    List<FieldToCsvInfo> outputFieldInfoList();

    String getFqn();

    int getHeaderLines();

    default boolean isHeaderPresent(){
        return getHeaderLines() > 0;
    }

    default String getImports() {
        return "import com.fluxtion.extension.csvcompiler.BaseMarshaller;\n" +
               "import com.fluxtion.extension.csvcompiler.BaseMarshallerNoBufferCopy;\n" +
               "import com.fluxtion.extension.csvcompiler.CharArrayCharSequence;\n" +
               "import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;\n" +
               "import com.fluxtion.extension.csvcompiler.RowMarshaller;\n" +
               "import com.fluxtion.extension.csvcompiler.ValidationLogger;\n" +
               "import com.fluxtion.extension.csvcompiler.CsvProcessingException;\n" +
               "\n" +
               "import com.google.auto.service.AutoService;\n" +
               "\n" +
               "import java.io.IOException;\n" +
               "import java.io.Reader;\n" +
               "import java.util.ArrayList;\n" +
               "import java.util.HashMap;\n" +
               "import java.util.List;\n" +
               "import java.util.function.BiConsumer;\n" +
               "import java.util.function.BiPredicate;\n" +
               "import java.util.function.Consumer;\n" +
               "import java.util.function.Consumer;\n" +
               "import java.util.function.Function;\n" +
               "import java.util.regex.Pattern;\n" +
               "\n" +
               "import static com.fluxtion.extension.csvcompiler.converters.Conversion.*;\n";
    }

    int getMappingRow();

    default boolean isMappingRowPresent(){
        return getMappingRow() > 0;
    }

    String getMarshallerClassName();

    String getPackageName();

    String getTargetClassName();

    String getPostProcessMethod();

    default boolean isPostProcessMethodSet(){
        return Objects.nonNull(getPostProcessMethod()) && !StringUtils.isBlank(getPostProcessMethod());
    }

    boolean isProcessEscapeSequence();

    boolean isIgnoreQuotes();

    boolean isFormatSource();

    boolean isSkipCommentLines();

    boolean isSkipEmptyLines();

    char getDelimiter();

    boolean isNewBeanPerRecord();

    boolean isAcceptPartials();

    boolean isTrim();

    boolean isFailOnFirstError();

    int getMaximumInlineFieldsLimit();

    default boolean isLoopAssignment(){
        return getMaximumInlineFieldsLimit() < fieldInfoList().size();
    }
}
