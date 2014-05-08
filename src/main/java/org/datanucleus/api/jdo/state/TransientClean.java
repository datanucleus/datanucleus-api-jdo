/**********************************************************************
Copyright (c) 2004 Erik Bengtson and others. All rights reserved.
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

import org.datanucleus.Transaction;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;

/**
 * This class represents TransientClean state specific state transitions as requested by ObjectProvider. 
 * This state is the result of a call to makeTransactional on a Transient instance, or commit or rollback 
 * of a TransientDirty instance.
 */
class TransientClean extends LifeCycleState
{
    TransientClean()
    {
        // these flags are set only in the constructor 
        // and shouldn't be changed afterwards
        // (cannot make them final since they are declared in superclass 
        // but their values are specific to subclasses)
        isPersistent = false;
        isTransactional = true;
        isDirty = false;
        isNew = false;
        isDeleted = false;

        stateType = T_CLEAN;
    }

    /**
     * @param op The ObjectProvider 
     * @param useFetchPlan to make transient the fields in the fetch plan
     * @return new LifeCycle state.
     * @see LifeCycleState#transitionMakeTransient(ObjectProvider op)
     */
    public LifeCycleState transitionMakeTransient(ObjectProvider op, boolean useFetchPlan, boolean detachAllOnCommit)
    {
        return this;
    }

    /**
     * @param op The ObjectProvider 
     * @see LifeCycleState#transitionMakeNontransactional(ObjectProvider op)
     */
    public LifeCycleState transitionMakeNontransactional(ObjectProvider op)
    {  
        try
        {
            return changeTransientState(op,TRANSIENT);
        }
        finally
        {
            op.disconnect();
        }
    }

    /**
     * @param op The ObjectProvider 
     * @see LifeCycleState#transitionMakePersistent(ObjectProvider op)
     */
    public LifeCycleState transitionMakePersistent(ObjectProvider op)
    {    
        op.registerTransactional();
        return changeState(op,P_NEW);
    }
    
    /**
     * @param op The ObjectProvider
     * @param isLoaded if the field was previously loaded.
     * @see LifeCycleState#transitionReadField(ObjectProvider op, boolean isLoaded)
     */
    public LifeCycleState transitionReadField(ObjectProvider op, boolean isLoaded)
    {
        return this;
    }

    /**
     * @param op The ObjectProvider
     * @see LifeCycleState#transitionWriteField(ObjectProvider op)
     */
    public LifeCycleState transitionWriteField(ObjectProvider op)
    {
        Transaction tx = op.getExecutionContext().getTransaction();
        if (tx.isActive())
        {
            op.saveFields();
            return changeTransientState(op,T_DIRTY);
        }
        else
        {
            return this;
        }
    }

    /**
     * Method to transition to commit state.
     * This is a no-op.
     * @param op ObjectProvider.
     * @param tx the Transaction been committed.
     * @return new LifeCycle state.
     */
    public LifeCycleState transitionCommit(ObjectProvider op, Transaction tx)
    {
        return this;
    }

    /**
     * @param op The ObjectProvider
     * @param tx The Transaction
     * @see LifeCycleState#transitionRollback(ObjectProvider op,Transaction tx)
     */
    public LifeCycleState transitionRollback(ObjectProvider op, Transaction tx)
    {
        return this;
    }

    /**
     * Method to return a string version of this object.
     * @return The string "T_CLEAN".
     */
    public String toString()
    {
        return "T_CLEAN";
    }
}