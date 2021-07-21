/******************************************************************
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
*****************************************************************/
package org.datanucleus.api.jdo.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.datanucleus.PersistenceNucleusContextImpl;
import org.datanucleus.api.jdo.JDOPropertyNames;

/**
 * Component tests for the MetaDataManager class.
 */
public class MetaDataManagerTest extends TestCase
{
    /**
     * Start of the test, so log it and initialise.
     * @param name Name of the <i>TestCase</i>.
     */
    public MetaDataManagerTest(String name)
    {
        super(name);
    }

    /**
     * Test of the valid locations for a specified package.
     */
    public void testLocationsForPackage()
    {
        Map startupProps = new HashMap<>();
        startupProps.put(JDOPropertyNames.PROPERTY_METADATA_XML_JDO_1_0, "true");
        JDOMetaDataManager mgr = new JDOMetaDataManager(new PersistenceNucleusContextImpl("JDO", startupProps));

        // Try typical JDO package name
        String packageName = "org.datanucleus.samples";
        List locations = mgr.getValidMetaDataLocationsForPackage("jdo", null, packageName);
        assertTrue("Locations returned from MetaData Manager was null!", locations != null);
        List validLocations = new ArrayList();
        validLocations.add("/META-INF/package.jdo");
        validLocations.add("/WEB-INF/package.jdo");
        validLocations.add("/package.jdo");
        validLocations.add("/org.jdo");
        validLocations.add("/org/package.jdo");
        validLocations.add("/org/datanucleus.jdo");
        validLocations.add("/org/datanucleus/package.jdo");
        validLocations.add("/org/datanucleus/samples.jdo");
        validLocations.add("/org/datanucleus/samples/package.jdo");
        checkLocations(packageName, locations, validLocations);

        // Try 1 level package name
        packageName = "org";
        locations = mgr.getValidMetaDataLocationsForPackage("jdo", null, packageName);
        assertTrue("Locations returned from MetaData Manager was null!", locations != null);
        validLocations.clear();
        validLocations.add("/META-INF/package.jdo");
        validLocations.add("/WEB-INF/package.jdo");
        validLocations.add("/package.jdo");
        validLocations.add("/org.jdo");
        validLocations.add("/org/package.jdo");
        checkLocations(packageName, locations, validLocations);

        // Try 0 level package name
        packageName = "";
        locations = mgr.getValidMetaDataLocationsForPackage("jdo", null, packageName);
        assertTrue("Locations returned from MetaData Manager was null!", locations != null);
        validLocations.clear();
        validLocations.add("/META-INF/package.jdo");
        validLocations.add("/WEB-INF/package.jdo");
        validLocations.add("/package.jdo");
        checkLocations(packageName, locations, validLocations);

        // Try typical ORM package name
        packageName = "org.datanucleus.samples";
        locations = mgr.getValidMetaDataLocationsForPackage("orm", "datanucleus", packageName);
        assertTrue("Locations returned from MetaData Manager was null!", locations != null);
        validLocations.clear();
        validLocations.add("/META-INF/package-datanucleus.orm");
        validLocations.add("/WEB-INF/package-datanucleus.orm");
        validLocations.add("/package-datanucleus.orm");
        validLocations.add("/org-datanucleus.orm");
        validLocations.add("/org/package-datanucleus.orm");
        validLocations.add("/org/datanucleus-datanucleus.orm");
        validLocations.add("/org/datanucleus/package-datanucleus.orm");
        validLocations.add("/org/datanucleus/samples-datanucleus.orm");
        validLocations.add("/org/datanucleus/samples/package-datanucleus.orm");
        checkLocations(packageName, locations, validLocations);
    }

    /**
     * Test of the valid locations for a specified class.
     */
    public void testLocationsForClass()
    {
        Map startupProps = new HashMap<>();
        startupProps.put(JDOPropertyNames.PROPERTY_METADATA_XML_JDO_1_0, "true");
        JDOMetaDataManager mgr = new JDOMetaDataManager(new PersistenceNucleusContextImpl("JDO", startupProps));

        // Try typical JDO class name
        String className = "org.datanucleus.samples.store.Product";
        List locations = mgr.getValidMetaDataLocationsForClass("jdo", null, className);
        assertTrue("Locations returned from MetaData Manager was null!", locations != null);
        List validLocations = new ArrayList();
        validLocations.add("/META-INF/package.jdo");
        validLocations.add("/WEB-INF/package.jdo");
        validLocations.add("/package.jdo");
        validLocations.add("/org.jdo");
        validLocations.add("/org/package.jdo");
        validLocations.add("/org/datanucleus.jdo");
        validLocations.add("/org/datanucleus/package.jdo");
        validLocations.add("/org/datanucleus/samples.jdo");
        validLocations.add("/org/datanucleus/samples/package.jdo");
        validLocations.add("/org/datanucleus/samples/store.jdo");
        validLocations.add("/org/datanucleus/samples/store/package.jdo");
        validLocations.add("/org/datanucleus/samples/store/Product.jdo");
        checkLocations(className, locations, validLocations);
    }

    private void checkLocations(String packageName, List locations, List validLocations)
    {
        assertEquals("Number of valid locations for package " + packageName + " is wrong", locations.size(), validLocations.size());
        for (int i=0;i<validLocations.size();i++)
        {
            String validLocation = (String)validLocations.get(i);
            assertTrue("Location " + validLocation + " is not returned as a valid location for package " + packageName + ", but should be", 
                locations.contains(validLocation));
        }
    }
}