/**********************************************************************
Copyright (c) 2015 Andy Jefferson and others. All rights reserved.
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

import org.datanucleus.store.types.converters.TypeConverter;

/**
 * Wrapper for a JDO AttributeConverter for use internally to DataNucleus.
 */
public class JDOTypeConverter<X, Y> implements TypeConverter<X, Y>
{
    private static final long serialVersionUID = -4250901331525617340L;

    // AttributeConverter<X, Y> jdoConverter;

    /** The member type. */
    Class<X> memberType;

    /** The datastore type. */
    Class<Y> dbType;

    public JDOTypeConverter(/* AttributeConverter<X, Y> conv, */ Class<X> memberType, Class<Y> dbType)
    {
        // TODO Set jdoConverter
        this.dbType = dbType;
        this.memberType = memberType;
    }

    public Class<X> getMemberClass()
    {
        return memberType;
    }

    public Class<Y> getDatastoreClass()
    {
        return dbType;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.converters.TypeConverter#toDatastoreType(java.lang.Object)
     */
    @Override
    public Y toDatastoreType(X memberValue)
    {
//        return jdoConverter.convertToDatastore(memberValue);
        return null;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.types.converters.TypeConverter#toMemberType(java.lang.Object)
     */
    @Override
    public X toMemberType(Y datastoreValue)
    {
//      return jdoConverter.convertToAttribute(datastoreValue);
        return null;
    }

    public String toString()
    {
        return "JDOTypeConverter<" + memberType.getName() + "," + dbType.getName() + ">";
    }
}