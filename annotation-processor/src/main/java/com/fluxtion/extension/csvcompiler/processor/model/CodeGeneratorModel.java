package com.fluxtion.extension.csvcompiler.processor.model;

import java.util.List;

public interface CodeGeneratorModel {

    List<CsvToFieldInfoModel> fieldInfoList();

    String getFqn();

    int getHeaderLines();

    default boolean isHeaderPresent(){
        return getHeaderLines() > -1;
    }

    default String getImports() {
        return "import com.fluxtion.extension.csvcompiler.CharArrayCharSequence;\n" +
               "import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;\n" +
               "import com.fluxtion.extension.csvcompiler.CsvMarshallerLoader;\n" +
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
               "import java.util.function.Consumer;\n" +
               "\n" +
               "import static com.fluxtion.extension.csvcompiler.Conversion.*;\n";
    }

    int getMappingRow();

    default boolean isMappingRowPresent(){
        return getMappingRow() > 0;
    }

    String getMarshallerClassName();

    String getPackageName();

    String getTargetClassName();

    boolean isProcessEscapeSequence();

    char getIgnoreCharacter();

    boolean isIgnoreQuotes();

    boolean isFormatSource();

    boolean isSkipCommentLines();

    boolean isSkipEmptyLines();

    boolean isAsciiOnlyHeader();

    char getDelimiter();

    boolean isNewBeanPerRecord();

    boolean isAcceptPartials();

    boolean isTrim();
}
