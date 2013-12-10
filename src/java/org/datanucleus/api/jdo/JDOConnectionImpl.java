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
package org.datanucleus.api.jdo;

import javax.jdo.JDOUserException;
import javax.jdo.datastore.JDOConnection;

import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.store.NucleusConnection;
import org.datanucleus.util.Localiser;

/**
 * Implementation of a generic JDO connection for non-RDBMS datastores.
 * Takes the NucleusConnection as input, providing all behaviour and closure of underlying connections.
 */
public class JDOConnectionImpl implements JDOConnection
{
    /** Localisation utility for output messages */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.api.jdo.Localisation",
        JDOPersistenceManagerFactory.class.getClassLoader());

    /** Underlying NucleusConnection providing the connection. */
    protected NucleusConnection nucConn = null;

    /**
     * Constructor taking the underlying NucleusConnection that provides the datastore access.
     * @param nconn Underlying connection
     */
    public JDOConnectionImpl(NucleusConnection nconn)
    {
        this.nucConn = nconn;
    }

    /**
     * Method to close the connection.
     */
    public void close()
    {
        try
        {
            nucConn.close();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Accessor for the native connection for this datastore.
     * For RDBMS this would be a java.sql.Connection, or for db4o an ObjectContainer etc.
     * @return The native connection
     */
    public Object getNativeConnection()
    {
        try
        {
            return nucConn.getNativeConnection();
        }
        catch (NucleusException ne)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Throw a JDOUserException that the connection is no longer available
     * @throws JDOUserException
     */
    protected void throwExceptionNotAvailable()
    {
        throw new JDOUserException(LOCALISER.msg("046001"));
    }

    /**
     * Convenience method that throws a JDOUserException that the specified method name is not supported.
     * @param methodName Name of the method
     */
    protected void throwExceptionUnsupportedOperation(String methodName)
    {
        throw new JDOUserException(LOCALISER.msg("046000", methodName));
    }
}