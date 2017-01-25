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
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.ClassAnnotationHandler;

/**
 * Handler for the {@link SoftDelete} annotation when applied to a class.
 */
public class SoftDeleteHandler implements ClassAnnotationHandler
{
    /* (non-Javadoc)
     * @see org.datanucleus.metadata.annotations.ClassAnnotationHandler#processClassAnnotation(org.datanucleus.metadata.annotations.AnnotationObject, org.datanucleus.metadata.AbstractClassMetaData, org.datanucleus.ClassLoaderResolver)
     */
    @Override
    public void processClassAnnotation(AnnotationObject annotation, AbstractClassMetaData cmd, ClassLoaderResolver clr)
    {
        Map<String, Object> annotationValues = annotation.getNameValueMap();
        cmd.addExtension(MetaData.EXTENSION_CLASS_SOFTDELETE, "true");

        String column = (String)annotationValues.get("column");
        if (column != null && column.length() > 0)
        {
            cmd.addExtension(MetaData.EXTENSION_CLASS_SOFTDELETE_COLUMN_NAME, column);
        }
    }
}