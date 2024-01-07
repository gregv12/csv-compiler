package com.fluxtion.extension.csvcompiler;

import lombok.Data;

@Data
public class AggregateField {
    private String name;
    private String type;
    private String constructor = "";
}
