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
 * Extension annotation allowing for specifying a class as multitenant, meaning that it will have a surrogate column in its table storing the tenant id.
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiTenant
{
    /**
     * Name of the multitenancy column in the table for this class.
     * @return The multitenancy column name
     */
    String column() default "TENANT_ID";

    /**
     * Length of the multitenancy column for this class.
     * @return Length of the multitenancy column.
     */
    int columnLength() default -1;

    /**
     * Whether you want to disable multitenancy for this class (when enabled globally).
     * @return Whether multitenancy is disabled
     */
    boolean disable() default false;

    /**
     * JDBC Type of the multitenancy column in the table for this class.
     * @return The multitenancy column JDBC Type
     */
    String jdbcType() default "";
}