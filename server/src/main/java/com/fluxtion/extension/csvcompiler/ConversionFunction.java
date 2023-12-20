package com.fluxtion.extension.csvcompiler;

import lombok.Data;

@Data
public class ConversionFunction {
    private String name;
    private String code;
    private String convertsTo;
    private String configuration;
}
