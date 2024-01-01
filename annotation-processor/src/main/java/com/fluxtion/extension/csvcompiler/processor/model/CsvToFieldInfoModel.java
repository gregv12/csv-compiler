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

import org.apache.commons.lang3.StringUtils;

public interface CsvToFieldInfoModel {
    String getTargetSetMethodName();

    String getTargetGetMethodName();

    String getFieldIdentifier();

    int getFieldIndex();

    String getSourceFieldName();

    String getOutFieldName();

    boolean isMandatory();

    boolean isDefaultOptionalField();

    boolean isTrim();

    boolean isIndexField();

    String getUpdateTarget();

    //converter
    String getConverterInstanceId();

    String getConverterClassName();

    String getConvertConfiguration();

    default boolean isConverterApplied() {
        return getConverterClassName() != null && !StringUtils.isBlank(getConverterClassName()) && getConverterClassName().length() > 0;
    }

    //validator
    boolean isValidated();

    String getValidatorDeclaration();

    String getValidatorInvocation();

    //lookup
    String getLookupField();

    String getLookupKey();

    boolean isDerived();
    boolean isWriteFieldToOutput();

    default boolean isLookupApplied() {
        return getLookupKey() != null && !StringUtils.isBlank(getLookupKey());
    }

}
