package com.fluxtion.extension.csvcompiler;

import com.squareup.javapoet.TypeName;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;

@Data
public class ColumnMapping {
    private String name;
    private String type = CharSequence.class.getCanonicalName();
    private String csvColumnName = "";
    private int csvIndex = -1;
    private boolean optional = false;
    private boolean trimOverride = false;
    private String defaultValue = "";
    private String converterCode = "";
    private String converterFunction = "";
    private String converter = "";
    private String converterConfiguration = "";
    private String validationFunction = "";
    private boolean derived = false;
    private boolean escapeOutput = false;
    private String lookupTable;

    @SneakyThrows
    public TypeName asTypeName(){
        return CsvChecker.asTypeName(getType());
    }
}
