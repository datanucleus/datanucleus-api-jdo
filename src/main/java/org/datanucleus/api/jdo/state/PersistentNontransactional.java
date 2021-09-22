/**********************************************************************
Copyright (c) 2002 Kelly Grizzle and others. All rights reserved.
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
2002 Mike Martin - unknown changes
2003 Andy Jefferson - commented
2003 Erik Bengtson - optimistic transaction
2008 Andy Jefferson - fixed transitionReadField, transitionWriteField exceptions
    ...
**********************************************************************/
package org.datanucleus.api.jdo.state;

import org.datanucleus.FetchPlan;
import org.datanucleus.api.jdo.exceptions.TransactionNotReadableException;
import org.datanucleus.api.jdo.exceptions.TransactionNotWritableException;
import org.datanucleus.state.IllegalStateTransitionException;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of PersistentNontransactional.
 */
class PersistentNontransactional extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected PersistentNontransactional()
    {
		isPersistent = true;    	
        isDirty = false;
        isNew = false;
        isDeleted = false;
        isTransactional = false;

        stateType = P_NONTRANS;
    }

    @Override
    public LifeCycleState transitionDeletePersistent(ObjectProvider op)
    {
        op.clearLoadedFlags();
        return changeState(op, P_DELETED);
    }

    @Override
    public LifeCycleState transitionMakeTransactional(ObjectProvider op, boolean refreshFields)
    {
        if (refreshFields)
        {
            op.refreshLoadedFields();
        }
        return changeState(op, P_CLEAN);
    }

    @Override
    public LifeCycleState transitionMakeTransient(ObjectProvider op, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        if (useFetchPlan)
        {
            op.loadUnloadedFieldsInFetchPlan();
        }
        return changeState(op, TRANSIENT);
    }

    @Override
    public LifeCycleState transitionCommit(ObjectProvider op, Transaction tx)
    {
        throw new IllegalStateTransitionException(this, "commit", op);
    }

    @Override
    public LifeCycleState transitionRollback(ObjectProvider op,Transaction tx)
    {
        throw new IllegalStateTransitionException(this, "rollback", op);
    }

    @Override
    public LifeCycleState transitionRefresh(ObjectProvider op)
    {
        // Refresh the FetchPlan fields and unload all others
        op.refreshFieldsInFetchPlan();
        op.unloadNonFetchPlanFields();

        return this;
    }

    @Override
    public LifeCycleState transitionEvict(ObjectProvider op)
    {
        op.clearNonPrimaryKeyFields();
        op.clearSavedFields();
        return changeState(op, HOLLOW);
    }

    @Override
    public LifeCycleState transitionReadField(ObjectProvider op, boolean isLoaded)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
		if (!tx.isActive() && !tx.getNontransactionalRead())
		{
	        throw new TransactionNotReadableException(Localiser.msg("027002"), op.getInternalObjectId());
		}
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback.
            op.saveFields();
            op.refreshLoadedFields();
            return changeState(op, P_CLEAN);
        }
        return this;
    }

    @Override
    public LifeCycleState transitionWriteField(ObjectProvider op)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalWrite())
        {
            throw new TransactionNotWritableException(Localiser.msg("027001"), op.getInternalObjectId());
        }
        if (tx.isActive())
        {
            // Save the fields for rollback.
            op.saveFields();
            return changeState(op, P_DIRTY);
        }

        // Save the fields for rollback.
        op.saveFields();
        return changeState(op, P_NONTRANS_DIRTY);
    }

    @Override
    public LifeCycleState transitionRetrieve(ObjectProvider op, boolean fgOnly)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback.
            op.saveFields();
    		if (fgOnly)
            {
                op.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			op.loadUnloadedFields();
            }             
            return changeState(op, P_CLEAN);
        }
        else if (tx.isActive() && tx.getOptimistic())
        {
            // Save the fields for rollback.
            op.saveFields(); //TODO this is wrong... saving all the time, retrieve is asked... side effects besides performance?
    		if (fgOnly)
            {
                op.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			op.loadUnloadedFields();
            }
    		return this;
        }
        else
        {
    		if (fgOnly)
            {
                op.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			op.loadUnloadedFields();
            }
    		return this;
        }
    }

    @Override
    public LifeCycleState transitionRetrieve(ObjectProvider op, FetchPlan fetchPlan)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback.
            op.saveFields();
            op.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return changeState(op, P_CLEAN);
        }
        else if (tx.isActive() && tx.getOptimistic())
        {
            // Save the fields for rollback.
            op.saveFields(); //TODO this is wrong... saving all the time, retrieve is asked... side effects besides performance?
            op.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return this;
        }
        else
        {
            op.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return this;
        }
    }

    @Override
    public LifeCycleState transitionSerialize(ObjectProvider op)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            return changeState(op, P_CLEAN);
        }
        return this;
    }

    @Override
    public LifeCycleState transitionDetach(ObjectProvider op)
    {
        return changeState(op, DETACHED_CLEAN);
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_NONTRANS".
     **/
    public String toString()
    {
        return "P_NONTRANS";
    }
}