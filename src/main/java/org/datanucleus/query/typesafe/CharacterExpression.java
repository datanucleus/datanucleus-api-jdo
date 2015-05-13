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
 * Representation of a character expression.
 */
public interface CharacterExpression extends ComparableExpression<Character>
{
    /**
     * Method to return a CharacterExpression representing this character expression in lower case.
     * @return The lower case expression
     */
    CharacterExpression toLowerCase();

    /**
     * Method to return a CharacterExpression representing this character expression in upper case.
     * @return The upper case expression
     */
    CharacterExpression toUpperCase();
}