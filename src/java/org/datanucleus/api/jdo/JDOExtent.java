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

import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

/**
 * Wrapper implementation of a JDO Extent.
 * @param <E> type that this Extent is for.
 */
public class JDOExtent<E> implements Extent<E>
{
    /** Underlying PersistenceManager. */
    PersistenceManager pm;

    /** Underlying Extent. */
    org.datanucleus.store.Extent extent;

    /** JDO Fetch Plan. */
    JDOFetchPlan fetchPlan = null;

    /**
     * Constructor.
     * @param pm PersistenceManager
     * @param extent Underlying Extent
     */
    public JDOExtent(PersistenceManager pm, org.datanucleus.store.Extent extent)
    {
        this.pm = pm;
        this.extent = extent;
        fetchPlan = new JDOFetchPlan(extent.getFetchPlan());
    }

    /**
     * Method to close the Extent iterator.
     * @param iterator Iterator for the extent.
     */
    public void close(Iterator iterator)
    {
        extent.close(iterator);
    }

    /**
     * Method to close all Extent iterators.
     */
    public void closeAll()
    {
        extent.closeAll();
    }

    /**
     * Accessor for the candidate class of the Extent.
     * @return Candidate class
     */
    public Class<E> getCandidateClass()
    {
        return extent.getCandidateClass();
    }

    /**
     * Accessor for whether the Extent includes subclasses.
     * @return Whether it has subclasses
     */
    public boolean hasSubclasses()
    {
        return extent.hasSubclasses();
    }

    /**
     * Accessor for the FetchPlan for the Extent.
     * @return FetchPlan
     */
    public FetchPlan getFetchPlan()
    {
        return fetchPlan;
    }

    /**
     * Accessor for the PersistenceManager.
     * @return The PM
     */
    public PersistenceManager getPersistenceManager()
    {
        return pm;
    }

    /**
     * Accessor for the real extent.
     * @return The Underlying extent
     */
    public org.datanucleus.store.Extent getExtent()
    {
        return extent;
    }

    /**
     * Accessor for an iterator for this Extent.
     * @return The iterator
     */
    public Iterator<E> iterator()
    {
        return extent.iterator();
    }
}