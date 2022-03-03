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

package com.fluxtion.extension.csvcompiler.jmh.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.ToString;

@CsvMarshaller(processEscapeSequences = true, mappingRow = 0, headerLines = 0, formatSource = true)
@ToString
public class SampleData {

    @ColumnMapping(columnIndex = 0)
    private int rowId;
    @ColumnMapping(columnIndex = 1)
    private String description;
//    @ColumnMapping(columnIndex = 9)
//    private double percentage;

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

//    public double getPercentage() {
//        return percentage;
//    }
//
//    public void setPercentage(double percentage) {
//        this.percentage = percentage;
//    }
}
