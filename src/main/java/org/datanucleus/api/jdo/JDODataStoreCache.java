/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.datastore.DataStoreCache;

import org.datanucleus.cache.Level2Cache;

/**
 * Implementation of the JDO DataStoreCache.
 * Provides a wrapper and hands off calls to the underlying Level2 cache
 */
public class JDODataStoreCache implements DataStoreCache, Serializable
{
    /** Underlying Level 2 cache. */
    Level2Cache cache = null;

    /**
     * Constructor.
     * @param cache Level 2 Cache
     */
    public JDODataStoreCache(Level2Cache cache)
    {
        this.cache = cache;
    }

    /**
     * Accessor for the underlying Level 2 cache.
     * @return Underlying L2 cache.
     */
    public Level2Cache getLevel2Cache()
    {
        return cache;
    }

    /**
     * Evict the parameter instance from the second-level cache.
     * @param oid the object id of the instance to evict.
     */
    public void evict(Object oid)
    {
        cache.evict(oid);
    }

    /**
     * Evict the parameter instances from the second-level cache.
     * All instances in the PersistenceManager's cache are evicted
     * from the second-level cache.
     */
    public void evictAll()
    {
        cache.evictAll();
    }

    /**
     * Evict the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     */
    public void evictAll(Object... oids)
    {
        cache.evictAll(oids);
    }

    /**
     * Evict the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     */
    public void evictAll(Collection oids)
    {
        cache.evictAll(oids);
    }

    /**
     * Evict the parameter instances from the second-level cache.
     * @param pcClass the class of instances to evict
     * @param subclasses if true, evict instances of subclasses also
     */
    public void evictAll(Class pcClass, boolean subclasses)
    {
        cache.evictAll(pcClass, subclasses);
    }

    /**
     * Evict the parameter instances from the second-level cache.
     * @param pcClass the class of instances to evict
     * @param subclasses if true, evict instances of subclasses also
     */
    public void evictAll(boolean subclasses, Class pcClass)
    {
        cache.evictAll(pcClass, subclasses);
    }

    /**
     * Pin the parameter instance in the second-level cache.
     * @param oid the object id of the instance to pin.
     */
    public void pin(Object oid)
    {
        cache.pin(oid);
    }

    /**
     * Pin the parameter instances in the second-level cache.
     * @param oids the object ids of the instances to pin.
     */
    public void pinAll(Collection oids)
    {
        cache.pinAll(oids);
    }

    /**
     * Pin the parameter instances in the second-level cache.
     * @param oids the object ids of the instances to pin.
     */
    public void pinAll(Object... oids)
    {
        cache.pinAll(oids);
    }

    /**
     * Pin instances in the second-level cache.
     * @param pcClass the class of instances to pin
     * @param subclasses if true, pin instances of subclasses also
     */
    public void pinAll(Class pcClass, boolean subclasses)
    {
        cache.pinAll(pcClass, subclasses);
    }

    /**
     * Pin instances in the second-level cache.
     * @param subclasses if true, pin instances of subclasses also
     * @param pcClass the class of instances to pin
     */
    public void pinAll(boolean subclasses, Class pcClass)
    {
        cache.pinAll(pcClass, subclasses);
    }

    /**
     * Unpin the parameter instance from the second-level cache.
     * @param oid the object id of the instance to unpin.
     */
    public void unpin(Object oid)
    {
        cache.unpin(oid);
    }

    /**
     * Unpin the parameter instances from the second-level cache.
     * @param oids the object ids of the instance to evict.
     */
    public void unpinAll(Collection oids)
    {
        cache.unpinAll(oids);
    }

    /**
     * Unpin the parameter instance from the second-level cache.
     * @param oids the object id of the instance to evict.
     */
    public void unpinAll(Object... oids)
    {
        cache.unpinAll(oids);
    }

    /**
     * Unpin instances from the second-level cache.
     * @param pcClass the class of instances to unpin
     * @param subclasses if true, unpin instances of subclasses also
     */
    public void unpinAll(Class pcClass, boolean subclasses)
    {
        cache.unpinAll(pcClass, subclasses);
    }

    /**
     * Unpin instances from the second-level cache.
     * @param subclasses if true, unpin instances of subclasses also
     * @param pcClass the class of instances to unpin
     */
    public void unpinAll(boolean subclasses, Class pcClass)
    {
        cache.unpinAll(pcClass, subclasses);
    }
}