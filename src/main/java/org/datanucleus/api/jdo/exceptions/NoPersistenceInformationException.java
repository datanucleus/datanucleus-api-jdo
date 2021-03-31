/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved. 
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
package org.datanucleus.api.jdo.exceptions;

import javax.jdo.JDOUserException;

/**
 * Exception thrown when a class is required to have persistence information (metadata/annotations) yet none can be found.
 */
public class NoPersistenceInformationException extends JDOUserException
{
    private static final long serialVersionUID = 8218822469557539549L;

    /**
     * Constructs an exception for the specified class with the supplied nested exception.
     * @param msg The message
     * @param nested the nested exception(s).
     */
    public NoPersistenceInformationException(String msg, Exception nested)
    {
        super(msg, nested);
    }
}