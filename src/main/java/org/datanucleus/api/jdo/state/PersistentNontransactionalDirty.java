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
import org.datanucleus.state.DNStateManager;
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

    @Override
    public LifeCycleState transitionMakeTransactional(DNStateManager sm, boolean refreshFields)
    {
        return this;
    }

    @Override
    public LifeCycleState transitionCommit(DNStateManager sm, Transaction tx)
    {
        sm.clearSavedFields();

        if (tx.getRetainValues())
        {
            return changeState(sm, P_NONTRANS);
        }

        sm.clearNonPrimaryKeyFields();
        return changeState(sm, HOLLOW);
    }

    @Override
    public LifeCycleState transitionRollback(DNStateManager sm,Transaction tx)
    {
        if (tx.getRestoreValues())
        {
            sm.restoreFields();
            return changeState(sm, P_NONTRANS_DIRTY);
        }

        sm.clearNonPrimaryKeyFields();
        sm.clearSavedFields();
        return changeState(sm, HOLLOW);
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
	        throw new JDOUserException(Localiser.msg("027002"),sm.getInternalObjectId());
		}
        return this;
    }

    @Override
    public LifeCycleState transitionBegin(DNStateManager sm, org.datanucleus.transaction.Transaction tx)
    {
        sm.saveFields();
        sm.enlistInTransaction();
        return this;
    }

    @Override
    public LifeCycleState transitionWriteField(DNStateManager sm)
    {
        return this;
    }

    @Override
    public LifeCycleState transitionDetach(DNStateManager sm)
    {
        return changeState(sm, DETACHED_CLEAN);
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