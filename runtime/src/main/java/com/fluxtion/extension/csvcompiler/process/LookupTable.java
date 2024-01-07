package com.fluxtion.extension.csvcompiler.process;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

@CsvMarshaller
@Data
public class LookupTable {
    private String table;
    private String key;
    private String value;
}
