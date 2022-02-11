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
import org.datanucleus.state.DNStateManager;
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
    public LifeCycleState transitionDeletePersistent(DNStateManager sm)
    {
        sm.clearLoadedFlags();
        return changeState(sm, P_DELETED);
    }

    @Override
    public LifeCycleState transitionMakeTransactional(DNStateManager sm, boolean refreshFields)
    {
        if (refreshFields)
        {
            sm.refreshLoadedFields();
        }
        return changeState(sm, P_CLEAN);
    }

    @Override
    public LifeCycleState transitionMakeTransient(DNStateManager sm, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        if (useFetchPlan)
        {
            sm.loadUnloadedFieldsInFetchPlan();
        }
        return changeState(sm, TRANSIENT);
    }

    @Override
    public LifeCycleState transitionCommit(DNStateManager sm, Transaction tx)
    {
        throw new IllegalStateTransitionException(this, "commit", sm);
    }

    @Override
    public LifeCycleState transitionRollback(DNStateManager sm, Transaction tx)
    {
        throw new IllegalStateTransitionException(this, "rollback", sm);
    }

    @Override
    public LifeCycleState transitionRefresh(DNStateManager sm)
    {
        // Refresh the FetchPlan fields and unload all others
        sm.refreshFieldsInFetchPlan();
        sm.unloadNonFetchPlanFields();

        return this;
    }

    @Override
    public LifeCycleState transitionEvict(DNStateManager sm)
    {
        sm.clearNonPrimaryKeyFields();
        sm.clearSavedFields();
        return changeState(sm, HOLLOW);
    }

    @Override
    public LifeCycleState transitionReadField(DNStateManager sm, boolean isLoaded)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
		if (!tx.isActive() && !tx.getNontransactionalRead())
		{
	        throw new TransactionNotReadableException(Localiser.msg("027002"), sm.getInternalObjectId());
		}
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback.
            sm.saveFields();
            sm.refreshLoadedFields();
            return changeState(sm, P_CLEAN);
        }
        return this;
    }

    @Override
    public LifeCycleState transitionWriteField(DNStateManager sm)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalWrite())
        {
            throw new TransactionNotWritableException(Localiser.msg("027001"), sm.getInternalObjectId());
        }
        if (tx.isActive())
        {
            // Save the fields for rollback.
            sm.saveFields();
            return changeState(sm, P_DIRTY);
        }

        // Save the fields for rollback.
        sm.saveFields();
        return changeState(sm, P_NONTRANS_DIRTY);
    }

    @Override
    public LifeCycleState transitionRetrieve(DNStateManager sm, boolean fgOnly)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback - TODO Omit this if no unloaded fields
            sm.saveFields();
    		if (fgOnly)
            {
                sm.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			sm.loadUnloadedFields();
            }             
            return changeState(sm, P_CLEAN);
        }
        else if (tx.isActive() && tx.getOptimistic())
        {
            // Save the fields for rollback - TODO Omit this if no unloaded fields
            sm.saveFields();
    		if (fgOnly)
            {
                sm.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			sm.loadUnloadedFields();
            }
    		return this;
        }
        else
        {
    		if (fgOnly)
            {
                sm.loadUnloadedFieldsInFetchPlan();
            }
    		else
            {
    			sm.loadUnloadedFields();
            }
    		return this;
        }
    }

    @Override
    public LifeCycleState transitionRetrieve(DNStateManager sm, FetchPlan fetchPlan)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            // Save the fields for rollback - TODO Omit this if no unloaded fields
            sm.saveFields();
            sm.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return changeState(sm, P_CLEAN);
        }
        else if (tx.isActive() && tx.getOptimistic())
        {
            // Save the fields for rollback - TODO Omit this if no unloaded fields
            sm.saveFields();
            sm.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return this;
        }
        else
        {
            sm.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
            return this;
        }
    }

    @Override
    public LifeCycleState transitionSerialize(DNStateManager sm)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            return changeState(sm, P_CLEAN);
        }
        return this;
    }

    @Override
    public LifeCycleState transitionDetach(DNStateManager sm)
    {
        return changeState(sm, DETACHED_CLEAN);
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