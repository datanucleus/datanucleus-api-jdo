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

import java.lang.reflect.Method;

import javax.jdo.AttributeConverter;

import org.datanucleus.util.NucleusLogger;

/**
 * Convenience methods for handling AttributeConverters.
 */
public class JDOTypeConverterUtils
{
    /**
     * Convenience method that takes the class of an AttributeConverter class, and returns the attribute type that it is for.
     * @param converterCls The converter class
     * @param attrTypeFallback The fallback to return if the attribute type is not found
     * @return The attribute type for this converter
     */
    public static Class getAttributeTypeForAttributeConverter(Class<? extends AttributeConverter> converterCls, Class attrTypeFallback)
    {
        Class attrType = attrTypeFallback;
        Method[] methods = converterCls.getMethods();
        if (methods != null)
        {
            for (int j=0;j<methods.length;j++)
            {
                if (methods[j].getName().equals("convertToAttribute"))
                {
                    Class returnCls = methods[j].getReturnType();
                    if (returnCls != Object.class)
                    {
                        attrType = returnCls;
                        break;
                    }
                }
            }
        }
        return attrType;
    }

    /**
     * Convenience method that takes the class of an AttributeConverter class, and returns the datastore type that it is for.
     * @param converterCls The converter class
     * @param attrType Type for the attribute
     * @param dbTypeFallback The fallback to return if the datastore type is not found
     * @return The datastore type for this converter
     */
    public static Class getDatastoreTypeForAttributeConverter(Class<? extends AttributeConverter> converterCls, Class attrType, Class dbTypeFallback)
    {
        Class dbType = dbTypeFallback;
        try
        {
            Class returnCls = converterCls.getMethod("convertToDatastore", attrType).getReturnType();
            if (returnCls != Object.class)
            {
                dbType = returnCls;
            }
        }
        catch (Exception e)
        {
            NucleusLogger.GENERAL.error("Exception in lookup", e);
        }
        return dbType;
    }

}
