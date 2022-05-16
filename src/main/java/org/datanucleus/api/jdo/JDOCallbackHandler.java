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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.api.jdo.metadata.JDOAnnotationUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.EventListenerMetaData;
import org.datanucleus.state.CallbackHandler;
import org.datanucleus.state.DNStateManager;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;

/**
 * CallbackHandler implementation for JDO.
 */
public class JDOCallbackHandler implements CallbackHandler
{
    ExecutionContext ec;

    private final Map<InstanceLifecycleListener, LifecycleListenerForClass> listeners = new IdentityHashMap<InstanceLifecycleListener, LifecycleListenerForClass>(1);

    private List<LifecycleListenerForClass> listenersWorkingCopy = null;

    BeanValidationHandler beanValidationHandler;
    
    boolean allowAnnotatedCallbacks = false;

    public JDOCallbackHandler(ExecutionContext ec)
    {
        this.ec = ec;
        this.beanValidationHandler = ec.getNucleusContext().getBeanValidationHandler(ec);

        allowAnnotatedCallbacks = ec.getNucleusContext().getConfiguration().getBooleanProperty("datanucleus.allowInstanceCallbackAnnotations", false);
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
                // PRE_STORE will return the fields being stored (DataNucleus extension)
                DNStateManager sm = ec.findStateManager(pc);
                String[] fieldNames = sm.getDirtyFieldNames();
                if (fieldNames == null)
                {
                    // Must be persisting so just return all loaded fields
                    fieldNames = sm.getLoadedFieldNames();
                }
                ((StoreLifecycleListener)listener.getListener()).preStore(new FieldInstanceLifecycleEvent(pc, InstanceLifecycleEvent.STORE, null, fieldNames));
            }
        }

        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.PRESTORE, false);
        }

        if (pc instanceof StoreCallback)
        {
            // Has a jdoPreStore method
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
            DNStateManager sm = ec.findStateManager(pc);
            if (!sm.getLifecycleState().isNew())
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

        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.PRECLEAR, false);
        }

        if (pc instanceof ClearCallback)
        {
            // Has a jdoPreClear method
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

        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.PREDELETE, false);
        }

        if (pc instanceof DeleteCallback)
        {
            // Has a jdoPreDelete method
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
        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.POSTLOAD, false);
        }

        if (pc instanceof LoadCallback)
        {
            // Has a jdoPostLoad method
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

        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.PREDETACH, false);
        }

        if (pc instanceof DetachCallback)
        {
            // Has a jdoPreDetach method
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
        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.POSTDETACH, true);
        }

        if (pc instanceof DetachCallback)
        {
            // Has a jdoPostDetach method
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

        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.PREATTACH, false);
        }

        if (pc instanceof AttachCallback)
        {
            // Has a jdoPreAttach method
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
        if (allowAnnotatedCallbacks)
        {
            invokeCallback(pc, JDOAnnotationUtils.POSTATTACH, true);
        }

        if (pc instanceof AttachCallback)
        {
            // Has a jdoPostAttach method
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
            listenersWorkingCopy = new ArrayList<>(listeners.values());
        }

        return listenersWorkingCopy;
    }

    /**
     * Method to invoke all listeners for a particular callback.
     * @param pc The PC object causing the event
     * @param callbackClass The callback type to call
     * @param pcArgument Whether to pass a PC argument to the callback
     */
    private void invokeCallback(final Object pc, final String callbackClassName, boolean pcArgument)
    {
        final ClassLoaderResolver clr = ec.getClassLoaderResolver();

        // Class listeners for this class
        AbstractClassMetaData acmd = ec.getMetaDataManager().getMetaDataForClass(pc.getClass(), clr);
        List<String> entityMethodsToInvoke = null;
        while (acmd != null)
        {
            List<EventListenerMetaData> listenerMetaData = acmd.getListeners();
            if (listenerMetaData != null && !listenerMetaData.isEmpty())
            {
                // Class has listeners so go through them in the same order
                Iterator<EventListenerMetaData> listenerIter = listenerMetaData.iterator();
                while (listenerIter.hasNext())
                {
                    EventListenerMetaData elmd = listenerIter.next();
                    if (elmd.getClassName().equals(acmd.getFullClassName()))
                    {
                        // Only looking for calling methods in the class of the object since these are annotated methods
                        String methodName = elmd.getMethodNameForCallbackClass(callbackClassName);
                        if (methodName != null)
                        {
                            if (entityMethodsToInvoke == null)
                            {
                                entityMethodsToInvoke = new ArrayList<String>();
                            }
                            if (!entityMethodsToInvoke.contains(methodName))
                            {
                                // Only add the method if is not already present (allows for inherited listener methods)
                                entityMethodsToInvoke.add(methodName);
                            }
                        }
                    }
                }
                if (acmd.isExcludeSuperClassListeners())
                {
                    break;
                }
            }

            // Move up to superclass
            acmd = acmd.getSuperAbstractClassMetaData();
        }

        if (entityMethodsToInvoke != null && !entityMethodsToInvoke.isEmpty())
        {
            // Invoke all listener methods on the entity
            for (int i=0;i<entityMethodsToInvoke.size();i++)
            {
                String methodName = entityMethodsToInvoke.get(i);
                invokeCallbackMethod(pc, methodName, clr, pcArgument);
            }
        }
    }

    /**
     * Method to invoke a method of a listener where the Entity is the listener.
     * Means that the method invoked takes no arguments as input.
     * @param listener Listener object
     * @param methodName The method name, including the class name prefixed
     * @param pcArgument Whether to pass a PC argument to the callback
     */
    private void invokeCallbackMethod(final Object pc, final String methodName, ClassLoaderResolver clr, boolean pcArgument)
    {
        final String callbackClassName = methodName.substring(0, methodName.lastIndexOf('.'));
        final String callbackMethodName = methodName.substring(methodName.lastIndexOf('.')+1);
        final Class callbackClass = callbackClassName.equals(pc.getClass().getName()) ? pc.getClass() : clr.classForName(callbackClassName);

        try
        {
            Class[] classArgs = pcArgument ? new Class[]{Object.class} : null;
            Object[] methodArgs = pcArgument ? new Object[] {pc} : null;
            Method m = callbackClass.getDeclaredMethod(callbackMethodName, classArgs);
            if (!m.canAccess(pc))
            {
                m.setAccessible(true);
            }
            m.invoke(pc, methodArgs);
        }
        catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException e)
        {
            NucleusLogger.GENERAL.warn("Exception in JDOCallbackHandler", e);
        }
        catch (InvocationTargetException e)
        {
            if (e.getTargetException() instanceof RuntimeException)
            {
                throw (RuntimeException) e.getTargetException();
            }
            throw new RuntimeException(e.getTargetException());
        }
    }
}