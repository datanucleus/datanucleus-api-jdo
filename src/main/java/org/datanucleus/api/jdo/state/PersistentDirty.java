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

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.transaction.Transaction;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of PersistentDirty.
 */
class PersistentDirty extends LifeCycleState
{
    protected PersistentDirty()
    {
        isPersistent = true;        
        isDirty = true;
        isNew = false;
        isDeleted = false;
        isTransactional = true;

        stateType =  P_DIRTY;
    }

    @Override
    public LifeCycleState transitionDeletePersistent(ObjectProvider sm)
    {
        sm.clearLoadedFlags();        
        return changeState(sm, P_DELETED);
    }

    @Override
    public LifeCycleState transitionMakeNontransactional(ObjectProvider sm)
    {
        throw new NucleusUserException(Localiser.msg("027011"), sm.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionMakeTransient(ObjectProvider sm, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        if (detachAllOnCommit)
        {
            return changeState(sm, TRANSIENT);
        }
        throw new NucleusUserException(Localiser.msg("027012"),sm.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionCommit(ObjectProvider sm, Transaction tx)
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
    public LifeCycleState transitionRollback(ObjectProvider sm, Transaction tx)
    {
        if (tx.getRestoreValues())
        {
            sm.restoreFields();
            return changeState(sm, P_NONTRANS);
        }

        sm.clearNonPrimaryKeyFields();
        sm.clearSavedFields();
        return changeState(sm, HOLLOW);
    }

    @Override
    public LifeCycleState transitionRefresh(ObjectProvider sm)
    {
        sm.clearSavedFields();

        // Refresh the FetchPlan fields and unload all others
        sm.refreshFieldsInFetchPlan();
        sm.unloadNonFetchPlanFields();

        Transaction tx = sm.getExecutionContext().getTransaction();
        if (tx.isActive() && !tx.getOptimistic())
        {
            return changeState(sm,P_CLEAN);
        }
        return changeState(sm,P_NONTRANS);      
    }

    @Override
    public LifeCycleState transitionDetach(ObjectProvider sm)
    {
        return changeState(sm, DETACHED_CLEAN);
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_DIRTY".
     **/
    public String toString()
    {
        return "P_DIRTY";
    }
}