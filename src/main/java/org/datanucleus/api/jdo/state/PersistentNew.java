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
 * Class representing the life cycle state of PersistentNew.
 */
class PersistentNew extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected PersistentNew()
    {
		isPersistent = true;    	
        isDirty = true;
        isNew = true;
        isDeleted = false;
        isTransactional = true;

        stateType = P_NEW;
    }

    @Override
    public LifeCycleState transitionDeletePersistent(ObjectProvider op)
    {
        op.clearLoadedFlags();        
        return changeState(op, P_NEW_DELETED);
    }

    @Override
    public LifeCycleState transitionMakeNontransactional(ObjectProvider op)
    {
        throw new NucleusUserException(Localiser.msg("027013"),op.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionMakeTransient(ObjectProvider op, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        if (detachAllOnCommit)
        {
            return changeState(op, TRANSIENT);
        }
        throw new NucleusUserException(Localiser.msg("027014"),op.getInternalObjectId());
    }

    @Override
    public LifeCycleState transitionCommit(ObjectProvider op, Transaction tx)
    {
        op.clearSavedFields();

        if (tx.getRetainValues())
        {
            return changeState(op, P_NONTRANS);
        }

        op.clearNonPrimaryKeyFields();
        //op.restoreFields();
        return changeState(op, HOLLOW);
    }

    @Override
    public LifeCycleState transitionRollback(ObjectProvider op, Transaction tx)
    {
        if (tx.getRestoreValues())
        {
            op.restoreFields();
        }

        return changeState(op, TRANSIENT);
    }

    @Override
    public LifeCycleState transitionDetach(ObjectProvider op)
    {
        return changeState(op, DETACHED_CLEAN);
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_NEW".
     **/
    public String toString()
    {
        return "P_NEW";
    }
}