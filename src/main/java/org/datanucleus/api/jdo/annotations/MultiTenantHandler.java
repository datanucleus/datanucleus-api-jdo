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

import javax.jdo.annotations.Column;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.api.jdo.metadata.JDOAnnotationUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.MultitenancyMetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.ClassAnnotationHandler;
import org.datanucleus.util.StringUtils;

/**
 * Handler for the {@link MultiTenant} annotation when applied to a class.
 */
public class MultiTenantHandler implements ClassAnnotationHandler
{
    /* (non-Javadoc)
     * @see org.datanucleus.metadata.annotations.ClassAnnotationHandler#processClassAnnotation(org.datanucleus.metadata.annotations.AnnotationObject, org.datanucleus.metadata.AbstractClassMetaData, org.datanucleus.ClassLoaderResolver)
     */
    @Override
    public void processClassAnnotation(AnnotationObject annotation, AbstractClassMetaData cmd, ClassLoaderResolver clr)
    {
        MultitenancyMetaData mtmd = cmd.newMultitenancyMetaData();

        Map<String, Object> annotationValues = annotation.getNameValueMap();

        String columnName = (String)annotationValues.get("column");
        Column[] columns = (Column[])annotationValues.get("columns");

        if (columns != null && columns.length > 0)
        {
            // Only use the first column
            ColumnMetaData colmd = JDOAnnotationUtils.getColumnMetaDataForColumnAnnotation(columns[0]);
            colmd.setName(columnName);
            mtmd.setColumnMetaData(colmd);
        }
        else
        {
            mtmd.setColumnName(columnName);
        }

        String indexed = (String)annotationValues.get("indexed");
        if (!StringUtils.isWhitespace(indexed))
        {
            mtmd.setIndexed(Boolean.parseBoolean(indexed));
        }
    }
}