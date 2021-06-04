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
    ...
**********************************************************************/
package org.datanucleus.api.jdo.state;

import javax.jdo.JDOUserException;

import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of PersistentNontransactionalDirty
 */
class PersistentNontransactionalDirty extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected PersistentNontransactionalDirty()
    {
		isPersistent = true;    	
        isDirty = true;
        isNew = false;
        isDeleted = false;
        isTransactional = false;

        stateType = P_NONTRANS_DIRTY;
    }


    /**
     * Method to transition to transactional.
     * @param op ObjectProvider.
     * @param refreshFields Whether to refresh loaded fields
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionMakeTransactional(ObjectProvider op, boolean refreshFields)
    {
        return this;
    }

    /**
     * Method to transition to commit state.
     * @param op ObjectProvider.
     * @param tx the Transaction been committed.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionCommit(ObjectProvider op, Transaction tx)
    {
        op.clearSavedFields();

        if (tx.getRetainValues())
        {
            return changeState(op, P_NONTRANS);
        }

        op.clearNonPrimaryKeyFields();
        return changeState(op, HOLLOW);
    }

    /**
     * Method to transition to rollback state.
     * @param op ObjectProvider.
     * @param tx The transaction
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionRollback(ObjectProvider op,Transaction tx)
    {
        if (tx.getRestoreValues())
        {
            op.restoreFields();
            return changeState(op, P_NONTRANS_DIRTY);
        }

        op.clearNonPrimaryKeyFields();
        op.clearSavedFields();
        return changeState(op, HOLLOW);
    }
    
    /**
     * Method to transition to evict state.
     * @param op ObjectProvider.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionEvict(ObjectProvider op)
    {
        op.clearNonPrimaryKeyFields();
        op.clearSavedFields();
        return changeState(op, HOLLOW);
    }

    /**
     * Method to transition to read-field state.
     * @param op ObjectProvider.
     * @param isLoaded if the field was previously loaded.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionReadField(ObjectProvider op, boolean isLoaded)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
		if (!tx.isActive() && !tx.getNontransactionalRead())
		{
	        throw new JDOUserException(Localiser.msg("027002"),op.getInternalObjectId());
		}
        return this;
    }

    /**
     * Method to transition to transaction begin state.
     * @param op ObjectProvider.
     * @param tx Transaction.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionBegin(ObjectProvider op, org.datanucleus.transaction.Transaction tx)
    {
        op.saveFields();
        op.enlistInTransaction();
        return this;
    }
    
    /**
     * Method to transition to write-field state.
     * @param op ObjectProvider.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionWriteField(ObjectProvider op)
    {
        return this;
    }

    /**
     * Method to transition to detached-clean.
     * @param op ObjectProvider.
     * @return new LifeCycle state.
     */
    public LifeCycleState transitionDetach(ObjectProvider op)
    {
        return changeState(op, DETACHED_CLEAN);
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_NONTRANS_DIRTY".
     **/
    public String toString()
    {
        return "P_NONTRANS_DIRTY";
    }
}