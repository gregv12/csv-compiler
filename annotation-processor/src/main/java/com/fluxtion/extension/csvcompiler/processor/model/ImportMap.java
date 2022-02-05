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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A holder of imports for a java class. Classes are added with the addImport 
 * method, this returns the String classname to be used in code. If the class name
 * is already in use then the returned value will be the fqn.
 * @author Greg Higgins
 */
public class ImportMap {

    private final Set<Class> importedClassSet;
    private final Set<Class> staticImportedClassSet;

    private ImportMap() {
        importedClassSet = new HashSet<>();
        staticImportedClassSet = new HashSet<>();
    }

    public static ImportMap newMap() {
        return new ImportMap();
    }

    public static ImportMap newMap(Class... clazzes) {
        final ImportMap importMap = new ImportMap();
        for (Class clazz : clazzes) {
            importMap.addImport(clazz);
        }
        return importMap;
    }

    public String addImport(Class clazz) {
        String className = clazz.getEnclosingClass()==null?
                clazz.getCanonicalName():clazz.getEnclosingClass().getCanonicalName();
        String simpleName = clazz.getSimpleName();
        if(clazz.isPrimitive() || className.startsWith("java.lang") ){
            return simpleName;
        }
        if (importedClassSet.contains(clazz)) {
            className = clazz.getSimpleName();
        }else if(importedClassSet.stream().map(c -> c.getSimpleName()).noneMatch(name -> name.equals(simpleName))){
            importedClassSet.add(clazz);
            className = clazz.getSimpleName();
        }
        return className;
    }
    
    public void addStaticImport(Class clazz){
        staticImportedClassSet.add(clazz);
    }

    /**
     * the imports as String list.
     * @return 
     */
    public List<String> asString() {
        final List<String> list = importedClassSet.stream().map(c -> c.getCanonicalName()).collect(Collectors.toList());
        list.addAll(staticImportedClassSet.stream().map(c -> "static " + c.getCanonicalName() + ".*").collect(Collectors.toList()));
        Collections.sort(list);
        return list;
    }

    /**
     * The imports as a set of classes.
     * @return 
     */
    public Set<Class> getImportedClassSet() {
        return Collections.unmodifiableSet(importedClassSet);
    }
    
    
}
