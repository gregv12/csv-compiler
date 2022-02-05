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

package com.fluxtion.extension.csvcompiler.processor;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Configuration parameters for line endings, separators, ignore characters
 * 
 * @author gregp
 */
@Data
@AllArgsConstructor
public class CharTokenConfig {
    private char lineEnding;
    private char fieldSeparator;
    private char ignoredChars;
    
    public static final CharTokenConfig WINDOWS = new CharTokenConfig('\n',',','\r');
    public static final CharTokenConfig UNIX = new CharTokenConfig('\n',',');

    public CharTokenConfig(char lineEnding, char fieldSeparator) {
        this(lineEnding, fieldSeparator, '\u0000');
    }
}
