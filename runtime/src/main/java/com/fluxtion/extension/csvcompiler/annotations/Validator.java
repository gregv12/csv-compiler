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

package com.fluxtion.extension.csvcompiler.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.BiConsumer;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface Validator {

    /**
     * A {@link java.util.function.Predicate} in the form of lambda expression that will be applied to the value in the
     * bean using the correct get expression for this property
     *
     * @return The validation predicate
     */
    String validationLambda() default "";

    /**
     * A validation method in the target class that accepts a {@link BiConsumer <String, Boolean> validatorLog }
     *
     * @return the validation method
     */
    String validationMethod() default "";

    /**
     * Error message to log on a validation failure
     * @return failure message
     */
    String errorMessage() default "";

    /**
     * Control the continuation of source parsing when a failure occurs. By default the parser exits on a validation
     * failure, set exitOnFailure to false in order to continue processing and log the failure.
     * @return fail fast flag
     */
    boolean exitOnFailure() default true;
}
