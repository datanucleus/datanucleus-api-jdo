/**********************************************************************
Copyright (c) 2014 Andy Jefferson and others. All rights reserved.
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

import javax.jdo.PersistenceManager;
import javax.jdo.spi.StateInterrogation;

import org.datanucleus.enhancer.Persistable;

/**
 * Hook for providing JDOHelper support for non-JDO-bytecode-enhanced classes, for when we dump the JDO bytecode contract.
 * TODO Register this with JDOImplHelper.
 */
public class JDOStateInterrogation implements StateInterrogation
{
    public JDOStateInterrogation()
    {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#getObjectId(java.lang.Object)
     */
    @Override
    public Object getObjectId(Object pc)
    {
        return ((Persistable)pc).dnGetObjectId();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#getPersistenceManager(java.lang.Object)
     */
    @Override
    public PersistenceManager getPersistenceManager(Object pc)
    {
        return (PersistenceManager) ((Persistable)pc).dnGetExecutionContext().getOwner();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#getTransactionalObjectId(java.lang.Object)
     */
    @Override
    public Object getTransactionalObjectId(Object pc)
    {
        return ((Persistable)pc).dnGetTransactionalObjectId();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#getVersion(java.lang.Object)
     */
    @Override
    public Object getVersion(Object pc)
    {
        return ((Persistable)pc).dnGetVersion();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isDeleted(java.lang.Object)
     */
    @Override
    public Boolean isDeleted(Object pc)
    {
        return ((Persistable)pc).dnIsDeleted();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isDetached(java.lang.Object)
     */
    @Override
    public Boolean isDetached(Object pc)
    {
        return ((Persistable)pc).dnIsDetached();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isDirty(java.lang.Object)
     */
    @Override
    public Boolean isDirty(Object pc)
    {
        return ((Persistable)pc).dnIsDirty();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isNew(java.lang.Object)
     */
    @Override
    public Boolean isNew(Object pc)
    {
        return ((Persistable)pc).dnIsNew();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isPersistent(java.lang.Object)
     */
    @Override
    public Boolean isPersistent(Object pc)
    {
        return ((Persistable)pc).dnIsPersistent();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#isTransactional(java.lang.Object)
     */
    @Override
    public Boolean isTransactional(Object pc)
    {
        return ((Persistable)pc).dnIsTransactional();
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.StateInterrogation#makeDirty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean makeDirty(Object pc, String fieldName)
    {
        ((Persistable)pc).dnMakeDirty(fieldName);
        return true;
    }
}