/**********************************************************************
Copyright (c) 2009 Erik Bengtson and others. All rights reserved.
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
package org.datanucleus;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

public class FetchGroupManagerTest extends TestCase
{
    public void testAddFetchGroup()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
        fgmgr.addFetchGroup(fg);
        assertEquals(fgmgr.getFetchGroup(FetchGroup.class, "fg1"), fg);
    }

    public void testRemoveFetchGroup()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
        fgmgr.addFetchGroup(fg);
        fgmgr.removeFetchGroup(fg);
    }

    public void testCreateFetchGroup()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
        assertEquals("fg1",fg.getName());
        assertEquals(FetchGroup.class, fg.getType());
    }

    public void testGetFetchGroupsWithName()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
        fgmgr.addFetchGroup(fg);
        assertTrue(fgmgr.getFetchGroupsWithName("fg1").contains(fg));
    }

    public void testClearFetchGroups()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        fgmgr.createFetchGroup(FetchGroup.class,"fg1");
        fgmgr.clearFetchGroups();
    }

    public void testMultithreadedAccess()
    {
        NucleusContext ctx = new NucleusContext("JDO", null);
        final FetchGroupManager fgmgr = new FetchGroupManager(ctx);
        Thread[] threads = new Thread[300];
        final AtomicInteger counter = new AtomicInteger();
        for (int i=0; i<100; i++)
        {
            threads[i] = new Thread(new Runnable(){
            
                public void run()
                {
                    FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
                    fgmgr.addFetchGroup(fg);
                    counter.incrementAndGet();
                }
            });
        }
        for (int i=100; i<200; i++)
        {
            threads[i] = new Thread(new Runnable(){
            
                public void run()
                {
                    FetchGroup fg = fgmgr.createFetchGroup(FetchGroup.class,"fg1");
                    fgmgr.removeFetchGroup(fg);
                    counter.incrementAndGet();
                }
            });
        }
        for (int i=200; i<300; i++)
        {
            threads[i] = new Thread(new Runnable(){
            
                public void run()
                {
                    fgmgr.createFetchGroup(FetchGroup.class,"fg1");
                    fgmgr.clearFetchGroups();
                    counter.incrementAndGet();
                }
            });
        }
        for (int i=0; i<300; i++)
        {
            threads[i].start();
        }
        for (int i=0; i<300; i++)
        {
            try
            {
                threads[i].join();
            }
            catch (InterruptedException e)
            {
            }
        }
        assertEquals(300, counter.intValue());
    }
}