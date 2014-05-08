/**********************************************************************
Copyright (c) 2010 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.query.typesafe;

/**
 * Expression representing a persistable object in a query (e.g alias.persistableField).
 *
 * @param <T> (Persistable) Java type being represented here
 */
public interface PersistableExpression<T> extends Expression<T>
{
    /**
     * Method to return an expression for the identity of this persistable object.
     * TODO Only applicable to JDOQL so move to language specific interface
     * @return The identity expression
     */
    Expression jdoObjectId();

    /**
     * Method to return an expression for the version of this persistable object.
     * TODO Only applicable to JDOQL so move to language specific interface
     * @return The version expression
     */
    Expression jdoVersion();
}