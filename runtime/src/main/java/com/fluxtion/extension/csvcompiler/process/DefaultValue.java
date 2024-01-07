package com.fluxtion.extension.csvcompiler.process;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

@CsvMarshaller(processEscapeSequences = true)
@Data
public class DefaultValue {
    private String columnName;
    private String defaultValue = "";
}
