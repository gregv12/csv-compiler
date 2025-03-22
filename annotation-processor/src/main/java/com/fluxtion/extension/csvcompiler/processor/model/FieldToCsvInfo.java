/*
 *
 * Copyright 2022-2022 greg higgins
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.fluxtion.extension.csvcompiler.processor.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class FieldToCsvInfo {

    private String sourceMethod;
    private String nullValue;
    private String converterId;
    private String sourceType;
    private boolean enumField;
    private boolean escapeOutputField;
    private boolean writeFieldToOutput;
    private static final Set<String> primitiveTypeSet = new HashSet<>();
    static{
        primitiveTypeSet.add("int");
        primitiveTypeSet.add("short");
        primitiveTypeSet.add("byte");
        primitiveTypeSet.add("char");
        primitiveTypeSet.add("long");
        primitiveTypeSet.add("float");
        primitiveTypeSet.add("boolean");
        primitiveTypeSet.add("double");
    }

    public String getWriteStatement() {
        if(!writeFieldToOutput){
            return "";
        }
        String writeStatement;
        String sourceMethodOriginal = sourceMethod;
        if(enumField){
            sourceMethod += "().name";
        }
        if (converterId == null) {
            writeStatement = "builder.append(target." + sourceMethod + "());";
        } else {
            writeStatement = converterId + ".toCharSequence(target." + sourceMethod + "() , builder);";
        }
        if (nullValue != null && !isPrimitive(sourceType)) {
            //null check
            writeStatement = "if(target." + sourceMethod + "() != null) {"
                    + writeStatement
                    + "}";
        }
        if(escapeOutputField){
            writeStatement = "builder.append(\"\\\"\");\n\t\t" + writeStatement  + "\n\t\tbuilder.append(\"\\\"\");";
        }
        sourceMethod = sourceMethodOriginal;
        return writeStatement;
    }

    public static boolean isPrimitive(String targetType) {
        return primitiveTypeSet.contains(targetType);
    }
}
