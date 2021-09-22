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
import org.datanucleus.state.ObjectProvider;
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
    public LifeCycleState transitionRollback(ObjectProvider op, Transaction tx)
    {
        throw new IllegalStateTransitionException(this, "rollback", op);
    }

    @Override
    public LifeCycleState transitionReadField(ObjectProvider op, boolean isLoaded)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalRead())
        {
            throw new TransactionNotReadableException(Localiser.msg("027000"), op.getInternalObjectId());
        }
        else if (!tx.isActive() && op.getClassMetaData().getIdentityType() == IdentityType.NONDURABLE)
        {
            if (!isLoaded)
            {
                // JDO2 spec 5.4.4
                throw new NucleusUserException("Not able to read fields of nondurable object when in HOLLOW state");
            }
        }
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(op, P_CLEAN);
        }
        return changeState(op, P_NONTRANS);
    }

    @Override
    public LifeCycleState transitionWriteField(ObjectProvider op)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (!tx.isActive() && !tx.getNontransactionalWrite())
        {
            throw new TransactionNotWritableException(Localiser.msg("027001"), op.getInternalObjectId());
        }
        return changeState(op, tx.isActive() ? P_DIRTY : P_NONTRANS);
    }

    @Override
    public LifeCycleState transitionRetrieve(ObjectProvider op, boolean fgOnly)
    {
        if (fgOnly)
        {
            op.loadUnloadedFieldsInFetchPlan();
        }
        else
        {
            op.loadUnloadedFields();
        }
        Transaction tx = op.getExecutionContext().getTransaction();
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(op, P_CLEAN);
        }
        else if (tx.getOptimistic())
        {
            return changeState(op, P_NONTRANS);
        }
        return super.transitionRetrieve(op, fgOnly);
    }

    @Override
    public LifeCycleState transitionRetrieve(ObjectProvider op, FetchPlan fetchPlan)
    {
        op.loadUnloadedFieldsOfClassInFetchPlan(fetchPlan);
        Transaction tx = op.getExecutionContext().getTransaction();
        if (!tx.getOptimistic() && tx.isActive())
        {
            return changeState(op, P_CLEAN);
        }
        else if (tx.getOptimistic())
        {
            return changeState(op, P_NONTRANS);
        }
        return super.transitionRetrieve(op, fetchPlan);
    }

    @Override
    public LifeCycleState transitionRefresh(ObjectProvider op)
    {
        op.clearSavedFields();

        // Refresh the FetchPlan fields and unload all others
        op.refreshFieldsInFetchPlan();
        op.unloadNonFetchPlanFields();

        // We leave in the same state to be consistent with JDO section 5.9.1
        return this;
    }

    @Override
    public LifeCycleState transitionDetach(ObjectProvider op)
    {
        return changeState(op, DETACHED_CLEAN);
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

    /**
     * Method to return a string version of this object.
     * @return The string "HOLLOW".
     */
    public String toString()
    {
        return "HOLLOW";
    }
}