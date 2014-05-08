/**********************************************************************
Copyright (c) 2002 Mike Martin (TJDO) and others. All rights reserved.
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
2002 Kelly Grizzle (TJDO)
2003 Andy Jefferson - commented and localised.
2004 Andy Jefferson - changed to take the class name.
    ...
**********************************************************************/
package org.datanucleus.api.jdo.exceptions;

import javax.jdo.JDOUserException;

/**
 * A <tt>ClassNotPersistenceCapableException</tt> is thrown if an attempt is
 * made to persist an object whose class is not persistence-capable. This may
 * be because the class has not been appropriately enhanced to be made persistence-capable.
 */
public class ClassNotPersistenceCapableException extends JDOUserException
{
    /**
     * Constructs a class-not-persistence-capable exception with the specified detail message.
     * @param msg The message
     */
    public ClassNotPersistenceCapableException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a class-not-persistence-capable exception with the specified
     * detail message and nested exception.
     * @param msg The message
     * @param nested the nested exception(s).
     */
    public ClassNotPersistenceCapableException(String msg, Exception nested)
    {
        super(msg, nested);
    }
}