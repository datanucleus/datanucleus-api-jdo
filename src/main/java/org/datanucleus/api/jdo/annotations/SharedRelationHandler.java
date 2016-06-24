/**********************************************************************
Copyright (c) 2016 Andy Jefferson and others. All rights reserved.
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
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.MemberAnnotationHandler;

/**
 * Handler for the {@link SharedRelation} annotation when applied to a field/property of a persistable class.
 */
public class SharedRelationHandler implements MemberAnnotationHandler
{
    public void processMemberAnnotation(AnnotationObject ann, AbstractMemberMetaData mmd, ClassLoaderResolver clr)
    {
        Map<String, Object> annotationValues = ann.getNameValueMap();
        String column = (String)annotationValues.get("column");
        String value = (String)annotationValues.get("value");
        Boolean pk = (Boolean)annotationValues.get("primaryKey");

        mmd.addExtension("relation-discriminator-column", column);
        mmd.addExtension("relation-discriminator-value", value);
        if (pk)
        {
            mmd.addExtension("relation-discriminator-pk", "true");
        }
    }
}