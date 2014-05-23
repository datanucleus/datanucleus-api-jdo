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
    ...
**********************************************************************/
package org.datanucleus.api.jdo.exceptions;

import javax.jdo.JDOUserException;

import org.datanucleus.util.Localiser;

/**
 * An <tt>TransactionActiveException</tt> is thrown if a transaction is already
 * active and an operation is performed that requires that a transaction not be
 * active (such as beginning a transaction).
 */
public class TransactionActiveException extends JDOUserException
{
    public TransactionActiveException(Object failedObject)
    {
        super(Localiser.msg("015032"),failedObject);
    }
}
