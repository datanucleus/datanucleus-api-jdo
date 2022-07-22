/**********************************************************************
Copyright (c) 2004 Andy Jefferson and others. All rights reserved.
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

import javax.jdo.listener.InstanceLifecycleListener;

/**
 * Wrapper for a LifecycleListener for an array of classes. If the classes is null, applies
 * to all classes.
 */
public class LifecycleListenerForClass
{
    /** The classes to which this listener applies */
    private final Class[] classes;

    /** The listener. */
    private final InstanceLifecycleListener listener;

    /**
     * Constructor.
     * @param listener The listener
     * @param classes The classes supported by the listener
     */
    public LifecycleListenerForClass(InstanceLifecycleListener listener, Class[] classes)
    {
        this.classes = classes;
        this.listener = listener;
    }

    /**
     * Accessor for the listener for this specification
     * @return The listener
     */
    public InstanceLifecycleListener getListener()
    {
        return listener;
    }

    /**
     * Accessor for the classes in this specification
     * @return The classes
     */
    public Class[] getClasses()
    {
        return classes;
    }
    
    /**
     * Accessor for whether the listener supported the class.
     * Actually checks if the passed class is a subclass of one of the listener classes.
     * @param cls The class
     * @return Whether it is supported
     */
    public boolean forClass(Class<?> cls)
    {
        if (classes == null)
        {
            return true;
        }

        for (int i=0;i<classes.length;i++)
        {
            if (classes[i].isAssignableFrom(cls))
            {
                return true;
            }
        }
        return false;
    }

    LifecycleListenerForClass mergeClasses(Class[] extraClasses)
    {
        if (classes == null)
        {
            return this;
        }
        if (extraClasses == null)
        {
            return new LifecycleListenerForClass(listener, null);
        }
        Class[] allClasses = new Class[classes.length + extraClasses.length];
        System.arraycopy(classes, 0, allClasses, 0, classes.length);
        System.arraycopy(extraClasses, 0, allClasses, classes.length, extraClasses.length);
        return new LifecycleListenerForClass(listener, allClasses);
    }

    /**
     * For non-null argument, makes a defensive copy, filtering out any entries that are null
     * @param classes an array received as argument to one of the addInstanceLifecycleListener methods
     * @return a copy of the input array, excluding nulls, or null if input is null
     */
    static Class[] canonicaliseClasses(Class[] classes)
    {
        if (classes == null)
        {
            return null;
        }

        // Count how many nulls there are
        int count = 0;
        for (Class c : classes)
        {
            if (c == null)
            {
                ++count;
            }
        }

        // Make a copy of the array, ignoring nulls
        Class[] result = new Class[classes.length - count];
        int pos = 0;
        for (Class c : classes)
        {
            if (c != null)
            {
                result[pos++] = c;
            }
        }
        return result;
    }
}