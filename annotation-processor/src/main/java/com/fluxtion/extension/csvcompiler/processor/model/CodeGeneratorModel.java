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
        return """
                import com.fluxtion.extension.csvcompiler.CharArrayCharSequence;
                import com.fluxtion.extension.csvcompiler.CharArrayCharSequence.CharSequenceView;
                import com.fluxtion.extension.csvcompiler.CsvMarshallerLoader;
                import com.fluxtion.extension.csvcompiler.ValidationLogger;                
                
                import com.google.auto.service.AutoService;
                
                import java.io.IOException;
                import java.io.Reader;
                import java.util.ArrayList;
                import java.util.HashMap;
                import java.util.List;
                import java.util.function.Consumer;
                
                import static com.fluxtion.extension.csvcompiler.Conversion.*;
                """;
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
