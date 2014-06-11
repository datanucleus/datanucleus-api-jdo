/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.api.jdo;

import javax.jdo.listener.InstanceLifecycleEvent;

/**
 * Extension to InstanceLifecycleEvent where the event can relate to specific
 * field providing access to the field names that are affected by this event.
 */
public class FieldInstanceLifecycleEvent extends InstanceLifecycleEvent
{
    private static final long serialVersionUID = 4518746566556032678L;
    /** Names of the fields affected. */
    private String[] fieldNames;

    /**
     * Constructor.
     * @param obj The object on which the event occurs
     * @param eventType Type of event
     * @param otherObj The other object
     * @param fieldNames Names of the fields affected
     */
    public FieldInstanceLifecycleEvent(Object obj, int eventType, Object otherObj, String[] fieldNames)
    {
        super(obj, eventType, otherObj);
        this.fieldNames = fieldNames;
    }

    /**
     * Accessor for the field names affected by this event
     * @return The field names
     */
    public String[] getFieldNames()
    {
        return fieldNames;
    }
}