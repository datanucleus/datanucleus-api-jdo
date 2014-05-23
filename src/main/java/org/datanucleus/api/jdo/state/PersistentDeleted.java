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

import org.datanucleus.Transaction;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.util.Localiser;

/**
 * Class representing the life cycle state of PersistentDeleted.
 */
class PersistentDeleted extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected PersistentDeleted()
    {
		isPersistent = true;    	
        isDirty = true;
        isNew = false;
        isDeleted = true;
        isTransactional = true;

        stateType =  P_DELETED;
    }

    /**
     * Method to transition to non-transactional.
     * @param op ObjectProvider.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionMakeNontransactional(ObjectProvider op)
    {
        throw new JDOUserException(Localiser.msg("027007"),op.getInternalObjectId());
    }

    /**
     * Method to transition to transient.
     * @param op ObjectProvider.
     * @param useFetchPlan to make transient the fields in the fetch plan
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionMakeTransient(ObjectProvider op, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        throw new JDOUserException(Localiser.msg("027008"),op.getInternalObjectId());
    }

    /**
     * Method to transition to commit state.
     * @param op ObjectProvider.
     * @param tx the Transaction been committed.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionCommit(ObjectProvider op, Transaction tx)
    {
        if (!tx.getRetainValues())
        {
            op.clearFields();
        }
        return changeState(op, TRANSIENT);
    }

    /**
     * Method to transition to rollback state.
     * @param op ObjectProvider.
     * @param tx The transaction
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionRollback(ObjectProvider op, Transaction tx)
    {
        if (tx.getRetainValues())
        {
            if (tx.getRestoreValues())
            {
                op.restoreFields();
            }

            return changeState(op, P_NONTRANS);
        }
        else
        {
            op.clearNonPrimaryKeyFields();
            op.clearSavedFields();
            return changeState(op, HOLLOW);
        }

    }

    /**
     * Method to transition to read-field state.
     * @param op ObjectProvider.
     * @param isLoaded if the field was previously loaded.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionReadField(ObjectProvider op, boolean isLoaded)
    {
        throw new JDOUserException(Localiser.msg("027009"),op.getInternalObjectId());
    }

    /**
     * Method to transition to write-field state.
     * @param op ObjectProvider.
     * @return new LifeCycle state.
     **/
    public LifeCycleState transitionWriteField(ObjectProvider op)
    {
        throw new JDOUserException(Localiser.msg("027010"),op.getInternalObjectId());
    }

    /**
     * Method to return a string version of this object.
     * @return The string "P_DELETED".
     **/
    public String toString()
    {
        return "P_DELETED";
    }
}
