package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

@CsvMarshaller(fluent = true)
public class FluentBean {

    private String name;
    private String AGE;
    @ColumnMapping(columnName = "ms zone")
    private int ms_Zone;
    private String  msZone2;

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public String AGE() {
        return AGE;
    }

    public void AGE(String AGE) {
        this.AGE = AGE;
    }

    public int ms_Zone() {
        return ms_Zone;
    }

    public void ms_Zone(int ms_Zone) {
        this.ms_Zone = ms_Zone;
    }

    public String msZone2() {
        return msZone2;
    }

    public void msZone2(String msZone2) {
        this.msZone2 = msZone2;
    }
}
