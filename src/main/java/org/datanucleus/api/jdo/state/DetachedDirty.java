/**********************************************************************
Copyright (c) 2005 Andy Jefferson and others. All rights reserved. 
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
import org.datanucleus.state.DNStateManager;

/**
 * Class representing the life cycle state of DetachedDirty.
 */
class DetachedDirty extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected DetachedDirty()
    {
        isPersistent = false;           
        isDirty = true;
        isNew = false;
        isDeleted = false;
        isTransactional = false;

        stateType = DETACHED_DIRTY;
    }

    /**
     * Method to return a string version of this object.
     * @return The string "DETACHED_DIRTY".
     **/
    public String toString()
    {
        return "DETACHED_DIRTY";
    }

    @Override
    public LifeCycleState transitionAttach(DNStateManager sm)
    {
        return changeState(sm, P_DIRTY);
    }
}