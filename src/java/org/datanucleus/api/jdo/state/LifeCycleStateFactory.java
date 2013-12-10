/**********************************************************************
Copyright (c) 2006 Erik Bengtson and others. All rights reserved. 
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

import org.datanucleus.state.LifeCycleState;

/**
 * Factory for life cycle states.
 */
public abstract class LifeCycleStateFactory
{
    private static LifeCycleState states[];

    static
    {
        states = new LifeCycleState[LifeCycleState.TOTAL];

        states[LifeCycleState.HOLLOW] = new Hollow();
        states[LifeCycleState.P_CLEAN] = new PersistentClean();
        states[LifeCycleState.P_DIRTY] = new PersistentDirty();
        states[LifeCycleState.P_NEW] = new PersistentNew();
        states[LifeCycleState.P_NEW_DELETED] = new PersistentNewDeleted();
        states[LifeCycleState.P_DELETED] = new PersistentDeleted();
        states[LifeCycleState.P_NONTRANS] = new PersistentNontransactional();
        states[LifeCycleState.T_CLEAN] = new TransientClean();
        states[LifeCycleState.T_DIRTY] = new TransientDirty();
        states[LifeCycleState.P_NONTRANS_DIRTY] = new PersistentNontransactionalDirty();
        states[LifeCycleState.DETACHED_CLEAN] = new DetachedClean();
        states[LifeCycleState.DETACHED_DIRTY] = new DetachedDirty();
        states[LifeCycleState.TRANSIENT] = null;
    }

    /**
     * Returns the LifeCycleState for the state constant.
     * @param stateType the type as integer
     * @return the type as LifeCycleState object
     */
    public static final LifeCycleState getLifeCycleState(int stateType)
    {
        return states[stateType];
    }
}