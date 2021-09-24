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
2003 Andy Jefferson - added localiser
    ...
**********************************************************************/
package org.datanucleus.api.jdo.state;

import javax.jdo.JDOUserException;

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.DNStateManager;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of PersistentNewDeleted.
 */
class PersistentNewDeleted extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected PersistentNewDeleted()
    {
		isPersistent = true;    	
        isDirty = true;
        isNew = true;
        isDeleted = true;
        isTransactional = true;

        stateType = P_NEW_DELETED;
    }

    @Override
    public LifeCycleState transitionMakeNontransactional(DNStateManager sm)
    {
        throw new NucleusUserException(Localiser.msg("027003"), sm.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionMakeTransient(DNStateManager sm, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        throw new NucleusUserException(Localiser.msg("027004"),sm.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionCommit(DNStateManager sm, Transaction tx)
    {
        if (!tx.getRetainValues())
        {
            sm.clearFields();
        }
        return changeState(sm, TRANSIENT);
    }

    @Override
    public LifeCycleState transitionRollback(DNStateManager sm, Transaction tx)
    {
        if (tx.getRestoreValues())
        {
            sm.restoreFields();
        }
        return changeState(sm, TRANSIENT);
    }

    @Override
    public LifeCycleState transitionReadField(DNStateManager sm, boolean isLoaded)
    {
        throw new JDOUserException(Localiser.msg("027005"), sm.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionWriteField(DNStateManager sm)
    {
        throw new JDOUserException(Localiser.msg("027006"), sm.getInternalObjectId());
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_NEW_DELETED".
     **/
    public String toString()
    {
        return "P_NEW_DELETED";
    }
}
