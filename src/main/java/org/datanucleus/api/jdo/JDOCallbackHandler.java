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
2007 Andy Jefferson - added prePersist
    ...
**********************************************************************/
package org.datanucleus.api.jdo;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOUserCallbackException;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.AttachLifecycleListener;
import javax.jdo.listener.ClearCallback;
import javax.jdo.listener.ClearLifecycleListener;
import javax.jdo.listener.CreateLifecycleListener;
import javax.jdo.listener.DeleteCallback;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.DetachLifecycleListener;
import javax.jdo.listener.DirtyLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadCallback;
import javax.jdo.listener.LoadLifecycleListener;
import javax.jdo.listener.StoreCallback;
import javax.jdo.listener.StoreLifecycleListener;

import org.datanucleus.BeanValidationHandler;
import org.datanucleus.ExecutionContext;
import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.state.CallbackHandler;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.util.Localiser;

/**
 * CallbackHandler implementation for JDO.
 */
public class JDOCallbackHandler implements CallbackHandler
{
    PersistenceNucleusContext nucleusCtx;

    private final Map<InstanceLifecycleListener, LifecycleListenerForClass> listeners = new IdentityHashMap<InstanceLifecycleListener, LifecycleListenerForClass>(1);

    private List<LifecycleListenerForClass> listenersWorkingCopy = null;

    BeanValidationHandler beanValidationHandler;

    public JDOCallbackHandler(PersistenceNucleusContext nucleusCtx)
    {
        this.nucleusCtx = nucleusCtx;
    }

    public void setBeanValidationHandler(BeanValidationHandler handler)
    {
        beanValidationHandler = handler;
    }

    /**
     * Callback after the object has been created.
     * @param pc The Object
     */
    public void postCreate(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof CreateLifecycleListener)
            {
                ((CreateLifecycleListener)listener.getListener()).postCreate(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CREATE, null));
            }
        }
    }

    /**
     * Callback before the object is persisted (just before the lifecycle state change).
     * @param pc The Object
     */
    public void prePersist(Object pc)
    {
        // Not supported by JDO for normal events
        if (beanValidationHandler != null)
        {
            beanValidationHandler.prePersist(pc);
        }
    }

    /**
     * Callback before the object is stored.
     * @param pc The Object
     */
    public void preStore(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof StoreLifecycleListener)
            {
                ExecutionContext ec = nucleusCtx.getApiAdapter().getExecutionContext(pc);
                String[] fieldNames = null;
                // PRE_STORE will return the fields being stored (DataNucleus extension)
                ObjectProvider op = ec.findObjectProvider(pc);
                fieldNames = op.getDirtyFieldNames();
                if (fieldNames == null)
                {
                    // Must be persisting so just return all loaded fields
                    fieldNames = op.getLoadedFieldNames();
                }
                ((StoreLifecycleListener)listener.getListener()).preStore(new FieldInstanceLifecycleEvent(pc, InstanceLifecycleEvent.STORE, null, fieldNames));
            }
        }

        if (pc instanceof StoreCallback)
        {
            try
            {
                ((StoreCallback) pc).jdoPreStore();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPreStore"), e);
            }
        }

        if (beanValidationHandler != null)
        {
            ObjectProvider op = nucleusCtx.getApiAdapter().getExecutionContext(pc).findObjectProvider(pc);
            if (!op.getLifecycleState().isNew())
            {
                // Don't fire this when persisting new since we will have done prePersist
                beanValidationHandler.preStore(pc);
            }
        }
    }

    /**
     * Callback after the object is stored.
     * @param pc The Object
     */
    public void postStore(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof StoreLifecycleListener)
            {
                ((StoreLifecycleListener)listener.getListener()).postStore(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.STORE, null));
            }
        }
    }

    /**
     * Callback before the fields of the object are cleared.
     * @param pc The Object
     */
    public void preClear(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof ClearLifecycleListener)
            {
                ((ClearLifecycleListener)listener.getListener()).preClear(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CLEAR, null));
            }
        }

        if (pc instanceof ClearCallback)
        {
            try
            {
                ((ClearCallback) pc).jdoPreClear();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPreClear"), e);
            }
        }
    }

    /**
     * Callback after the fields of the object are cleared.
     * @param pc The Object
     */
    public void postClear(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof ClearLifecycleListener)
            {
                ((ClearLifecycleListener)listener.getListener()).postClear(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.CLEAR, null));
            }
        }
    }

    /**
     * Callback before the object is deleted.
     * @param pc The Object
     */
    public void preDelete(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DeleteLifecycleListener)
            {
                ((DeleteLifecycleListener)listener.getListener()).preDelete(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DELETE, null));
            }
        }

        if (pc instanceof DeleteCallback)
        {
            try
            {
                ((DeleteCallback) pc).jdoPreDelete();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPreDelete"), e);
            }
        }

        if (beanValidationHandler != null)
        {
            beanValidationHandler.preDelete(pc);
        }
    }

    /**
     * Callback after the object is deleted.
     * @param pc The Object
     */
    public void postDelete(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DeleteLifecycleListener)
            {
                ((DeleteLifecycleListener)listener.getListener()).postDelete(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DELETE, null));
            }
        }
    }

    /**
     * Callback before the object is made dirty.
     * @param pc The Object
     */
    public void preDirty(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DirtyLifecycleListener)
            {
                ((DirtyLifecycleListener)listener.getListener()).preDirty(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DIRTY, null));
            }
        }
    }

    /**
     * Callback after the object is made dirty.
     * @param pc The Object
     */
    public void postDirty(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DirtyLifecycleListener)
            {
                ((DirtyLifecycleListener)listener.getListener()).postDirty(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DIRTY, null));
            }
        }
    }

    /**
     * Callback after the fields of the object are loaded.
     * @param pc The Object
     */
    public void postLoad(Object pc)
    {
        if (pc instanceof LoadCallback)
        {
            try
            {
                ((LoadCallback) pc).jdoPostLoad();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPostLoad"), e);
            }
        }

        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof LoadLifecycleListener)
            {
                ((LoadLifecycleListener)listener.getListener()).postLoad(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.LOAD, null));
            }
        }
    }

    /**
     * Callback after the fields of the object are refreshed.
     * @param pc The Object
     */
    public void postRefresh(Object pc)
    {
        // do nothing; JDO does not invoke postRefresh
    }

    /**
     * Callback before the object is detached.
     * @param pc The Object
     */
    public void preDetach(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DetachLifecycleListener)
            {
                ((DetachLifecycleListener)listener.getListener()).preDetach(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.DETACH, null));
            }
        }

        if (pc instanceof DetachCallback)
        {
            try
            {
                ((DetachCallback) pc).jdoPreDetach();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPreDetach"), e);
            }
        }        
    }

    /**
     * Callback after the object is detached.
     * @param pc The Object
     * @param detachedPC The detached object
     */
    public void postDetach(Object pc, Object detachedPC)
    {
        if (pc instanceof DetachCallback)
        {
            try
            {
                ((DetachCallback) detachedPC).jdoPostDetach(pc);
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPostDetach"), e);
            }
        }

        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof DetachLifecycleListener)
            {
                ((DetachLifecycleListener)listener.getListener()).postDetach(new InstanceLifecycleEvent(detachedPC, InstanceLifecycleEvent.DETACH, pc));
            }
        }
    }

    /**
     * Callback before the object is attached.
     * @param pc The Object
     */
    public void preAttach(Object pc)
    {
        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof AttachLifecycleListener)
            {
                ((AttachLifecycleListener)listener.getListener()).preAttach(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.ATTACH, null));
            }
        }

        if (pc instanceof AttachCallback)
        {
            try
            {
                ((AttachCallback) pc).jdoPreAttach();
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPreAttach"), e);
            }
        }
    }

    /**
     * Callback after the object is attached.
     * @param pc The attached Object
     * @param detachedPC The detached object
     */
    public void postAttach(Object pc,Object detachedPC)
    {
        if (pc instanceof AttachCallback)
        {
            try
            {
                ((AttachCallback) pc).jdoPostAttach(detachedPC);
            }
            catch (Exception e)
            {
                throw new JDOUserCallbackException(Localiser.msg("025001", "jdoPostAttach"), e);
            }
        }

        for (LifecycleListenerForClass listener : getListenersWorkingCopy())
        {
            if (listener.forClass(pc.getClass()) && listener.getListener() instanceof AttachLifecycleListener)
            {
                ((AttachLifecycleListener)listener.getListener()).postAttach(new InstanceLifecycleEvent(pc, InstanceLifecycleEvent.ATTACH, detachedPC));
            }
        }
    }

    /**
     * Adds a new listener to this handler.
     * @param listener the listener instance
     * @param classes the persistent classes which events are fired for the listener  
     */
    public void addListener(Object listener, Class[] classes)
    {
        if (listener == null)
        {
            return;
        }

        InstanceLifecycleListener jdoListener = (InstanceLifecycleListener)listener;

        LifecycleListenerForClass entry;
        if (listeners.containsKey(jdoListener))
        {
            entry = listeners.get(jdoListener).mergeClasses(classes);
        }
        else
        {
            entry = new LifecycleListenerForClass(jdoListener, classes);
        }

        listeners.put(jdoListener, entry);
        listenersWorkingCopy = null;
    }

    /**
     * Remove a listener for this handler.
     * @param listener the listener instance
     */
    public void removeListener(Object listener)
    {
        // Remove from the PMF
        if (listeners.remove(listener) != null)
        {
            listenersWorkingCopy = null;
        }
    }

    /**
     * Clear any objects to release resources.
     */
    public void close()
    {
        listeners.clear();
        listenersWorkingCopy = null;
    }

    /**
     * Accessor for the working copy of the listeners (in case any are added/removed in the callbacks).
     * @return The working copy
     */
    protected List<LifecycleListenerForClass> getListenersWorkingCopy()
    {
        if (listenersWorkingCopy == null)
        {
            listenersWorkingCopy = new ArrayList<LifecycleListenerForClass>(listeners.values());
        }

        return listenersWorkingCopy;
    }
}