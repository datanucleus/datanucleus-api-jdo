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
 * Expression representing the ordering using an expression and a direction.
 * 
 * @param <T> Java type of the expression being represented here
 */
public interface OrderExpression<T>
{
    public enum OrderDirection
    {
        ASC,
        DESC
    }

    /**
     * Accessor for the direction of the ordering with this expression.
     * @return The direction
     */
    OrderDirection getDirection();

    /**
     * Accessor for the expression being used for ordering.
     * @return Ordering expression
     */
    Expression<T> getExpression();
}