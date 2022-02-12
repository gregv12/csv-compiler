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

package com.fluxtion.extension.csvcompiler.beans;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@CsvMarshaller(
        formatSource = true,
        mappingRow = 0,
        headerLines = 0,
        acceptPartials = true,
        processEscapeSequences = true
)
@EqualsAndHashCode
@ToString
public class StringsOnly {


    @ColumnMapping(columnIndex = 0)
    private String field1;
    @ColumnMapping(columnIndex = 1)
    private String field2;
    @ColumnMapping(columnIndex = 2)
    private String field3;

    public StringsOnly() {
    }

    public StringsOnly(String field1) {
        this.field1 = field1;
    }

    public StringsOnly(String field1, String field2) {
        this.field1 = field1;
        this.field2 = field2;
    }

    public StringsOnly(String field1, String field2, String field3) {
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    @CsvMarshaller(
            formatSource = true,
            mappingRow = 0,
            headerLines = 0,
            acceptPartials = true,
            processEscapeSequences = true,
            skipEmptyLines = true
    )
    @EqualsAndHashCode
    @ToString
    public static class SkipEmptyLines {

        @ColumnMapping(columnIndex = 0)
        private String field1;
        @ColumnMapping(columnIndex = 1)
        private String field2;
        @ColumnMapping(columnIndex = 2)
        private String field3;

        public SkipEmptyLines() {
        }

        public SkipEmptyLines(String field1) {
            this.field1 = field1;
        }

        public SkipEmptyLines(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public SkipEmptyLines(String field1, String field2, String field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }

        public String getField3() {
            return field3;
        }

        public void setField3(String field3) {
            this.field3 = field3;
        }
    }
}
