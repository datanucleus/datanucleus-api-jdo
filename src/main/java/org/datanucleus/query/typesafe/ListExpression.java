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

import java.util.List;

/**
 * Representation of a List in a query.
 * 
 * @param <T> Java type being represented here
 * @param <E> Element type of the List being represented here
 */
public interface ListExpression<T extends List<E>, E> extends CollectionExpression<T, E>
{
    /**
     * Method returning the element at this position in the List.
     * @param posExpr The position expression
     * @return The element at this position in the List
     */
    Expression get(NumericExpression<Integer> posExpr);

    /**
     * Method returning the element at this position in the List.
     * @param pos The position
     * @return The element at this position in the List
     */
    Expression get(int pos);
}