/**********************************************************************
Copyright (c) 2007 Erik Bengtson and others. All rights reserved.
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
2007 Andy Jefferson - implemented checks to pass JDO TCK
2007 Andy Jefferson - reinstate optimistic exception handling
2007 Andy Jefferson - added setOption methods
    ...
**********************************************************************/
package org.datanucleus.api.jdo;

import java.util.Map;

import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUnsupportedOptionException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.transaction.Synchronization;

import org.datanucleus.TransactionEventListener;
import org.datanucleus.api.jdo.exceptions.TransactionActiveException;
import org.datanucleus.api.jdo.exceptions.TransactionCommitingException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.transaction.TransactionUtils;

/**
 * Wrapper for the transaction for use by JDO.
 */
public class JDOTransaction implements Transaction
{
    /** The underlying transaction */
    org.datanucleus.Transaction tx;

    /** JDO PersistenceManager. */
    JDOPersistenceManager pm;

    /**
     * Constructor
     * @param pm The JDO PersistenceManager
     * @param tx The real transaction
     */
    public JDOTransaction(JDOPersistenceManager pm, org.datanucleus.Transaction tx)
    {
        this.tx = tx;
        this.pm = pm;
    }

    /**
     * Accessor for the JDO PersistenceManager
     * @return The JDO PM
     */
    public JDOPersistenceManager getPersistenceManager()
    {
        return pm;
    }

    /**
     * Accessor for whether the transaction is active
     * @return Whether it is active
     */
    public boolean isActive()
    {
        return tx.isActive();
    }

    /**
     * Method to start the transaction.
     */
    public void begin()
    {
        if (pm.isClosed())
        {
            // TODO Should it be an error to begin a tx when the pm is closed?
        }

        internalBegin();
    }

    /**
     * Called by PMF under required synchronization
     */
    protected void internalBegin()
    {
        tx.begin();
    }

    /**
     * Method to commit the transaction.
     */
    public void commit()
    {
        try
        {
            tx.commit();
        }
        catch (NucleusException ne)
        {
            if (ne.getNestedExceptions() != null)
            {
                // Single wrapper exception with multiple nested exceptions of the failed object(s)
                if (ne.getNestedExceptions()[0] instanceof NucleusOptimisticException)
                {
                    // Exception with nested optimistic exception
                    if (ne.getNestedExceptions().length > 1)
                    {
                        // Exception with directly nested optimistic exception(s)
                        int numNested = ne.getNestedExceptions().length;
                        JDOOptimisticVerificationException[] jdoNested = new JDOOptimisticVerificationException[numNested];
                        for (int i=0;i<numNested;i++)
                        {
                            NucleusException nested = (NucleusOptimisticException)ne.getNestedExceptions()[i];
                            jdoNested[i] = (JDOOptimisticVerificationException) NucleusJDOHelper.getJDOExceptionForNucleusException(nested);
                        }
                        throw new JDOOptimisticVerificationException(ne.getMessage(), jdoNested);
                    }

                    // Exception with nested wrapper optimistic exception, with subnested optimistic exception(s)
                    NucleusException ex;
                    if (ne.getNestedExceptions()[0] instanceof NucleusException)
                    {
                        ex = (NucleusException)ne.getNestedExceptions()[0];
                    }
                    else
                    {
                        ex = new NucleusException(ne.getNestedExceptions()[0].getMessage(),ne.getNestedExceptions()[0]);                        
                    }

                    // Optimistic exceptions - return a single JDOOptimisticVerificationException
                    // with all individual exceptions nested
                    Throwable[] nested = ex.getNestedExceptions();
                    JDOOptimisticVerificationException[] jdoNested = new JDOOptimisticVerificationException[nested.length];
                    for (int i=0;i<nested.length;i++)
                    {
                        NucleusException nestedEx;
                        if (nested[i] instanceof NucleusException)
                        {
                            nestedEx = (NucleusException)nested[i];
                        }
                        else
                        {
                            nestedEx = new NucleusException(nested[i].getMessage(),nested[i]);                        
                        }
                        jdoNested[i] = (JDOOptimisticVerificationException)NucleusJDOHelper.getJDOExceptionForNucleusException(nestedEx);
                    }
                    throw new JDOOptimisticVerificationException(ne.getMessage(), jdoNested);
                }

                NucleusException ex;
                if (ne.getNestedExceptions()[0] instanceof NucleusException)
                {
                    ex = (NucleusException)ne.getNestedExceptions()[0];
                }
                else
                {
                    ex = new NucleusException(ne.getNestedExceptions()[0].getMessage(),ne.getNestedExceptions()[0]);                        
                }
                throw NucleusJDOHelper.getJDOExceptionForNucleusException(ex);
            }

            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

    /**
     * Method to rollback the transaction
     */
    public void rollback()
    {
        try
        {
            tx.rollback();
        }
        catch (NucleusException jpe)
        {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(jpe);
        }
    }

    /**
     * Accessor for nontransactionalRead setting
     * @return The setting for nontransactionalRead
     */
    public boolean getNontransactionalRead()
    {
        return tx.getNontransactionalRead();
    }

    /**
     * Accessor for nontransactionalWrite setting
     * @return The setting for nontransactionalWrite
     */
    public boolean getNontransactionalWrite()
    {
        return tx.getNontransactionalWrite();
    }

    /**
     * Accessor for nontransactionalWrite setting
     * @return The setting for nontransactionalWrite
     */
    public boolean getNontransactionalWriteAutoCommit()
    {
        return tx.getNontransactionalWriteAutoCommit();
    }

    /**
     * Accessor for optimistic setting
     * @return The setting for optimistic
     */
    public boolean getOptimistic()
    {
        return tx.getOptimistic();
    }

    /**
     * Accessor for restoreValues setting
     * @return The setting for restoreValues
     */
    public boolean getRestoreValues()
    {
        return tx.getRestoreValues();
    }

    /**
     * Accessor for retainValues setting
     * @return The setting for retainValues
     */
    public boolean getRetainValues()
    {
        return tx.getRetainValues();
    }

    /**
     * Accessor for whether to allow rollback only
     * @return Whether to allow rollback only
     */
    public boolean getRollbackOnly()
    {
        return tx.getRollbackOnly();
    }

    /**
     * Accessor for the synchronization (if any)
     * @return The synchronization
     */
    public Synchronization getSynchronization()
    {
        return tx.getSynchronization();
    }

    /**
     * Mutator for the nontransactionalRead setting.
     * @param flag Whether to allow nontransactional read
     */
    public void setNontransactionalRead(boolean flag)
    {
        assertNotCommitting();
        tx.setNontransactionalRead(flag);
    }

    /**
     * Mutator for the nontransactionalWrite setting.
     * @param flag Whether to allow nontransactional write
     */
    public void setNontransactionalWrite(boolean flag)
    {
        assertNotCommitting();
        tx.setNontransactionalWrite(flag);
    }

    /**
     * Mutator for the nontransactionalWrite auto-commit setting.
     * @param flag Whether to auto-commit any non-tx writes
     */
    public void setNontransactionalWriteAutoCommit(boolean flag)
    {
        assertNotCommitting();
        tx.setNontransactionalWriteAutoCommit(flag);
    }

    /**
     * Mutator for the optimistic setting
     * @param opt Whether to use optimistic transactions
     */
    public void setOptimistic(boolean opt)
    {
        assertNotInUse();
        assertNotCommitting();
        tx.setOptimistic(opt);
    }

    /**
     * Mutator for the restore values setting
     * @param restore Whether to restore values
     */
    public void setRestoreValues(boolean restore)
    {
        assertNotInUse();
        assertNotCommitting();
        tx.setRestoreValues(restore);
    }

    /**
     * Mutator for the retain values setting
     * @param retain Whether to retain values after commit
     */
    public void setRetainValues(boolean retain)
    {
        assertNotCommitting();
        tx.setRetainValues(retain);
    }

    /**
     * Mutator for the rollback-only setting
     */
    public void setRollbackOnly()
    {
        if (tx.isActive())
        {
            // Only apply to active transactions
            tx.setRollbackOnly();
        }
    }

    /**
     * Mutator for the Synchronisation
     * @param synch The Synchronisation
     */
    public void setSynchronization(Synchronization synch)
    {
        tx.setSynchronization(synch);
    }

    /**
     * Mutator for the isolation level.
     * @param level The level
     * @throws JDOUserException if the required level is not supported.
     */
    public void setIsolationLevel(String level)
    {
        assertNotCommitting();
        if (tx.isActive() && !tx.getOptimistic())
        {
            throw new JDOUnsupportedOptionException("Cannot change the transaction isolation level while a datastore transaction is active");
        }

        PersistenceManagerFactory pmf = pm.getPersistenceManagerFactory();
        if (!pmf.supportedOptions().contains("javax.jdo.option.TransactionIsolationLevel." + level))
        {
            throw new JDOUnsupportedOptionException("Isolation level \"" + level + "\" not supported by this datastore");
        }

        int isolationLevel = TransactionUtils.getTransactionIsolationLevelForName(level);
        tx.setOption(org.datanucleus.Transaction.TRANSACTION_ISOLATION_OPTION, isolationLevel);
    }

    /**
     * Accessor for the current isolation level.
     * @return The isolation level.
     */
    public String getIsolationLevel()
    {
        Map<String, Object> txOptions = tx.getOptions();
        Object value = (txOptions != null ? txOptions.get(org.datanucleus.Transaction.TRANSACTION_ISOLATION_OPTION) : null);
        if (value != null)
        {
            return TransactionUtils.getNameForTransactionIsolationLevel(((Integer)value).intValue());
        }
        return null;
    }

    /**
     * Method to mark the current point as a savepoint with the provided name.
     * @param name Name of the savepoint.
     * @throws UnsupportedOperationException if the underlying datastore doesn't support savepoints
     * @throws IllegalStateException if no name is provided
     */
    public void setSavepoint(String name)
    {
        if (name == null)
        {
            throw new IllegalStateException("No savepoint name provided so cannot set savepoint");
        }
        if (tx.isActive())
        {
            tx.setSavepoint(name);
        }
        else
        {
            throw new IllegalStateException("No active transaction so cannot set savepoint");
        }
    }

    /**
     * Method to mark the current point as a savepoint with the provided name.
     * @param name Name of the savepoint.
     * @throws UnsupportedOperationException if the underlying datastore doesn't support savepoints
     * @throws IllegalStateException if no name is provided, or the name doesn't correspond to a known savepoint
     */
    public void releaseSavepoint(String name)
    {
        if (name == null)
        {
            throw new IllegalStateException("No savepoint name provided so cannot release savepoint");
        }
        if (tx.isActive())
        {
            tx.releaseSavepoint(name);
        }
        else
        {
            throw new IllegalStateException("No active transaction so cannot release a savepoint");
        }
    }

    /**
     * Method to mark the current point as a savepoint with the provided name.
     * @param name Name of the savepoint.
     * @throws UnsupportedOperationException if the underlying datastore doesn't support savepoints
     * @throws IllegalStateException if no name is provided, or the name doesn't correspond to a known savepoint
     */
    public void rollbackToSavepoint(String name)
    {
        if (name == null)
        {
            throw new IllegalStateException("No savepoint name provided so cannot rollback to savepoint");
        }
        if (tx.isActive())
        {
            tx.rollbackToSavepoint(name);
        }
        else
        {
            throw new IllegalStateException("No active transaction so cannot rollback to savepoint");
        }
    }

    /**
     * Throw an Exception if the underlying transaction is currently committing.
     */
    protected void assertNotCommitting()
    {
        if (tx.isCommitting())
        {
            throw new TransactionCommitingException(this);
        }
    }

    /**
     * Asserts that the transaction is not in use.
     */
    protected void assertNotInUse()
    {
        if (tx.isActive())
        {
            throw new TransactionActiveException(this);
        }
    }

    /**
     * Accessor for whether to serialise any read objects in this transaction.
     * @return The setting for whether to serialise any read objects
     */
    public Boolean getSerializeRead()
    {
        return tx.getSerializeRead();
    }

    /**
     * Mutator for whether to serialise any read objects.
     * @param serialize Whether to serialise any read objects in this transaction
     */
    public void setSerializeRead(Boolean serialize)
    {
        assertNotCommitting();
        tx.setSerializeRead(serialize);
    }

    /**
     * Convenience accessor for setting a transaction option.
     * @param option option name
     * @param value The value
     */
    public void setOption(String option, int value)
    {
        tx.setOption(option, value);
    }

    /**
     * Convenience accessor for setting a transaction option.
     * @param option option name
     * @param value The value
     */
    public void setOption(String option, boolean value)
    {
        tx.setOption(option, value);
    }

    /**
     * Convenience accessor for setting a transaction option.
     * @param option option name
     * @param value The value
     */
    public void setOption(String option, String value)
    {
        tx.setOption(option, value);
    }

    /**
     * Method to register a listener for transaction events.
     * @param listener The listener
     */
    public void registerEventListener(TransactionEventListener listener)
    {
        tx.bindTransactionEventListener(listener);
    }

    /**
     * Method to deregister a listener for transaction events.
     * @param listener The listener to remove
     */
    public void deregisterEventListener(TransactionEventListener listener)
    {
        tx.removeTransactionEventListener(listener);
    }
}