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

import com.fluxtion.extension.csvcompiler.annotations.Validator;
import lombok.Value;

@Value
public class ValidatorConfig {

    String lambda;
    String method;
    String errorMessage;
    boolean exitOnFailure;
    String className;

    public static ValidatorConfig fromAnnotation(Validator validator, String className){
        return new ValidatorConfig(
                validator.value(),
                validator.validationMethod(),
                validator.errorMessage(),
                validator.exitOnFailure(),
                className
        );
    }
}
