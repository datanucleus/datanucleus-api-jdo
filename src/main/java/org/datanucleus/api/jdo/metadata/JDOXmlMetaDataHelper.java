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
package org.datanucleus.api.jdo.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ArrayMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.ContainerMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.ElementMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.DatastoreIdentityMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.InheritanceMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.KeyMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.OrderMetaData;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.PrimaryKeyMetaData;
import org.datanucleus.metadata.PropertyMetaData;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.ValueMetaData;
import org.datanucleus.metadata.VersionMetaData;
import org.datanucleus.util.StringUtils;

/**
 * Helper class that can convert internal metadata into (JDO) XML metadata.
 */
public class JDOXmlMetaDataHelper
{
    public JDOXmlMetaDataHelper()
    {
    }

    /**
     * Method to convert an internal class/interface metadata into the associated JDO XML metadata.
     * @param cmd Metadata for the class/interface
     * @param prefix Prefix for the XML (e.g "    ")
     * @param indent Indent for each block of XML (e.g "    ")
     * @return The XML
     */
    public String getXMLForMetaData(AbstractClassMetaData cmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        boolean intf = (cmd instanceof InterfaceMetaData);

        if (intf)
        {
            str.append(prefix).append("<interface name=\"" + cmd.getName() + "\"");
        }
        else
        {
            str.append(prefix).append("<class name=\"" + cmd.getName() + "\"");
        }

        if (cmd.getIdentityType() != null)
        {
            str.append("\n").append(prefix).append("       identity-type=\"" + cmd.getIdentityType() + "\"");
        }
        if (cmd.getObjectidClass() != null)
        {
            str.append("\n").append(prefix).append("       objectid-class=\"" + cmd.getObjectidClass() + "\"");
        }
        if (!cmd.isRequiresExtent())
        {
            str.append("\n").append(prefix).append("       requires-extent=\"" + cmd.isRequiresExtent() + "\"");
        }
        if (cmd.isEmbeddedOnly())
        {
            str.append("\n").append(prefix).append("       embedded-only=\"" + cmd.isEmbeddedOnly() + "\"");
        }
        if (cmd.getPersistenceModifier() != null)
        {
            str.append("\n").append(prefix).append("       persistence-modifier=\"" + cmd.getPersistenceModifier() + "\"");
        }
        if (cmd.getCatalog() != null)
        {
            str.append("\n").append(prefix).append("       catalog=\"" + cmd.getCatalog() + "\"");
        }
        if (cmd.getSchema() != null)
        {
            str.append("\n").append(prefix).append("       schema=\"" + cmd.getSchema() + "\"");
        }
        if (cmd.getTable() != null)
        {
            str.append("\n").append(prefix).append("       table=\"" + cmd.getTable() + "\"");
        }
        if (cmd.isDetachable())
        {
            str.append("\n").append(prefix).append("       detachable=\"" + cmd.isDetachable() + "\"");
        }
        str.append(">\n");

        // Identity
        if (cmd.getDatastoreIdentityMetaData() != null)
        {
            str.append(getXMLForMetaData(cmd.getDatastoreIdentityMetaData(), prefix + indent,indent));
        }

        // PrimaryKey
        if (cmd.getPrimaryKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(cmd.getPrimaryKeyMetaData(), prefix + indent,indent));
        }

        // Inheritance
        if (cmd.getInheritanceMetaData() != null)
        {
            str.append(getXMLForMetaData(cmd.getInheritanceMetaData(), prefix + indent,indent));
        }

        // Add Version
        if (cmd.getVersionMetaData() != null)
        {
            str.append(getXMLForMetaData(cmd.getVersionMetaData(), prefix + indent,indent));
        }

        // Add joins
        List<JoinMetaData> joins = cmd.getJoinMetaData();
        if (joins != null)
        {
            for (JoinMetaData joinmd : joins)
            {
                str.append(getXMLForMetaData(joinmd, prefix + indent, indent));
            }
        }
        // Add foreign-keys
        List<ForeignKeyMetaData> foreignKeys = cmd.getForeignKeyMetaData();
        if (foreignKeys != null)
        {
            for (ForeignKeyMetaData fkmd : foreignKeys)
            {
                str.append(getXMLForMetaData(fkmd, prefix + indent,indent));
            }
        }
        // Add indexes
        List<IndexMetaData> indexes = cmd.getIndexMetaData();
        if (indexes != null)
        {
            for (IndexMetaData idxmd : indexes)
            {
                str.append(getXMLForMetaData(idxmd, prefix + indent,indent));
            }
        }
        // Add unique constraints
        List<UniqueMetaData> uniqueConstraints = cmd.getUniqueMetaData();
        if (uniqueConstraints != null)
        {
            for (UniqueMetaData unimd : uniqueConstraints)
            {
                str.append(getXMLForMetaData(unimd, prefix + indent,indent));
            }
        }

        // Add members
        int numMembers = cmd.getNoOfMembers();
        for (int i=0;i<numMembers;i++)
        {
            AbstractMemberMetaData mmd = cmd.getMetaDataForMemberAtRelativePosition(i);
            str.append(getXMLForMetaData(mmd, prefix + indent,indent));
        }

        // Add unmapped columns
        List<ColumnMetaData> unmappedColumns = cmd.getUnmappedColumns();
        if (unmappedColumns != null)
        {
            for (int i=0;i<unmappedColumns.size();i++)
            {
                ColumnMetaData col = unmappedColumns.get(i);
                str.append(getXMLForMetaData(col, prefix + indent, indent));
            }
        }

        // Add queries
        QueryMetaData[] queries = cmd.getQueries();
        if (queries != null)
        {
            for (int i=0;i<queries.length;i++)
            {
                QueryMetaData q = queries[i];
                str.append(getXMLForMetaData(q, prefix + indent,indent));
            }
        }

        // Add fetch-groups
        Set<FetchGroupMetaData> fetchGroups = cmd.getFetchGroupMetaData();
        if (fetchGroups != null)
        {
            for (FetchGroupMetaData fgmd : fetchGroups)
            {
                str.append(getXMLForMetaData(fgmd, prefix + indent,indent));
            }
        }

        // Add extensions
        processExtensions(cmd.getExtensions(), str, prefix, indent);

        if (intf)
        {
            str.append(prefix + "</interface>\n");
        }
        else
        {
            str.append(prefix + "</class>\n");
        }
        return str.toString();
    }

    public String getXMLForMetaData(DatastoreIdentityMetaData idmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        if (idmd.getValueStrategy() != null)
        {
            str.append(prefix).append("<datastore-identity strategy=\"" + idmd.getValueStrategy() + "\"");
        }
        else
        {
            str.append(prefix).append("<datastore-identity");
        }

        if (idmd.getColumnName() != null)
        {
            str.append("\n").append(prefix).append("        column=\"" + idmd.getColumnName() + "\"");
        }
        if (idmd.getSequence() != null)
        {
            str.append("\n").append(prefix).append("        sequence=\"" + idmd.getSequence() + "\"");
        }

        if ((idmd.getColumnMetaData() != null) || idmd.getNoOfExtensions() > 0)
        {
            str.append(">\n");

            // Column MetaData
            ColumnMetaData columnMetaData = idmd.getColumnMetaData();
            if (columnMetaData != null)
            {
                str.append(getXMLForMetaData(columnMetaData, prefix + indent,indent));
            }

            // Add extensions
            processExtensions(idmd.getExtensions(), str, prefix, indent);

            str.append(prefix).append("</datastore-identity>\n");
        }
        else
        {
            str.append("/>\n");
        }
        return str.toString();
    }

    public String getXMLForMetaData(ColumnMetaData colmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<column");
        if (colmd.getName() != null)
        {
            str.append(" name=\"" + colmd.getName() + "\"");
        }
        if (colmd.getTarget() != null)
        {
            str.append(" target=\"" + colmd.getTarget() + "\"");
        }
        if (colmd.getTargetMember() != null)
        {
            str.append(" target-field=\"" + colmd.getTargetMember() + "\"");
        }
        if (colmd.getJdbcTypeName() != null)
        {
            str.append(" jdbc-type=\"" + colmd.getJdbcTypeName() + "\"");
        }
        if (colmd.getSqlType() != null)
        {
            str.append(" sql-type=\"" + colmd.getSqlType() + "\"");
        }
        if (colmd.isAllowsNull())
        {
            str.append(" allows-null=\"" + colmd.isAllowsNull() + "\"");
        }
        if (colmd.getLength() != null)
        {
            str.append(" length=\"" + colmd.getLength() + "\"");
        }
        if (colmd.getScale() != null)
        {
            str.append(" scale=\"" + colmd.getScale() + "\"");
        }
        if (colmd.getDefaultValue() != null)
        {
            str.append(" default-value=\"" + colmd.getDefaultValue() + "\"");
        }
        if (colmd.getInsertValue() != null)
        {
            str.append(" insert-value=\"" + colmd.getInsertValue() + "\"");
        }
        if (colmd.getPosition() != null)
        {
            str.append(" position=\"" + colmd.getPosition() + "\"");
        }

        Map<String, String> extensions = colmd.getExtensions();
        if (extensions != null)
        {
            processExtensions(extensions, str, prefix, indent);

            str.append(prefix).append("</column>\n");
        }
        else
        {
            str.append("/>\n");
        }
        return str.toString();
    }

    public String getXMLForMetaData(PrimaryKeyMetaData pkmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<primary-key" + 
            (pkmd.getName() != null ? (" name=\"" + pkmd.getName() + "\"") : "") +
            (pkmd.getColumnName() != null ? (" column=\"" + pkmd.getColumnName() + "\"") : "") +
            ">\n");

        // Add columns
        ColumnMetaData[] columns = pkmd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Add extensions
        processExtensions(pkmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</primary-key>\n");
        return str.toString();
    }

    public String getXMLForMetaData(InheritanceMetaData inhmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<inheritance strategy=\"" + inhmd.getStrategy() + "\">\n");

        // Add join
        if (inhmd.getJoinMetaData() != null)
        {
            str.append(getXMLForMetaData(inhmd.getJoinMetaData(), prefix + indent,indent));
        }

        // Add discriminator
        if (inhmd.getDiscriminatorMetaData() != null)
        {
            str.append(getXMLForMetaData(inhmd.getDiscriminatorMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(inhmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</inheritance>\n");
        return str.toString();
    }

    public String getXMLForMetaData(DiscriminatorMetaData dismd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<discriminator");
        if (dismd.getStrategy() != null)
        {
            str.append(" strategy=\"" + dismd.getStrategy() + "\"");
        }
        if (dismd.getColumnName() != null && dismd.getColumnMetaData() == null)
        {
            str.append(" column=\"" + dismd.getColumnName() + "\"");
        }
        if (dismd.getValue() != null)
        {
            str.append(" value=\"" + dismd.getValue() + "\"");
        }
        if (dismd.getIndexed() != null)
        {
            str.append(" indexed=\"" + dismd.getIndexed() + "\"");
        }
        str.append(">\n");

        // Column MetaData
        if (dismd.getColumnMetaData() != null)
        {
            str.append(getXMLForMetaData(dismd.getColumnMetaData(), prefix + indent,indent));
        }

        // Add index
        if (dismd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(dismd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(dismd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</discriminator>\n");
        return str.toString();
    }

    public String getXMLForMetaData(IndexMetaData idxmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<index unique=\"" + idxmd.isUnique() + "\"");
        if (idxmd.getTable() != null)
        {
            str.append(" table=\"" + idxmd.getTable() + "\"");
        }
        str.append(idxmd.getName() != null ? (" name=\"" + idxmd.getName() + "\">\n") : ">\n");

        String[] memberNames = idxmd.getMemberNames();
        if (memberNames != null)
        {
            for (String memberName : memberNames)
            {
                str.append(prefix).append(indent).append("<field name=\"" + memberName + "\"/>\n");
            }
        }
        String[] columnNames = idxmd.getColumnNames();
        if (columnNames != null)
        {
            for (String columnName : columnNames)
            {
                str.append(prefix).append(indent).append("<column name=\"" + columnName + "\"/>\n");
            }
        }

        // Add extensions
        processExtensions(idxmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</index>\n");
        return str.toString();
    }

    public String getXMLForMetaData(UniqueMetaData unimd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<unique");
        if (unimd.getTable() != null)
        {
            str.append(" table=\"" + unimd.getTable() + "\"");
        }
        if (unimd.isDeferred())
        {
            str.append(" deferred=\"true\"");
        }
        str.append(unimd.getName() != null ? (" name=\"" + unimd.getName() + "\">\n") : ">\n");

        String[] memberNames = unimd.getMemberNames();
        if (memberNames != null)
        {
            for (String memberName : memberNames)
            {
                str.append(prefix).append(indent).append("<field name=\"" + memberName + "\"/>\n");
            }
        }
        String[] columnNames = unimd.getColumnNames();
        if (columnNames != null)
        {
            for (String columnName : columnNames)
            {
                str.append(prefix).append(indent).append("<column name=\"" + columnName + "\"/>\n");
            }
        }

        // Add extensions
        processExtensions(unimd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</unique>\n");
        return str.toString();
    }

    public String getXMLForMetaData(JoinMetaData joinmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<join");
        if (joinmd.getTable() != null)
        {
            str.append(" table=\"" + joinmd.getTable() + "\"");
        }
        if (joinmd.getColumnName() != null)
        {
            str.append(" column=\"" + joinmd.getColumnName() + "\"");
        }
        str.append(" outer=\"" + joinmd.isOuter() + "\"");
        str.append(">\n");

        // Primary-key
        if (joinmd.getPrimaryKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(joinmd.getPrimaryKeyMetaData(), prefix + indent, indent));
        }

        // Add columns
        ColumnMetaData[] columns = joinmd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Foreign-key
        if (joinmd.getForeignKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(joinmd.getForeignKeyMetaData(), prefix + indent, indent));
        }

        // Index
        if (joinmd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(joinmd.getIndexMetaData(), prefix + indent, indent));
        }

        // Unique
        if (joinmd.getUniqueMetaData() != null)
        {
            str.append(getXMLForMetaData(joinmd.getUniqueMetaData(), prefix + indent, indent));
        }

        // Add extensions
        processExtensions(joinmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</join>\n");
        return str.toString();
    }

    public String getXMLForMetaData(ForeignKeyMetaData fkmd, String prefix, String indent)
    {
        if (!StringUtils.isWhitespace(fkmd.getFkDefinition()))
        {
            return "<foreign-key name=\"" + fkmd.getName() + "\" definition=\"" + fkmd.getFkDefinition() + "\" definition-applies="+ fkmd.getFkDefinitionApplies() + "/>";
        }

        // Field needs outputting so generate metadata
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<foreign-key deferred=\"" + fkmd.isDeferred() + "\"\n");
        str.append(prefix).append("       unique=\"" + fkmd.isUnique() + "\"");
        if (fkmd.getUpdateAction() != null)
        {
            str.append("\n").append(prefix).append("       update-action=\"" + fkmd.getUpdateAction() + "\"");
        }
        if (fkmd.getDeleteAction() != null)
        {
            str.append("\n").append(prefix).append("       delete-action=\"" + fkmd.getDeleteAction() + "\"");
        }
        if (fkmd.getTable() != null)
        {
            str.append("\n").append(prefix).append("       table=\"" + fkmd.getTable() + "\"");
        }
        if (fkmd.getName() != null)
        {
            str.append("\n").append(prefix).append("       name=\"" + fkmd.getName() + "\"");
        }
        str.append(">\n");

        String[] memberNames = fkmd.getMemberNames();
        if (memberNames != null)
        {
            for (String memberName : memberNames)
            {
                str.append(prefix).append(indent).append("<field name=\"" + memberName + "\"/>\n");
            }
        }
        ColumnMetaData[] columns = fkmd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Add extensions
        processExtensions(fkmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</foreign-key>\n");
        return str.toString();
    }

    public String getXMLForMetaData(VersionMetaData vermd, String prefix, String indent)
    {
        // Field needs outputting so generate metadata
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<version " + 
            (vermd.getStrategy()!=null ? ("strategy=\"" + vermd.getStrategy() + "\"") : "") +
            (vermd.getIndexed() != null ? (" indexed=\"" + vermd.getIndexed() + "\"") : ""));
        if (vermd.getColumnName() != null && vermd.getColumnMetaData() == null)
        {
            str.append(" column=\"" + vermd.getColumnName() + "\"");
        }
        str.append(">\n");

        // Column MetaData
        if (vermd.getColumnMetaData() != null)
        {
            str.append(getXMLForMetaData(vermd.getColumnMetaData(), prefix + indent,indent));
        }

        // Add index
        if (vermd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(vermd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(vermd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</version>\n");
        return str.toString();
    }

    public String getXMLForMetaData(QueryMetaData qmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<query name=\"" + qmd.getName() + "\"\n");
        str.append(prefix).append("       language=\"" + qmd.getLanguage() + "\"\n");
        if (qmd.isUnique())
        {
            str.append(prefix).append("       unique=\"true\"\n");
        }
        if (qmd.getResultClass() != null)
        {
            str.append(prefix).append("       result-class=\"" + qmd.getResultClass() + "\"\n");
        }
        if (qmd.getFetchPlanName() != null)
        {
            str.append(prefix).append("       fetch-plan=\"" + qmd.getFetchPlanName() + "\"\n");
        }
        str.append(prefix).append("       unmodifiable=\"" + qmd.isUnmodifiable() + "\">\n");
        str.append(prefix).append(qmd.getQuery()).append("\n");

        // Add extensions
        processExtensions(qmd.getExtensions(), str, prefix, indent);

        str.append(prefix + "</query>\n");
        return str.toString();
    }

    public String getXMLForMetaData(FetchGroupMetaData fgmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<fetch-group name=\"" + fgmd.getName() + "\">\n");

        // Add fetch-groups
        Set<FetchGroupMetaData> fetchGroups = fgmd.getFetchGroups();
        if (fetchGroups != null)
        {
            for (FetchGroupMetaData subfgmd : fetchGroups)
            {
                str.append(getXMLForMetaData(subfgmd, prefix + indent, indent));
            }
        }

        // Add fields
        Set<FetchGroupMemberMetaData> members = fgmd.getMembers();
        if (members != null)
        {
            for (FetchGroupMemberMetaData fgmmd : members)
            {
                str.append(getXMLForMetaData(fgmmd, prefix + indent, indent));
            }
        }

        str.append(prefix + "</fetch-group>\n");
        return str.toString();
    }

    public String getXMLForMetaData(FetchGroupMemberMetaData fgmmd, String prefix, String indent)
    {
        StringBuilder sb = new StringBuilder();
        if (fgmmd.isProperty())
        {
            sb.append(prefix).append("<property ");
        }
        else
        {
            sb.append(prefix).append("<field ");
        }

        if (fgmmd.getRecursionDepth() != 1)
        {
            sb.append("name=\"" + fgmmd.getName() + "\" recursion-depth=\"" + fgmmd.getRecursionDepth() + "\"/>\n");
        }
        else
        {
            sb.append("name=\"" + fgmmd.getName() + "\"/>\n");
        }
        return sb.toString();
    }

    public String getXMLForMetaData(AbstractMemberMetaData mmd, String prefix, String indent)
    {
        if (mmd.isStatic() || mmd.isFinal())
        {
            // If this field is static or final, don't bother with MetaData since we will ignore it anyway.
            return "";
        }

        boolean field = (mmd instanceof FieldMetaData);

        // Field needs outputting so generate metadata
        StringBuilder str = new StringBuilder();
        if (field)
        {
            str.append(prefix).append("<field name=\"" + mmd.getName() + "\"");
        }
        else
        {
            str.append(prefix).append("<property name=\"" + mmd.getName() + "\"");
        }

        if (mmd.getPersistenceModifier() != null && !StringUtils.isWhitespace(mmd.getPersistenceModifier().toString()))
        {
            str.append("\n").append(prefix).append("       persistence-modifier=\"" + mmd.getPersistenceModifier() + "\"");
        }
        if (!StringUtils.isWhitespace(mmd.getTable()))
        {
            str.append("\n").append(prefix).append("       table=\"" + mmd.getTable() + "\"");
        }
        if (mmd.isPrimaryKey())
        {
            str.append("\n").append(prefix).append("       primary-key=\"" + mmd.isPrimaryKey() + "\"");
        }
        str.append("\n").append(prefix).append("       null-value=\"" + mmd.getNullValue() + "\"");
        if (mmd.isDefaultFetchGroup())
        {
            str.append("\n").append(prefix).append("       default-fetch-group=\"" + mmd.isDefaultFetchGroup() + "\"");
        }
        if (mmd.isEmbedded())
        {
            str.append("\n").append(prefix).append("       embedded=\"" + mmd.isEmbedded() + "\"");
        }
        if (mmd.isSerialized())
        {
            str.append("\n").append(prefix).append("       serialized=\"" + mmd.isSerialized() + "\"");
        }
        if (mmd.isDependent())
        {
            str.append("\n").append(prefix).append("       dependent=\"" + mmd.isDependent() + "\"");
        }
        if (mmd.getMappedBy() != null)
        {
            str.append("\n").append(prefix).append("       mapped-by=\"" + mmd.getMappedBy() + "\"");
        }
        String[] fieldTypes = mmd.getFieldTypes();
        if (fieldTypes != null)
        {
            str.append("\n").append(prefix).append("       field-type=\"");
            for (int i=0;i<fieldTypes.length;i++)
            {
                str.append(fieldTypes[i]);
            }
            str.append("\"");
        }
        if (!StringUtils.isWhitespace(mmd.getLoadFetchGroup()))
        {
            str.append("\n").append(prefix).append("       load-fetch-group=\"" + mmd.getLoadFetchGroup() + "\"");
        }
        if (mmd.getRecursionDepth() != null && mmd.getRecursionDepth() != 1)
        {
            str.append("\n").append(prefix).append("       recursion-depth=\"" + mmd.getRecursionDepth() + "\"");
        }
        if (mmd.getValueStrategy() != null)
        {
            str.append("\n").append(prefix).append("       value-strategy=\"" + mmd.getValueStrategy() + "\"");
        }
        if (mmd.getSequence() != null)
        {
            str.append("\n").append(prefix).append("       sequence=\"" + mmd.getSequence() + "\"");
        }
        if (!field)
        {
            PropertyMetaData propmd = (PropertyMetaData)mmd;
            if (propmd.getFieldName() != null)
            {
                str.append("\n").append(prefix).append("       field-name=\"" + propmd.getFieldName() + "\"");
            }
        }
        if (mmd.getIndexMetaData() == null && mmd.getIndexed() != null)
        {
            str.append("\n").append(prefix).append("       indexed=\"" + mmd.getIndexed() + "\"");
        }
        if (mmd.getUniqueMetaData() == null)
        {
            str.append("\n").append(prefix).append("       unique=\"" + mmd.isUnique() + "\"");
        }
        str.append(">\n");

        // Add field containers
        ContainerMetaData contmd = mmd.getContainer();
        if (contmd != null)
        {
            if (contmd instanceof CollectionMetaData)
            {
                CollectionMetaData c = (CollectionMetaData)contmd;
                str.append(getXMLForMetaData(c, prefix + indent,indent));
            }
            else if (contmd instanceof ArrayMetaData)
            {
                ArrayMetaData c = (ArrayMetaData)contmd;
                str.append(getXMLForMetaData(c, prefix + indent,indent));
            }
            else if (contmd instanceof MapMetaData)
            {
                MapMetaData c = (MapMetaData)contmd;
                str.append(getXMLForMetaData(c, prefix + indent,indent));
            }
        }

        // Add columns
        ColumnMetaData[] columnMetaData = mmd.getColumnMetaData();
        if (columnMetaData != null)
        {
            for (int i=0; i<columnMetaData.length; i++)
            {
                str.append(getXMLForMetaData(columnMetaData[i], prefix + indent,indent));
            }
        }

        // Add join
        if (mmd.getJoinMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getJoinMetaData(), prefix + indent,indent));
        }

        // Add element
        if (mmd.getElementMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getElementMetaData(), prefix + indent,indent));
        }

        // Add key
        if (mmd.getKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getKeyMetaData(), prefix + indent,indent));
        }

        // Add value
        if (mmd.getValueMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getValueMetaData(), prefix + indent,indent));
        }

        // TODO Add fetch-groups

        // Add order
        if (mmd.getOrderMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getOrderMetaData(), prefix + indent,indent));
        }

        // Add embedded
        if (mmd.getEmbeddedMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getEmbeddedMetaData(), prefix + indent,indent));
        }

        // Add index
        if (mmd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add unique
        if (mmd.getUniqueMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getUniqueMetaData(), prefix + indent,indent));
        }

        // Add foreign-key
        if (mmd.getForeignKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(mmd.getForeignKeyMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(mmd.getExtensions(), str, prefix, indent);

        if (field)
        {
            str.append(prefix).append("</field>\n");
        }
        else
        {
            str.append(prefix).append("</property>\n");
        }
        return str.toString();
    }

    public String getXMLForMetaData(EmbeddedMetaData embmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<embedded");
        if (embmd.getOwnerMember() != null)
        {
            str.append(" owner-field=\"" + embmd.getOwnerMember() + "\"");
        }
        if (embmd.getNullIndicatorColumn() != null)
        {
            str.append(" null-indicator-column=\"" + embmd.getNullIndicatorColumn() + "\"");
        }
        if (embmd.getNullIndicatorValue() != null)
        {
            str.append(" null-indicator-value=\"" + embmd.getNullIndicatorValue() + "\"");
        }
        str.append(">\n");

        if (embmd.getDiscriminatorMetaData() != null)
        {
            str.append(getXMLForMetaData(embmd.getDiscriminatorMetaData(), prefix+indent, indent));
        }

        // Add members
        List<AbstractMemberMetaData> members = embmd.getMemberMetaData();
        if (members != null)
        {
            for (AbstractMemberMetaData member : members)
            {
                str.append(getXMLForMetaData(member, prefix + indent,indent));
            }
        }

        // Add extensions
        processExtensions(embmd.getExtensions(), str, prefix, indent);

        str.append(prefix + "</embedded>\n");
        return str.toString();
    }

    public String getXMLForMetaData(ElementMetaData elemmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<element");
        if (elemmd.getMappedBy() != null)
        {
            str.append(" mapped-by=\"" + elemmd.getMappedBy() + "\"");
        }
        if (!StringUtils.isWhitespace(elemmd.getTable()))
        {
            str.append(" table=\"" + elemmd.getTable() + "\"");
        }
        if (!StringUtils.isWhitespace(elemmd.getColumnName()))
        {
            str.append(" column=\"" + elemmd.getColumnName() + "\"");
        }
        str.append(">\n");

        // Add columns
        ColumnMetaData[] columns = elemmd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Add index metadata
        if (elemmd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(elemmd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add unique metadata
        if (elemmd.getUniqueMetaData() != null)
        {
            str.append(getXMLForMetaData(elemmd.getUniqueMetaData(), prefix + indent,indent));
        }

        // Add embedded metadata
        if (elemmd.getEmbeddedMetaData() != null)
        {
            str.append(getXMLForMetaData(elemmd.getEmbeddedMetaData(), prefix + indent,indent));
        }

        // Add foreign-key metadata
        if (elemmd.getForeignKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(elemmd.getForeignKeyMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(elemmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</element>\n");
        return str.toString();
    }

    public String getXMLForMetaData(KeyMetaData keymd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<key");
        if (keymd.getMappedBy() != null)
        {
            str.append(" mapped-by=\"" + keymd.getMappedBy() + "\"");
        }
        if (!StringUtils.isWhitespace(keymd.getTable()))
        {
            str.append(" table=\"" + keymd.getTable() + "\"");
        }
        if (!StringUtils.isWhitespace(keymd.getColumnName()))
        {
            str.append(" column=\"" + keymd.getColumnName() + "\"");
        }
        str.append(">\n");

        // Add columns
        ColumnMetaData[] columns = keymd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Add index metadata
        if (keymd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(keymd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add unique metadata
        if (keymd.getUniqueMetaData() != null)
        {
            str.append(getXMLForMetaData(keymd.getUniqueMetaData(), prefix + indent,indent));
        }

        // Add embedded metadata
        if (keymd.getEmbeddedMetaData() != null)
        {
            str.append(getXMLForMetaData(keymd.getEmbeddedMetaData(), prefix + indent,indent));
        }

        // Add foreign-key metadata
        if (keymd.getForeignKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(keymd.getForeignKeyMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(keymd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</key>\n");
        return str.toString();
    }

    public String getXMLForMetaData(ValueMetaData valmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<value");
        if (valmd.getMappedBy() != null)
        {
            str.append(" mapped-by=\"" + valmd.getMappedBy() + "\"");
        }
        if (!StringUtils.isWhitespace(valmd.getTable()))
        {
            str.append(" table=\"" + valmd.getTable() + "\"");
        }
        if (!StringUtils.isWhitespace(valmd.getColumnName()))
        {
            str.append(" column=\"" + valmd.getColumnName() + "\"");
        }
        str.append(">\n");

        // Add columns
        ColumnMetaData[] columns = valmd.getColumnMetaData();
        if (columns != null)
        {
            for (ColumnMetaData colmd : columns)
            {
                str.append(getXMLForMetaData(colmd, prefix + indent,indent));
            }
        }

        // Add index metadata
        if (valmd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(valmd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add unique metadata
        if (valmd.getUniqueMetaData() != null)
        {
            str.append(getXMLForMetaData(valmd.getUniqueMetaData(), prefix + indent,indent));
        }

        // Add embedded metadata
        if (valmd.getEmbeddedMetaData() != null)
        {
            str.append(getXMLForMetaData(valmd.getEmbeddedMetaData(), prefix + indent,indent));
        }

        // Add foreign-key metadata
        if (valmd.getForeignKeyMetaData() != null)
        {
            str.append(getXMLForMetaData(valmd.getForeignKeyMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(valmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</value>\n");
        return str.toString();
    }

    public String getXMLForMetaData(OrderMetaData ordermd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<order");
        if (ordermd.getColumnName() != null)
        {
            str.append(" column=\"" + ordermd.getColumnName() + "\"");
        }
        if (ordermd.getIndexed() != null)
        {
            str.append(" indexed=\"" + ordermd.getIndexed() + "\"");
        }
        if (ordermd.getMappedBy() != null)
        {
            str.append(" mapped-by=\"" + ordermd.getMappedBy() + "\"");
        }
        str.append(">\n");

        // Add columns
        ColumnMetaData[] columns = ordermd.getColumnMetaData();
        if (columns != null)
        {
            for (int i=0; i<columns.length; i++)
            {
                str.append(getXMLForMetaData(columns[i], prefix + indent,indent));
            }
        }

        // Add index
        if (ordermd.getIndexMetaData() != null)
        {
            str.append(getXMLForMetaData(ordermd.getIndexMetaData(), prefix + indent,indent));
        }

        // Add extensions
        processExtensions(ordermd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</order>\n");
        return str.toString();
    }

    public String getXMLForMetaData(CollectionMetaData collmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<collection element-type=\"").append(collmd.getElementType()).append("\"");
        if (collmd.isEmbeddedElement())
        {
            str.append(" embedded-element=\"").append(collmd.isEmbeddedElement()).append("\"");
        }
        if (collmd.isDependentElement())
        {
            str.append(" dependent-element=\"").append(collmd.isDependentElement()).append("\"");
        }
        if (collmd.isSerializedElement())
        {
            str.append(" serialized-element=\"").append(collmd.isSerializedElement()).append("\"");
        }
        str.append(">\n");

        // Add extensions
        processExtensions(collmd.getExtensions(), str, prefix, indent);
 
        str.append(prefix).append("</collection>\n");
        return str.toString();
    }

    public String getXMLForMetaData(MapMetaData mapmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<map key-type=\"").append(mapmd.getKeyType()).append("\" value-type=\"").append(mapmd.getValueType()).append("\"");
        if (mapmd.isEmbeddedKey())
        {
            str.append(" embedded-key=\"").append(mapmd.isEmbeddedKey()).append("\"");
        }
        if (mapmd.isEmbeddedValue())
        {
            str.append(" embedded-value=\"").append(mapmd.isEmbeddedValue()).append("\"");
        }
        if (mapmd.isDependentKey())
        {
            str.append(" dependent-key=\"").append(mapmd.isDependentKey()).append("\"");
        }
        if (mapmd.isDependentValue())
        {
            str.append(" dependent-value=\"").append(mapmd.isDependentValue()).append("\"");
        }
        if (mapmd.isSerializedKey())
        {
            str.append(" serialized-key=\"").append(mapmd.isSerializedKey()).append("\"");
        }
        if (mapmd.isSerializedValue())
        {
            str.append(" serialized-value=\"").append(mapmd.isSerializedValue()).append("\"");
        }
        str.append(">\n");

        // Add extensions
        processExtensions(mapmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</map>\n");
        return str.toString();
    }

    public String getXMLForMetaData(ArrayMetaData arrmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<array");
        if (arrmd.getElementType() != null)
        {
            str.append(" element-type=\"").append(arrmd.getElementType()).append("\"");
        }
        if (arrmd.isEmbeddedElement())
        {
            str.append(" embedded-element=\"").append(arrmd.isEmbeddedElement()).append("\"");
        }
        if (arrmd.isSerializedElement())
        {
            str.append(" serialized-element=\"").append(arrmd.isSerializedElement()).append("\"");
        }
        if (arrmd.isDependentElement())
        {
            str.append(" dependent-element=\"").append(arrmd.isDependentElement()).append("\"");
        }

        Map<String, String> extensions = arrmd.getExtensions();
        if (extensions != null)
        {
            str.append(">\n");

            processExtensions(extensions, str, prefix, indent);

            str.append(prefix).append("</array>\n");
        }
        else
        {
            str.append(prefix).append("/>\n");
        }
        return str.toString();
    }

    public String getXMLForMetaData(PackageMetaData pmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<package name=\"" + pmd.getName() + "\"");
        if (pmd.getCatalog() != null)
        {
            str.append(" catalog=\"" + pmd.getCatalog() + "\"");
        }
        if (pmd.getSchema() != null)
        {
            str.append(" schema=\"" + pmd.getSchema() + "\"");
        }
        str.append(">\n");

        // Add interfaces
        if (pmd.getNoOfInterfaces() > 0)
        {
            for (int i=0;i<pmd.getNoOfInterfaces();i++)
            {
                InterfaceMetaData imd = pmd.getInterface(i);
                str.append(getXMLForMetaData(imd, prefix + indent,indent));
            }
        }

        // Add classes
        if (pmd.getNoOfClasses() > 0)
        {
            for (int i=0;i<pmd.getNoOfClasses();i++)
            {
                ClassMetaData cmd = pmd.getClass(i);
                str.append(getXMLForMetaData(cmd, prefix + indent,indent));
            }
        }

        // Add sequences
        SequenceMetaData[] seqmds = pmd.getSequences();
        if (seqmds != null)
        {
            for (SequenceMetaData seqmd : seqmds)
            {
                str.append(getXMLForMetaData(seqmd, prefix + indent,indent));
            }
        }

        // Add extensions
        processExtensions(pmd.getExtensions(), str, prefix, indent);

        str.append(prefix).append("</package>\n");
        return str.toString();
    }

    public String getXMLForMetaData(SequenceMetaData seqmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<sequence name=\"" + seqmd.getName() + "\"");
        if (seqmd.getDatastoreSequence() != null)
        {
            str.append(" datastore-sequence=\"" + seqmd.getDatastoreSequence() + "\"");
        }
        if (seqmd.getFactoryClass() != null)
        {
            str.append(" factory-class=\"" + seqmd.getFactoryClass() + "\"");
        }
        if (seqmd.getInitialValue() >= 0)
        {
            str.append(" initial-value=\"" + seqmd.getInitialValue() + "\"");
        }
        if (seqmd.getAllocationSize() >= 0)
        {
            str.append(" allocation-size=\"" + seqmd.getAllocationSize() + "\"");
        }
        if (seqmd.getStrategy() != null)
        {
            str.append(" strategy=\"" + seqmd.getStrategy() + "\">");
        }
        str.append(">\n");

        // Add extensions
        processExtensions(seqmd.getExtensions(), str, prefix, indent);

        str.append(prefix + "</sequence>\n");
        return str.toString();
    }

    public String getXMLForMetaData(FileMetaData filemd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<jdo");
        if (filemd.getCatalog() != null)
        {
            str.append(" catalog=\"" + filemd.getCatalog() + "\"");
        }
        if (filemd.getSchema() != null)
        {
            str.append(" schema=\"" + filemd.getSchema() + "\"");
        }
        str.append(">\n");

        // Add packages
        if (filemd.getNoOfPackages() > 0)
        {
            for (int i=0;i<filemd.getNoOfPackages();i++)
            {
                PackageMetaData pmd = filemd.getPackage(i);
                str.append(getXMLForMetaData(pmd, prefix+indent, indent));
            }
        }

        // Add queries
        QueryMetaData[] queries = filemd.getQueries();
        if (queries != null)
        {
            for (QueryMetaData qmd : queries)
            {
                str.append(getXMLForMetaData(qmd, prefix+indent, indent));
            }
        }

        // Add fetch plans
        FetchPlanMetaData[] fetchPlans = filemd.getFetchPlans();
        if (fetchPlans != null)
        {
            for (FetchPlanMetaData fpmd : fetchPlans)
            {
                str.append(getXMLForMetaData(fpmd, prefix+indent, indent));
            }
        }

        // Add extensions
        processExtensions(filemd.getExtensions(), str, prefix, indent);

        str.append("</jdo>");
        return str.toString();
    }

    protected void processExtensions(Map<String, String> extensions, StringBuilder str, String prefix, String indent)
    {
        if (extensions != null)
        {
            Iterator<Entry<String, String>> entryIter = extensions.entrySet().iterator();
            while (entryIter.hasNext())
            {
                Entry<String, String> entry = entryIter.next();
                str.append(getXMLForMetaData(entry.getKey(), entry.getValue(), prefix+indent, indent)).append("\n");
            }
        }
    }

    public String getXMLForMetaData(FetchPlanMetaData fpmd, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<fetch-plan name=\"" + fpmd.getName() + "\"" + 
            " max-fetch-depth=\"" + fpmd.getMaxFetchDepth() + "\"" +
            " fetch-size=\"" + fpmd.getFetchSize() + "\">\n");

        // Add fetch-groups
        FetchGroupMetaData[] fetchGroups = fpmd.getFetchGroupMetaData();
        if (fetchGroups != null)
        {
            for (FetchGroupMetaData fgmd : fetchGroups)
            {
                str.append(getXMLForMetaData(fgmd, prefix + indent, indent));
            }
        }

        str.append(prefix + "</fetch-plan>\n");
        return str.toString();
    }

    public String getXMLForMetaData(String key, String value, String prefix, String indent)
    {
        StringBuilder str = new StringBuilder();
        str.append(prefix).append("<extension vendor-name=\"").append(MetaData.VENDOR_NAME).append("\" ")
            .append("key=\"").append(key).append("\" ")
            .append("value=\"").append(value).append("\"/>");
        return str.toString();
    }
}