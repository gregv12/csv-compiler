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

package com.fluxtion.extension.csvcompiler.jmh;

import com.fluxtion.extension.csvcompiler.annotations.ColumnMapping;
import com.fluxtion.extension.csvcompiler.annotations.CsvMarshaller;

@CsvMarshaller(noHeader = true, newBeanPerRecord = false)
public class CanadaCharData {

    @ColumnMapping(columnIndex = 0)
    CharSequence doubleValue;

    public CharSequence getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(CharSequence CharSequence) {
        this.doubleValue = doubleValue;
    }
}
