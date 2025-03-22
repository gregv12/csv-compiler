package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.Data;

@Data
@CsvMarshaller
public class BeanWithEnum {

    public enum InnerEnum {VALUE_1, VALUE_2}

    private TestEnum assetName;
    private InnerEnum innergetEnumValue;
}
