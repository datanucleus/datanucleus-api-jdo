/**********************************************************************
Copyright (c) 2017 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.api.jdo.annotations;

import java.util.Map;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.ClassAnnotationHandler;
import org.datanucleus.metadata.annotations.MemberAnnotationHandler;

/**
 * Handler for the {@link CreateUser} annotation when applied to a field/property or surrogate column of a persistable class.
 */
public class CreateUserHandler implements MemberAnnotationHandler, ClassAnnotationHandler
{
    public void processMemberAnnotation(AnnotationObject ann, AbstractMemberMetaData mmd, ClassLoaderResolver clr)
    {
        mmd.addExtension(MetaData.EXTENSION_MEMBER_CREATE_USER, "true");
    }

    public void processClassAnnotation(AnnotationObject annotation, AbstractClassMetaData cmd, ClassLoaderResolver clr)
    {
        cmd.addExtension(MetaData.EXTENSION_CLASS_CREATEUSER, "true");

        Map<String, Object> annotationValues = annotation.getNameValueMap();
        String colName = (String)annotationValues.get("column");
        if (colName != null && colName.length() > 0)
        {
            cmd.addExtension(MetaData.EXTENSION_CLASS_CREATEUSER_COLUMN_NAME, colName);
        }

        Integer colLength = (Integer)annotationValues.get("columnLength");
        if (colLength != null && colLength > 0)
        {
            cmd.addExtension(MetaData.EXTENSION_CLASS_CREATEUSER_COLUMN_LENGTH, "" + colLength);
        }
    }
}