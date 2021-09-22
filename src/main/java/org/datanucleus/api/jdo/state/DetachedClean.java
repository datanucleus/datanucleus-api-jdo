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
2005 Andy Jefferson - split into own file
    ...
**********************************************************************/
package org.datanucleus.api.jdo.state;

import org.datanucleus.state.LifeCycleState;
import org.datanucleus.state.ObjectProvider;

/**
 * Class representing the life cycle state of DetachedClean.
 */
class DetachedClean extends LifeCycleState
{
    /** Protected Constructor to prevent external instantiation. */
    protected DetachedClean()
    {
        isPersistent = false;           
        isDirty = false;
        isNew = false;
        isDeleted = false;
        isTransactional = false;

        stateType = DETACHED_CLEAN;
    }

    /**
     * Method to return a string version of this object.
     * @return The string "DETACHED_CLEAN".
     **/
    public String toString()
    {
        return "DETACHED_CLEAN";
    }

    @Override
    public LifeCycleState transitionAttach(ObjectProvider sm)
    {
        return changeState(sm, P_CLEAN);
    }
}