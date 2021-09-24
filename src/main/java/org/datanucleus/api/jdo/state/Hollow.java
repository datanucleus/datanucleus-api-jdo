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
2004 Andy Jefferson - added refresh() handling
    ...
**********************************************************************/
package org.datanucleus.api.jdo.state;

import org.datanucleus.FetchPlan;
import org.datanucleus.api.jdo.exceptions.TransactionNotReadableException;
import org.datanucleus.api.jdo.exceptions.TransactionNotWritableException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.state.IllegalStateTransitionException;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.DNStateManager;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of Hollow.
 */
class Hollow extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected Hollow()
    {
        isPersistent = true;
        isDirty = false;
        isNew = false;
        isDeleted = false;
        isTransactional = false;

        stateType = HOLLOW;
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
    public LifeCycleState transitionReadField(DNStateManager sm, boolean isLoaded)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalRead())
        {
            throw new TransactionNotReadableException(Localiser.msg("027000"), sm.getInternalObjectId());
        }
        else if (!tx.isActive() && sm.getClassMetaData().getIdentityType() == IdentityType.NONDURABLE)
        {
            if (!isLoaded)
            {
                // JDO2 spec 5.4.4
                throw new NucleusUserException("Not able to read fields of nondurable object when in HOLLOW state");
            }
        }
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(sm, P_CLEAN);
        }
        return changeState(sm, P_NONTRANS);
    }

    @Override
    public LifeCycleState transitionWriteField(DNStateManager sm)
    {
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalWrite())
        {
            throw new TransactionNotWritableException(Localiser.msg("027001"), sm.getInternalObjectId());
        }
        return changeState(sm, tx.isActive() ? P_DIRTY : P_NONTRANS);
    }

    @Override
    public LifeCycleState transitionRetrieve(DNStateManager sm, boolean fgOnly)
    {
        if (fgOnly)
        {
            sm.loadUnloadedFieldsInFetchPlan();
        }
        else
        {
            sm.loadUnloadedFields();
        }
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(sm, P_CLEAN);
        }
        else if (tx.getOptimistic())
        {
            return changeState(sm, P_NONTRANS);
        }
        return super.transitionRetrieve(sm, fgOnly);
    }

    @Override
    public LifeCycleState transitionRetrieve(DNStateManager sm, FetchPlan fetchPlan)
    {
        sm.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
        Transaction tx = sm.getExecutionContext().getTransaction();
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(sm, P_CLEAN);
        }
        else if (tx.getOptimistic())
        {
            return changeState(sm, P_NONTRANS);
        }
        return super.transitionRetrieve(sm, fetchPlan);
    }

    @Override
    public LifeCycleState transitionRefresh(DNStateManager sm)
    {
        sm.clearSavedFields();

        // Refresh the FetchPlan fields and unload all others
        sm.refreshFieldsInFetchPlan();
        sm.unloadNonFetchPlanFields();

        // We leave in the same state to be consistent with JDO section 5.9.1
        return this;
    }

    @Override
    public LifeCycleState transitionDetach(DNStateManager sm)
    {
        return changeState(sm, DETACHED_CLEAN);
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

    /**
     * Method to return a string version of this object.
     * @return The string "HOLLOW".
     */
    public String toString()
    {
        return "HOLLOW";
    }
}