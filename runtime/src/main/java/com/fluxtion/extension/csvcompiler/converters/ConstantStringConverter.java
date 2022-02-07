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

package com.fluxtion.extension.csvcompiler.converters;

import com.fluxtion.extension.csvcompiler.FieldConverter;

public class ConstantStringConverter implements FieldConverter<String> {

    public static final String ID = "ConstantStringConverter";

    private String constantString;

    @Override
    public void setConversionConfiguration(String conversionConfiguration) {
        this.constantString = conversionConfiguration;
    }

    @Override
    public String fromCharSequence(CharSequence charSequence) {
        return constantString;
    }

    @Override
    public String getName() {
        return ID;
    }
}
