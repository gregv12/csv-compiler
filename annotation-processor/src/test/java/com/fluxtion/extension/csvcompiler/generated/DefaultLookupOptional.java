package com.fluxtion.extension.csvcompiler.generated;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import com.fluxtion.extension.csvcompiler.annotations.DataMapping;

@CsvMarshaller
public class DefaultLookupOptional {

    @DataMapping(lookupName = "meta")
    @ColumnMapping(optionalField = true, defaultValue = "myDefault")
    private String dataFile;

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }
}
