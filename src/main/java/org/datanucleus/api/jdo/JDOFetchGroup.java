/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
import java.util.Set;

import javax.jdo.FetchGroup;
import javax.jdo.JDOUserException;

import org.datanucleus.exceptions.NucleusUserException;

/**
 * Implementation of a FetchGroup for JDO.
 * Provides a JDO wrapper around the internal org.datanucleus.FetchGroup.
 */
public class JDOFetchGroup implements javax.jdo.FetchGroup, Serializable
{
    private static final long serialVersionUID = 8496393232964294401L;

    org.datanucleus.FetchGroup fg = null;

    /**
     * Constructor.
     * @param fg The internal FetchGroup
     */
    public JDOFetchGroup(org.datanucleus.FetchGroup fg)
    {
        this.fg = fg;
    }

    /**
     * Accessor for the internal fetch group.
     * @return Fetch group
     */
    public org.datanucleus.FetchGroup getInternalFetchGroup()
    {
        return fg;
    }

    /**
     * Accessor for the group name.
     * @return Name of the group
     */
    public String getName()
    {
        return fg.getName();
    }

    /**
     * Accessor for the class that this group is for.
     * @return the class
     */
    public Class getType()
    {
        return fg.getType();
    }

    /**
     * Mutator for whether the postLoad callback should be called on loading this fetch group.
     * @param postLoad Whether the postLoad callback should be called.
     * @return This fetch group
     */
    public FetchGroup setPostLoad(boolean postLoad)
    {
        assertUnmodifiable();

        fg.setPostLoad(postLoad);
        return this;
    }

    /**
     * Accessor for whether to call postLoad when this group is loaded.
     * @return Whether to call postLoad
     */
    public boolean getPostLoad()
    {
        return fg.getPostLoad();
    }

    /**
     * Accessor for the recursion depth for the specified field/property.
     * @param memberName Name of field/property
     * @return The recursion depth
     */
    public int getRecursionDepth(String memberName)
    {
        return fg.getRecursionDepth(memberName);
    }

    /**
     * Method to set the recursion depth for the specified field/property.
     * @param memberName Name of field/property
     * @param recursionDepth Recursion depth
     * @return The fetch group
     */
    public FetchGroup setRecursionDepth(String memberName, int recursionDepth)
    {
        assertUnmodifiable();

        fg.setRecursionDepth(memberName, recursionDepth);
        return this;
    }

    /**
     * Method to make the FetchGroup unmodifiable.
     * @return The FetchGroup
     */
    public FetchGroup setUnmodifiable()
    {
        fg.setUnmodifiable();
        return this;
    }

    /**
     * Accessor for whether the FetchGroup is unmodifiable
     * @return Whether unmodifiable
     */
    public boolean isUnmodifiable()
    {
        return fg.isUnmodifiable();
    }

    /**
     * Convenience method to add the members in the specified category.
     * @param categoryName The category
     * @return This FetchGroup
     */
    public FetchGroup addCategory(String categoryName)
    {
        assertUnmodifiable();

        fg.addCategory(categoryName);
        return this;
    }

    /**
     * Convenience method to remove the members in the specified category.
     * @param categoryName The category
     * @return This FetchGroup
     */
    public FetchGroup removeCategory(String categoryName)
    {
        assertUnmodifiable();

        fg.removeCategory(categoryName);
        return this;
    }

    /**
     * Accessor for the members that are in this fetch group.
     * @return Set of member names.
     */
    public Set getMembers()
    {
        return fg.getMembers();
    }

    /**
     * Method to add a field of the class to the fetch group.
     * @param memberName Name of the field
     * @return This FetchGroup
     * @throws JDOUserException if the field doesn't exist for this class
     */
    public FetchGroup addMember(String memberName)
    {
        assertUnmodifiable();

        try
        {
            fg.addMember(memberName);
            return this;
        }
        catch (NucleusUserException nue)
        {
            throw DataNucleusHelperJDO.getJDOExceptionForNucleusException(nue);
        }
    }

    /**
     * Method to remove a field of the class from the fetch group.
     * @param memberName Name of the field/property
     * @return This FetchGroup
     */
    public FetchGroup removeMember(String memberName)
    {
        assertUnmodifiable();

        try
        {
            fg.removeMember(memberName);
            return this;
        }
        catch (NucleusUserException nue)
        {
            throw DataNucleusHelperJDO.getJDOExceptionForNucleusException(nue);
        }
    }

    /**
     * Method to add members of the class from the fetch group.
     * @param members Names of the fields/properties
     * @return This FetchGroup
     */
    public FetchGroup addMembers(String... members)
    {
        assertUnmodifiable();

        try
        {
            fg.addMembers(members);
            return this;
        }
        catch (NucleusUserException nue)
        {
            throw DataNucleusHelperJDO.getJDOExceptionForNucleusException(nue);
        }
    }

    /**
     * Method to remove members of the class from the fetch group.
     * @param members Names of the fields/properties
     * @return This FetchGroup
     */
    public FetchGroup removeMembers(String... members)
    {
        assertUnmodifiable();

        try
        {
            fg.removeMembers(members);
            return this;
        }
        catch (NucleusUserException nue)
        {
            throw DataNucleusHelperJDO.getJDOExceptionForNucleusException(nue);
        }
    }

    /**
     * Method to throw an exception if the fetch group is currently unmodifiable.
     * @throw JDOUserException Thrown if the FetchGroup is unmodifiable
     */
    private void assertUnmodifiable()
    {
        if (fg.isUnmodifiable())
        {
            throw new JDOUserException("FetchGroup is unmodifiable!");
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof JDOFetchGroup))
        {
            return false;
        }
        JDOFetchGroup other = (JDOFetchGroup)obj;
        return other.fg.equals(fg);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return fg.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return fg.toString();
    }
}