/**********************************************************************
Copyright (c) 2016 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus.api.jdo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Extension annotation providing for marking a relation as "shared" and adding a distinguisher column.
 */
@Target({ElementType.FIELD, ElementType.METHOD}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface SharedRelation
{
    /**
     * The name of the distinguisher column.
     * @return The column name
     */
    String column();

    /**
     * Whether the distinguisher column defaults to being part of the PK (false unless specified).
     * @return Whether part of the PK
     */
    boolean primaryKey() default false;

    /**
     * The value to be stored in the distinguisher column for this relation.
     * @return distinguishing value
     */
    String value();
}