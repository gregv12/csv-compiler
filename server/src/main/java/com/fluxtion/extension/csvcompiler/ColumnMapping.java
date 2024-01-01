package com.fluxtion.extension.csvcompiler;

import com.squareup.javapoet.TypeName;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class ColumnMapping {
    private String name;
    private String type = String.class.getCanonicalName();
    private String sourceColumnName = "";
    private int sourceColumnIndex = -1;
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
    private boolean outputField = true;
    private String lookupTable;

    @SneakyThrows
    public TypeName asTypeName(){
        return CsvChecker.asTypeName(getType());
    }
}
