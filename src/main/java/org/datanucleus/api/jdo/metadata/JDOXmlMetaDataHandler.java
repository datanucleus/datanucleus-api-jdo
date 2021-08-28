/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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

import javax.jdo.AttributeConverter;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOQuery;
import org.datanucleus.api.jdo.JDOTypeConverter;
import org.datanucleus.api.jdo.JDOTypeConverterUtils;
import org.datanucleus.api.jdo.DataNucleusHelperJDO;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractElementMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ArrayMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.ClassPersistenceModifier;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.ElementMetaData;
import org.datanucleus.metadata.EmbeddedMetaData;
import org.datanucleus.metadata.FetchGroupMemberMetaData;
import org.datanucleus.metadata.FetchGroupMetaData;
import org.datanucleus.metadata.FetchPlanMetaData;
import org.datanucleus.metadata.FieldMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.ForeignKeyAction;
import org.datanucleus.metadata.ForeignKeyMetaData;
import org.datanucleus.metadata.IdentityMetaData;
import org.datanucleus.metadata.ValueGenerationStrategy;
import org.datanucleus.metadata.IdentityType;
import org.datanucleus.metadata.ImplementsMetaData;
import org.datanucleus.metadata.IndexMetaData;
import org.datanucleus.metadata.IndexedValue;
import org.datanucleus.metadata.InheritanceMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.InvalidClassMetaDataException;
import org.datanucleus.metadata.InvalidMetaDataException;
import org.datanucleus.metadata.JoinMetaData;
import org.datanucleus.metadata.KeyMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.metadata.MetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.MetaDataUtils;
import org.datanucleus.metadata.MetadataFileType;
import org.datanucleus.metadata.MultitenancyMetaData;
import org.datanucleus.metadata.NullValue;
import org.datanucleus.metadata.OrderMetaData;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.PrimaryKeyMetaData;
import org.datanucleus.metadata.PropertyMetaData;
import org.datanucleus.metadata.QueryLanguage;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.metadata.UniqueMetaData;
import org.datanucleus.metadata.ValueMetaData;
import org.datanucleus.metadata.VersionMetaData;
import org.datanucleus.metadata.xml.AbstractXmlMetaDataHandler;
import org.datanucleus.store.types.TypeManager;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Parser handler for JDO XML MetaData.
 * Implements DefaultHandler and handles the extracting of MetaData for JDO from the XML elements/attributes. 
 * This class simply constructs the MetaData representation mirroring what is in the XML MetaData file. 
 * It has no knowledge of the class(es) that it represents, simply the information in the XML MetaData file. 
 * The knowledge of the classes is imposed on the representation at a later stage where necessary.
 * <P>
 * Operates the parse process using a Stack. XML MetaData components are added to the stack as they are encountered and created. 
 * They are then popped off the stack when the end element is encountered.
 * </P>
 */
public class JDOXmlMetaDataHandler extends AbstractXmlMetaDataHandler
{
    /**
     * Constructor. Protected to prevent instantiation.
     * @param mgr the metadata manager
     * @param filename The name of the file to parse
     * @param resolver Entity Resolver to use (null if not available)
     */
    public JDOXmlMetaDataHandler(MetaDataManager mgr, String filename, EntityResolver resolver)
    {
        super(mgr, filename, resolver);
        metadata = new FileMetaData(filename);
        pushStack(metadata);
    }

    /**
     * Utility to create a new class component.
     * @param pmd The parent PackageMetaData
     * @param attrs The attributes
     * @return The ClassMetaData
     */
    protected ClassMetaData newClassObject(PackageMetaData pmd, Attributes attrs)
    {
        String name = getAttr(attrs, "name");
        if (StringUtils.isWhitespace(name))
        {
            throw new InvalidClassMetaDataException("044061", pmd.getName());
        }
        ClassMetaData cmd = new ClassMetaData(pmd, name);
        cmd.setTable(getAttr(attrs, "table"));
        cmd.setCatalog(getAttr(attrs, "catalog"));
        cmd.setSchema(getAttr(attrs, "schema"));
        cmd.setRequiresExtent(getAttr(attrs, "requires-extent"));
        String detachableStr = getAttr(attrs, "detachable");
        if (mmgr.getNucleusContext().getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALWAYS_DETACHABLE))
        {
            cmd.setDetachable(true);
        }
        else
        {
            cmd.setDetachable(detachableStr);
        }
        String objIdCls = getAttr(attrs, "objectid-class");
        if (!StringUtils.isWhitespace(objIdCls))
        {
            cmd.setObjectIdClass(DataNucleusHelperJDO.getObjectIdClassForInputIdClass(objIdCls));
        }
        cmd.setEmbeddedOnly(getAttr(attrs, "embedded-only"));
        cmd.setPersistenceModifier(ClassPersistenceModifier.getClassPersistenceModifier(getAttr(attrs, "persistence-modifier")));
        cmd.setIdentityType(IdentityType.getIdentityType(getAttr(attrs, "identity-type")));
        String cacheableAttr = getAttr(attrs, "cacheable");
        if (cacheableAttr != null)
        {
            cmd.setCacheable(cacheableAttr.equalsIgnoreCase("false") ? false : true);
        }
        String serializeReadAttr = getAttr(attrs, "serialize-read");
        if (serializeReadAttr != null)
        {
            cmd.setSerializeRead(serializeReadAttr.equalsIgnoreCase("true") ? true : false);
        }

        String converterAttr = getAttr(attrs, "converter");
        String disableConverterAttr = getAttr(attrs, "use-default-conversion");
        if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
        {
            // TODO Process disable-converter
        }
        else if (!StringUtils.isWhitespace(converterAttr))
        {
            // TODO Process converter
        }

        return cmd;
    }

    /**
     * Utility to create a new interface component.
     * @param pmd The parent PackageMetaData
     * @param attrs The attributes
     * @return The InterfaceMetaData
     */
    protected InterfaceMetaData newInterfaceObject(PackageMetaData pmd, Attributes attrs)
    {
        String name = getAttr(attrs, "name");
        if (StringUtils.isWhitespace(name))
        {
            throw new InvalidClassMetaDataException("044061", pmd.getName());
        }
        InterfaceMetaData imd = new InterfaceMetaData(pmd, name);
        imd.setTable(getAttr(attrs, "table"));
        imd.setCatalog(getAttr(attrs, "catalog"));
        imd.setSchema(getAttr(attrs, "schema"));
        String detachableStr = getAttr(attrs, "detachable");
        if (mmgr.getNucleusContext().getConfiguration().getBooleanProperty(PropertyNames.PROPERTY_METADATA_ALWAYS_DETACHABLE))
        {
            imd.setDetachable(true);
        }
        else
        {
            imd.setDetachable(detachableStr);
        }
        imd.setRequiresExtent(getAttr(attrs, "requires-extent"));
        String objIdCls = getAttr(attrs, "objectid-class");
        if (!StringUtils.isWhitespace(objIdCls))
        {
            imd.setObjectIdClass(DataNucleusHelperJDO.getObjectIdClassForInputIdClass(objIdCls));
        }
        imd.setEmbeddedOnly(getAttr(attrs, "embedded-only"));
        imd.setIdentityType(IdentityType.getIdentityType(getAttr(attrs, "identity-type")));
        imd.setPersistenceModifier(ClassPersistenceModifier.PERSISTENCE_CAPABLE);
        String cacheableAttr = getAttr(attrs, "cacheable");
        if (cacheableAttr != null)
        {
            imd.setCacheable(cacheableAttr.equalsIgnoreCase("false") ? false : true);
        }

        String converterAttr = getAttr(attrs, "converter");
        String disableConverterAttr = getAttr(attrs, "use-default-conversion");
        if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
        {
            // TODO Process disable-converter
        }
        else if (!StringUtils.isWhitespace(converterAttr))
        {
            // TODO Process converter
        }

        return imd;
    }

    /**
     * Utility to create a new field component.
     * @param md The parent MetaData
     * @param attrs The attributes
     * @return The FieldMetaData
     */
    protected FieldMetaData newFieldObject(MetaData md, Attributes attrs)
    {
        FieldMetaData fmd = new FieldMetaData(md, getAttr(attrs, "name"));
        String modStr = getAttr(attrs, "persistence-modifier");
        FieldPersistenceModifier modifier = FieldPersistenceModifier.getFieldPersistenceModifier(modStr);
        if (modifier != null)
        {
            fmd.setPersistenceModifier(modifier);
        }
        fmd.setDeleteAction(getAttr(attrs, "delete-action"));
        String pkStr = getAttr(attrs, "primary-key");
        if (!StringUtils.isWhitespace(pkStr))
        {
            fmd.setPrimaryKey(Boolean.valueOf(pkStr));
        }
        String dfgStr = getAttr(attrs, "default-fetch-group");
        if (!StringUtils.isWhitespace(dfgStr))
        {
            fmd.setDefaultFetchGroup(Boolean.valueOf(dfgStr));
        }
        String embStr = getAttr(attrs, "embedded");
        if (!StringUtils.isWhitespace(embStr))
        {
            fmd.setEmbedded(Boolean.valueOf(embStr));
        }
        String serStr = getAttr(attrs, "serialized");
        if (!StringUtils.isWhitespace(serStr))
        {
            fmd.setSerialised(Boolean.valueOf(serStr));
        }
        String depStr = getAttr(attrs, "dependent");
        if (!StringUtils.isWhitespace(depStr))
        {
            fmd.setDependent(Boolean.valueOf(depStr));
        }
        fmd.setNullValue(NullValue.getNullValue(getAttr(attrs, "null-value")));
        fmd.setMappedBy(getAttr(attrs, "mapped-by"));
        fmd.setColumn(getAttr(attrs, "column"));
        fmd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
        fmd.setUnique(getAttr(attrs, "unique"));
        fmd.setTable(getAttr(attrs, "table"));
        fmd.setLoadFetchGroup(getAttr(attrs, "load-fetch-group"));
        fmd.setRecursionDepth(getAttr(attrs, "recursion-depth"));
        fmd.setValueStrategy(getAttr(attrs, "value-strategy"));
        fmd.setSequence(getAttr(attrs, "sequence"));
        fmd.setFieldTypes(getAttr(attrs, "field-type"));
        String cacheableAttr = getAttr(attrs, "cacheable");
        if (cacheableAttr != null)
        {
            fmd.setCacheable(cacheableAttr.equalsIgnoreCase("false") ? false : true);
        }

        String converterAttr = getAttr(attrs, "converter");
        String disableConverterAttr = getAttr(attrs, "use-default-conversion");
        if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
        {
            fmd.setTypeConverterDisabled();
        }
        else if (!StringUtils.isWhitespace(converterAttr))
        {
            TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
            ClassLoaderResolver clr = mmgr.getNucleusContext().getClassLoaderResolver(null);
            Class converterCls = clr.classForName(converterAttr);
            if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
            {
                // Not yet cached an instance of this converter so create one
                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, null); // TODO member type
                Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                // Register the TypeConverter under the name of the AttributeConverter class
                JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                typeMgr.registerConverter(converterAttr, typeConv, attrType, dbType, false, null);
            }

            fmd.setTypeConverterName(converterAttr);
        }

        return fmd;
    }

    /**
     * Utility to create a new property component.
     * @param md The parent MetaData
     * @param attrs The attributes
     * @return The PropertyMetaData
     */
    protected PropertyMetaData newPropertyObject(MetaData md, Attributes attrs)
    {
        PropertyMetaData pmd = new PropertyMetaData(md, getAttr(attrs, "name"));
        String modStr = getAttr(attrs, "persistence-modifier");
        FieldPersistenceModifier modifier = FieldPersistenceModifier.getFieldPersistenceModifier(modStr);
        if (modifier != null)
        {
            pmd.setPersistenceModifier(modifier);
        }
        pmd.setDeleteAction(getAttr(attrs, "delete-action"));
        String pkStr = getAttr(attrs, "primary-key");
        if (!StringUtils.isWhitespace(pkStr))
        {
            pmd.setPrimaryKey(Boolean.valueOf(pkStr));
        }
        String dfgStr = getAttr(attrs, "default-fetch-group");
        if (!StringUtils.isWhitespace(dfgStr))
        {
            pmd.setDefaultFetchGroup(Boolean.valueOf(dfgStr));
        }
        String embStr = getAttr(attrs, "embedded");
        if (!StringUtils.isWhitespace(embStr))
        {
            pmd.setEmbedded(Boolean.valueOf(embStr));
        }
        String serStr = getAttr(attrs, "serialized");
        if (!StringUtils.isWhitespace(serStr))
        {
            pmd.setSerialised(Boolean.valueOf(serStr));
        }
        String depStr = getAttr(attrs, "dependent");
        if (!StringUtils.isWhitespace(depStr))
        {
            pmd.setDependent(Boolean.valueOf(depStr));
        }
        pmd.setNullValue(NullValue.getNullValue(getAttr(attrs, "null-value")));
        pmd.setMappedBy(getAttr(attrs, "mapped-by"));
        pmd.setColumn(getAttr(attrs, "column"));
        pmd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
        pmd.setUnique(getAttr(attrs, "unique"));
        pmd.setTable(getAttr(attrs, "table"));
        pmd.setLoadFetchGroup(getAttr(attrs, "load-fetch-group"));
        pmd.setRecursionDepth(getAttr(attrs, "recursion-depth"));
        pmd.setValueStrategy(getAttr(attrs, "value-strategy"));
        pmd.setSequence(getAttr(attrs, "sequence"));
        pmd.setFieldTypes(getAttr(attrs, "field-type"));
        pmd.setFieldName(getAttr(attrs, "field-name"));
        String cacheableAttr = getAttr(attrs, "cacheable");
        if (cacheableAttr != null)
        {
            pmd.setCacheable(cacheableAttr.equalsIgnoreCase("false") ? false : true);
        }

        String converterAttr = getAttr(attrs, "converter");
        String disableConverterAttr = getAttr(attrs, "use-default-conversion");
        if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
        {
            pmd.setTypeConverterDisabled();
        }
        else if (!StringUtils.isWhitespace(converterAttr))
        {
            TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
            ClassLoaderResolver clr = mmgr.getNucleusContext().getClassLoaderResolver(null);
            Class converterCls = clr.classForName(converterAttr);
            if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
            {
                // Not yet cached an instance of this converter so create one
                AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, null); // TODO member type
                Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                // Register the TypeConverter under the name of the AttributeConverter class
                JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                typeMgr.registerConverter(converterAttr, typeConv, attrType, dbType, false, null);
            }

            pmd.setTypeConverterName(converterAttr);
        }

        return pmd;
    }

    /**
     * Handler method called at the start of an element.
     * @param uri URI of the tag
     * @param localName Local name
     * @param qName Element name
     * @param attrs Attributes for this element
     * @throws SAXException in parsing errors
     */
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException
    {
        if (charactersBuffer.length() > 0)
        {
            // Cater for subelements that appear before the end of the body text (save the body text with the
            // parent)
            String currentString = getString().trim();
            if (getStack() instanceof QueryMetaData)
            {
                ((QueryMetaData) getStack()).setQuery(currentString.trim());
            }
        }

        if (localName.length() < 1)
        {
            localName = qName;
        }
        try
        {
            if (localName.equals("jdo"))
            {
                FileMetaData filemd = (FileMetaData) getStack();
                filemd.setType(MetadataFileType.JDO_FILE);
                filemd.setCatalog(getAttr(attrs, "catalog"));
                filemd.setSchema(getAttr(attrs, "schema"));
            }
            else if (localName.equals("orm"))
            {
                FileMetaData filemd = (FileMetaData) getStack();
                filemd.setType(MetadataFileType.JDO_ORM_FILE);
                filemd.setCatalog(getAttr(attrs, "catalog"));
                filemd.setSchema(getAttr(attrs, "schema"));
            }
            else if (localName.equals("jdoquery"))
            {
                FileMetaData filemd = (FileMetaData) getStack();
                filemd.setType(MetadataFileType.JDO_QUERY_FILE);
            }
            else if (localName.equals("fetch-plan"))
            {
                FileMetaData filemd = (FileMetaData) metadata;
                FetchPlanMetaData fpmd = filemd.newFetchPlanMetadata(getAttr(attrs, "name"));
                fpmd.setMaxFetchDepth(getAttr(attrs, "max-fetch-depth"));
                fpmd.setFetchSize(getAttr(attrs, "fetch-size"));
                pushStack(fpmd);
            }
            else if (localName.equals("package"))
            {
                FileMetaData filemd = (FileMetaData) getStack();
                PackageMetaData pmd = filemd.newPackageMetadata(getAttr(attrs, "name"));
                pmd.setCatalog(getAttr(attrs, "catalog"));
                pmd.setSchema(getAttr(attrs, "schema"));
                pushStack(pmd);
            }
            else if (localName.equals("class"))
            {
                PackageMetaData pmd = (PackageMetaData) getStack();
                ClassMetaData cmd = newClassObject(pmd, attrs);
                pmd.addClass(cmd);

                pushStack(cmd);
            }
            else if (localName.equals("interface"))
            {
                PackageMetaData pmd = (PackageMetaData) getStack();
                InterfaceMetaData imd = newInterfaceObject(pmd, attrs);
                pmd.addInterface(imd);
                pushStack(imd);
            }
            else if (localName.equals("primary-key"))
            {
                MetaData md = getStack();
                PrimaryKeyMetaData pkmd = new PrimaryKeyMetaData();
                pkmd.setName(getAttr(attrs, "name"));
                pkmd.setColumnName(getAttr(attrs, "column"));
                if (md instanceof AbstractClassMetaData)
                {
                    ((AbstractClassMetaData) md).setPrimaryKeyMetaData(pkmd);
                }
                else if (md instanceof JoinMetaData)
                {
                    ((JoinMetaData) md).setPrimaryKeyMetaData(pkmd);
                }
                pushStack(pkmd);
            }
            else if (localName.equals("implements"))
            {
                ClassMetaData cmd = (ClassMetaData) getStack();
                ImplementsMetaData imd = new ImplementsMetaData(getAttr(attrs, "name"));
                cmd.addImplements(imd);
                pushStack(imd);
            }
            else if (localName.equals("property"))
            {
                MetaData parent = getStack();
                if (parent instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData acmd = (AbstractClassMetaData) parent;
                    PropertyMetaData propmd = newPropertyObject(acmd, attrs);
                    acmd.addMember(propmd);
                    pushStack(propmd);
                }
                else if (parent instanceof EmbeddedMetaData)
                {
                    EmbeddedMetaData emd = (EmbeddedMetaData) parent;
                    PropertyMetaData propmd = newPropertyObject(emd, attrs);
                    emd.addMember(propmd);
                    pushStack(propmd);
                }
                else if (parent instanceof ImplementsMetaData)
                {
                    ImplementsMetaData implmd = (ImplementsMetaData) parent;
                    PropertyMetaData propmd = newPropertyObject(implmd, attrs);
                    implmd.addProperty(propmd);
                    pushStack(propmd);
                }
                else if (parent instanceof FetchGroupMetaData)
                {
                    FetchGroupMetaData fgmd = (FetchGroupMetaData) parent;
                    FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fgmd, getAttr(attrs, "name"));
                    fgmmd.setRecursionDepth(getAttr(attrs, "recursion-depth"));
                    fgmmd.setProperty();
                    fgmd.addMember(fgmmd);
                    pushStack(fgmmd);
                }
            }
            else if (localName.equals("datastore-identity"))
            {
                AbstractClassMetaData acmd = (AbstractClassMetaData) getStack();
                IdentityMetaData idmd = new IdentityMetaData();
                idmd.setColumnName(getAttr(attrs, "column"));
                idmd.setValueStrategy(ValueGenerationStrategy.getIdentityStrategy(getAttr(attrs, "strategy")));
                idmd.setSequence(getAttr(attrs, "sequence"));
                acmd.setIdentityMetaData(idmd);
                pushStack(idmd);
            }
            else if (localName.equals("inheritance"))
            {
                MetaData parent = getStack();
                AbstractClassMetaData acmd = (AbstractClassMetaData) parent;
                InheritanceMetaData inhmd = new InheritanceMetaData();
                inhmd.setStrategy(getAttr(attrs, "strategy"));
                acmd.setInheritanceMetaData(inhmd);
                pushStack(inhmd);
            }
            else if (localName.equals("discriminator"))
            {
                MetaData md = getStack();
                if (md instanceof InheritanceMetaData)
                {
                    InheritanceMetaData inhmd = (InheritanceMetaData) md;
                    DiscriminatorMetaData dismd = inhmd.newDiscriminatorMetadata();
                    dismd.setColumnName(getAttr(attrs, "column"));
                    dismd.setValue(getAttr(attrs, "value"));
                    dismd.setStrategy(getAttr(attrs, "strategy"));
                    dismd.setIndexed(getAttr(attrs, "indexed"));
                    pushStack(dismd);
                }
                else if (md instanceof EmbeddedMetaData)
                {
                    EmbeddedMetaData embmd = (EmbeddedMetaData) md;
                    DiscriminatorMetaData dismd = embmd.newDiscriminatorMetadata();
                    dismd.setColumnName(getAttr(attrs, "column"));
                    dismd.setValue(getAttr(attrs, "value"));
                    dismd.setStrategy(getAttr(attrs, "strategy"));
                    dismd.setIndexed(getAttr(attrs, "indexed"));
                    pushStack(dismd);
                }
            }
            else if (localName.equals("query"))
            {
                MetaData emd = getStack();
                String name = getAttr(attrs, "name");
                String lang = getAttr(attrs, "language");
                if (!StringUtils.isWhitespace(lang))
                {
                    if (lang.equals(JDOQuery.JDOQL_QUERY_LANGUAGE)) // Convert to JDOQL
                    {
                        lang = QueryLanguage.JDOQL.toString();
                    }
                    else if (lang.equals(JDOQuery.SQL_QUERY_LANGUAGE)) // Convert to SQL
                    {
                        lang = QueryLanguage.SQL.toString();
                    }
                    else if (lang.equals(JDOQuery.JPQL_QUERY_LANGUAGE)) // Convert to JPQL
                    {
                        lang = QueryLanguage.JPQL.toString();
                    }
                }
                if (emd instanceof ClassMetaData)
                {
                    ClassMetaData cmd = (ClassMetaData) emd;
                    if (StringUtils.isWhitespace(name))
                    {
                        throw new InvalidClassMetaDataException("044154", cmd.getFullClassName());
                    }
                    QueryMetaData qmd = new QueryMetaData(name);
                    qmd.setScope(cmd.getFullClassName());
                    qmd.setLanguage(lang);
                    qmd.setUnmodifiable(getAttr(attrs, "unmodifiable"));
                    qmd.setResultClass(getAttr(attrs, "result-class"));
                    qmd.setUnique(getAttr(attrs, "unique"));
                    qmd.setFetchPlanName(getAttr(attrs, "fetch-plan"));
                    cmd.addQuery(qmd);
                    pushStack(qmd);
                }
                else if (emd instanceof InterfaceMetaData)
                {
                    InterfaceMetaData imd = (InterfaceMetaData) emd;
                    if (StringUtils.isWhitespace(name))
                    {
                        throw new InvalidClassMetaDataException("044154", imd.getFullClassName());
                    }
                    QueryMetaData qmd = new QueryMetaData(name);
                    qmd.setScope(imd.getFullClassName());
                    qmd.setLanguage(lang);
                    qmd.setUnmodifiable(getAttr(attrs, "unmodifiable"));
                    qmd.setResultClass(getAttr(attrs, "result-class"));
                    qmd.setUnique(getAttr(attrs, "unique"));
                    qmd.setFetchPlanName(getAttr(attrs, "fetch-plan"));
                    imd.addQuery(qmd);
                    pushStack(qmd);
                }
                else if (emd instanceof FileMetaData)
                {
                    FileMetaData filemd = (FileMetaData) emd;
                    QueryMetaData qmd = filemd.newQueryMetadata(name);
                    qmd.setLanguage(lang);
                    qmd.setUnmodifiable(getAttr(attrs, "unmodifiable"));
                    qmd.setResultClass(getAttr(attrs, "result-class"));
                    qmd.setUnique(getAttr(attrs, "unique"));
                    qmd.setFetchPlanName(getAttr(attrs, "fetch-plan"));
                    pushStack(qmd);
                }
            }
            else if (localName.equals("sequence"))
            {
                PackageMetaData pmd = (PackageMetaData) getStack();
                SequenceMetaData seqmd = pmd.newSequenceMetadata(getAttr(attrs, "name"), getAttr(attrs, "strategy"));
                seqmd.setFactoryClass(getAttr(attrs, "factory-class"));
                seqmd.setDatastoreSequence(getAttr(attrs, "datastore-sequence"));
                String seqSize = getAttr(attrs, "allocation-size");
                if (seqSize != null)
                {
                    seqmd.setAllocationSize(seqSize);
                }
                String seqStart = getAttr(attrs, "initial-value");
                if (seqStart != null)
                {
                    seqmd.setInitialValue(seqStart);
                }
                pushStack(seqmd);
            }
            else if (localName.equals("field"))
            {
                MetaData md = getStack();
                if (md instanceof FetchGroupMetaData)
                {
                    FetchGroupMetaData fgmd = (FetchGroupMetaData) md;
                    FetchGroupMemberMetaData fgmmd = new FetchGroupMemberMetaData(fgmd, getAttr(attrs, "name"));
                    fgmmd.setRecursionDepth(getAttr(attrs, "recursion-depth"));
                    fgmd.addMember(fgmmd);
                    pushStack(fgmmd);
                    return;
                }

                FieldMetaData fmd = newFieldObject(md, attrs);
                if (md instanceof ClassMetaData)
                {
                    ClassMetaData cmd = (ClassMetaData) md;
                    cmd.addMember(fmd);
                }
                else if (md instanceof EmbeddedMetaData)
                {
                    EmbeddedMetaData emd = (EmbeddedMetaData) md;
                    emd.addMember(fmd);
                }
                else if (md instanceof ForeignKeyMetaData)
                {
                    ForeignKeyMetaData fkmd = (ForeignKeyMetaData) md;
                    fkmd.addMember(fmd.getName());
                }
                else if (md instanceof IndexMetaData)
                {
                    IndexMetaData imd = (IndexMetaData) md;
                    imd.addMember(fmd.getName());
                }
                else if (md instanceof UniqueMetaData)
                {
                    UniqueMetaData umd = (UniqueMetaData) md;
                    umd.addMember(fmd.getName());
                }
                pushStack(fmd);
            }
            else if (localName.equals("join"))
            {
                MetaData parent = getStack();
                String tableName = getAttr(attrs, "table");
                String columnName = getAttr(attrs, "column");
                String outer = getAttr(attrs, "outer");
                IndexedValue indexed = IndexedValue.getIndexedValue(getAttr(attrs, "indexed"));
                String unique = getAttr(attrs, "unique");
                String deleteAction = getAttr(attrs, "delete-action");

                JoinMetaData joinmd = null;
                if (parent instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) parent;
                    joinmd = fmd.newJoinMetaData();
                }
                else if (parent instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) parent;
                    joinmd = new JoinMetaData();
                    cmd.addJoin(joinmd);
                }
                else if (parent instanceof InheritanceMetaData)
                {
                    InheritanceMetaData inhmd = (InheritanceMetaData) parent;
                    joinmd = inhmd.newJoinMetadata();
                }
                else
                {
                    throw new NucleusUserException("Error processing JDO XML metadata. Found \"join\" with parent " + StringUtils.toJVMIDString(parent) + " - not supported");
                }
                joinmd.setTable(tableName);
                joinmd.setColumnName(columnName);
                joinmd.setOuter(MetaDataUtils.getBooleanForString(outer, false));
                joinmd.setIndexed(indexed);
                joinmd.setUnique(unique);
                joinmd.setDeleteAction(deleteAction);
                pushStack(joinmd);
            }
            else if (localName.equals("map"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                MapMetaData mapmd = fmd.newMapMetaData();

                mapmd.setKeyType(getAttr(attrs, "key-type"));
                String embKeyStr = getAttr(attrs, "embedded-key");
                if (!StringUtils.isWhitespace(embKeyStr))
                {
                    mapmd.setEmbeddedKey(Boolean.valueOf(embKeyStr));
                }
                String serKeyStr = getAttr(attrs, "serialized-key");
                if (!StringUtils.isWhitespace(serKeyStr))
                {
                    mapmd.setSerializedKey(Boolean.valueOf(serKeyStr));
                }
                String depKeyStr = getAttr(attrs, "dependent-key");
                if (!StringUtils.isWhitespace(depKeyStr))
                {
                    mapmd.setDependentKey(Boolean.valueOf(depKeyStr));
                }

                mapmd.setValueType(getAttr(attrs, "value-type"));
                String embValStr = getAttr(attrs, "embedded-value");
                if (!StringUtils.isWhitespace(embValStr))
                {
                    mapmd.setEmbeddedValue(Boolean.valueOf(embValStr));
                }
                String serValStr = getAttr(attrs, "serialized-value");
                if (!StringUtils.isWhitespace(serValStr))
                {
                    mapmd.setSerializedValue(Boolean.valueOf(serValStr));
                }
                String depValStr = getAttr(attrs, "dependent-value");
                if (!StringUtils.isWhitespace(depValStr))
                {
                    mapmd.setDependentValue(Boolean.valueOf(depValStr));
                }

                pushStack(mapmd);
            }
            else if (localName.equals("array"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                ArrayMetaData arrmd = fmd.newArrayMetaData();
                arrmd.setElementType(getAttr(attrs, "element-type"));
                String embElemStr = getAttr(attrs, "embedded-element");
                if (!StringUtils.isWhitespace(embElemStr))
                {
                    arrmd.setEmbeddedElement(Boolean.valueOf(embElemStr));
                }
                String serElemStr = getAttr(attrs, "serialized-element");
                if (!StringUtils.isWhitespace(serElemStr))
                {
                    arrmd.setSerializedElement(Boolean.valueOf(serElemStr));
                }
                String depElemStr = getAttr(attrs, "dependent-element");
                if (!StringUtils.isWhitespace(depElemStr))
                {
                    arrmd.setDependentElement(Boolean.valueOf(depElemStr));
                }
                pushStack(arrmd);
            }
            else if (localName.equals("collection"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                CollectionMetaData collmd = fmd.newCollectionMetaData();
                collmd.setElementType(getAttr(attrs, "element-type"));
                String embElemStr = getAttr(attrs, "embedded-element");
                if (!StringUtils.isWhitespace(embElemStr))
                {
                    collmd.setEmbeddedElement(Boolean.valueOf(embElemStr));
                }
                String serElemStr = getAttr(attrs, "serialized-element");
                if (!StringUtils.isWhitespace(serElemStr))
                {
                    collmd.setSerializedElement(Boolean.valueOf(serElemStr));
                }
                String depElemStr = getAttr(attrs, "dependent-element");
                if (!StringUtils.isWhitespace(depElemStr))
                {
                    collmd.setDependentElement(Boolean.valueOf(depElemStr));
                }
                pushStack(collmd);
            }
            else if (localName.equals("column"))
            {
                MetaData md = getStack();
                ColumnMetaData colmd = new ColumnMetaData();
                colmd.setName(getAttr(attrs, "name"));
                colmd.setTarget(getAttr(attrs, "target"));
                colmd.setTargetMember(getAttr(attrs, "target-field"));
                colmd.setJdbcType(getAttr(attrs, "jdbc-type"));
                colmd.setSqlType(getAttr(attrs, "sql-type"));
                colmd.setLength(getAttr(attrs, "length"));
                colmd.setScale(getAttr(attrs, "scale"));
                colmd.setAllowsNull(getAttr(attrs, "allows-null"));
                colmd.setDefaultValue(getAttr(attrs, "default-value"));
                colmd.setInsertValue(getAttr(attrs, "insert-value"));
                String pos = getAttr(attrs, "position"); // JDO 3.1+
                if (pos != null)
                {
                    colmd.setPosition(pos);
                }

                if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) md;
                    fmd.addColumn(colmd);
                }
                else if (md instanceof AbstractElementMetaData)
                {
                    AbstractElementMetaData elemd = (AbstractElementMetaData) md;
                    elemd.addColumn(colmd);
                }
                else if (md instanceof JoinMetaData)
                {
                    JoinMetaData jnmd = (JoinMetaData) md;
                    jnmd.addColumn(colmd);
                }
                else if (md instanceof IdentityMetaData)
                {
                    IdentityMetaData idmd = (IdentityMetaData) md;
                    idmd.setColumnMetaData(colmd);
                }
                else if (md instanceof ForeignKeyMetaData)
                {
                    ForeignKeyMetaData fkmd = (ForeignKeyMetaData) md;
                    fkmd.addColumn(colmd);
                }
                else if (md instanceof IndexMetaData)
                {
                    IndexMetaData idxmd = (IndexMetaData) md;
                    idxmd.addColumn(colmd.getName());
                }
                else if (md instanceof UniqueMetaData)
                {
                    UniqueMetaData unimd = (UniqueMetaData) md;
                    unimd.addColumn(colmd.getName());
                }
                else if (md instanceof OrderMetaData)
                {
                    OrderMetaData ormd = (OrderMetaData) md;
                    ormd.addColumn(colmd);
                }
                else if (md instanceof DiscriminatorMetaData)
                {
                    DiscriminatorMetaData dismd = (DiscriminatorMetaData) md;
                    dismd.setColumnMetaData(colmd);
                }
                else if (md instanceof VersionMetaData)
                {
                    VersionMetaData vermd = (VersionMetaData) md;
                    vermd.setColumnMetaData(colmd);
                }
                else if (md instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) md;
                    cmd.addUnmappedColumn(colmd);
                }
                else if (md instanceof PrimaryKeyMetaData)
                {
                    PrimaryKeyMetaData pkmd = (PrimaryKeyMetaData) md;
                    pkmd.addColumn(colmd);
                }
                pushStack(colmd);
            }
            else if (localName.equals("element"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                ElementMetaData elemmd = new ElementMetaData();
                elemmd.setTable(getAttr(attrs, "table"));
                elemmd.setColumnName(getAttr(attrs, "column"));
                elemmd.setDeleteAction(getAttr(attrs, "delete-action"));
                elemmd.setUpdateAction(getAttr(attrs, "update-action"));
                elemmd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
                elemmd.setUnique(MetaDataUtils.getBooleanForString(getAttr(attrs, "unique"), false));
                String mappedBy = getAttr(attrs, "mapped-by");
                elemmd.setMappedBy(mappedBy);
                if (!StringUtils.isWhitespace(mappedBy) && fmd.getMappedBy() == null)
                {
                    // With collection/array this is the same as mapped-by on the field
                    fmd.setMappedBy(mappedBy);
                }

                String converterAttr = getAttr(attrs, "converter");
                String disableConverterAttr = getAttr(attrs, "use-default-conversion");
                if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
                {
                    // TODO Disable on the element?
                }
                else if (!StringUtils.isWhitespace(converterAttr))
                {
                    TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                    ClassLoaderResolver clr = mmgr.getNucleusContext().getClassLoaderResolver(null);
                    Class converterCls = clr.classForName(converterAttr);
                    if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                    {
                        // Not yet cached an instance of this converter so create one
                        AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, null); // TODO element type
                        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                        // Register the TypeConverter under the name of the AttributeConverter class
                        JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                        typeMgr.registerConverter(converterAttr, typeConv, attrType, dbType, false, null);
                    }

                    elemmd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterAttr);
                }

                fmd.setElementMetaData(elemmd);
                pushStack(elemmd);
            }
            else if (localName.equals("key"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                KeyMetaData keymd = new KeyMetaData();
                keymd.setTable(getAttr(attrs, "table"));
                keymd.setColumnName(getAttr(attrs, "column"));
                keymd.setDeleteAction(getAttr(attrs, "delete-action"));
                keymd.setUpdateAction(getAttr(attrs, "update-action"));
                keymd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
                keymd.setUnique(MetaDataUtils.getBooleanForString(getAttr(attrs, "unique"), false));
                keymd.setMappedBy(getAttr(attrs, "mapped-by"));

                String converterAttr = getAttr(attrs, "converter");
                String disableConverterAttr = getAttr(attrs, "use-default-conversion");
                if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
                {
                    // TODO Disable on the key?
                }
                else if (!StringUtils.isWhitespace(converterAttr))
                {
                    TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                    ClassLoaderResolver clr = mmgr.getNucleusContext().getClassLoaderResolver(null);
                    Class converterCls = clr.classForName(converterAttr);
                    if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                    {
                        // Not yet cached an instance of this converter so create one
                        AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, null); // TODO key type
                        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                        // Register the TypeConverter under the name of the AttributeConverter class
                        JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                        typeMgr.registerConverter(converterAttr, typeConv, attrType, dbType, false, null);
                    }

                    keymd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterAttr);
                }

                fmd.setKeyMetaData(keymd);
                pushStack(keymd);
            }
            // New value
            else if (localName.equals("value"))
            {
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                ValueMetaData valuemd = new ValueMetaData();
                valuemd.setTable(getAttr(attrs, "table"));
                valuemd.setColumnName(getAttr(attrs, "column"));
                valuemd.setDeleteAction(getAttr(attrs, "delete-action"));
                valuemd.setUpdateAction(getAttr(attrs, "update-action"));
                valuemd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
                valuemd.setUnique(MetaDataUtils.getBooleanForString(getAttr(attrs, "unique"), false));
                valuemd.setMappedBy(getAttr(attrs, "mapped-by"));

                String converterAttr = getAttr(attrs, "converter");
                String disableConverterAttr = getAttr(attrs, "use-default-conversion");
                if (disableConverterAttr != null && Boolean.getBoolean(disableConverterAttr))
                {
                    // TODO Disable on the value?
                }
                else if (!StringUtils.isWhitespace(converterAttr))
                {
                    TypeManager typeMgr = mmgr.getNucleusContext().getTypeManager();
                    ClassLoaderResolver clr = mmgr.getNucleusContext().getClassLoaderResolver(null);
                    Class converterCls = clr.classForName(converterAttr);
                    if (typeMgr.getTypeConverterForName(converterCls.getName()) == null)
                    {
                        // Not yet cached an instance of this converter so create one
                        AttributeConverter conv = JDOTypeConverterUtils.createAttributeConverter(mmgr.getNucleusContext(), converterCls);
                        Class attrType = JDOTypeConverterUtils.getAttributeTypeForAttributeConverter(converterCls, null); // TODO value type
                        Class dbType = JDOTypeConverterUtils.getDatastoreTypeForAttributeConverter(converterCls, attrType, null);

                        // Register the TypeConverter under the name of the AttributeConverter class
                        JDOTypeConverter typeConv = new JDOTypeConverter(conv);
                        typeMgr.registerConverter(converterAttr, typeConv, attrType, dbType, false, null);
                    }

                    valuemd.addExtension(MetaData.EXTENSION_MEMBER_TYPE_CONVERTER_NAME, converterAttr);
                }

                fmd.setValueMetaData(valuemd);
                pushStack(valuemd);
            }
            // New fetch-group
            else if (localName.equals("fetch-group"))
            {
                MetaData md = getStack();
                FetchGroupMetaData fgmd = new FetchGroupMetaData(getAttr(attrs, "name"));
                String postLoadStr = getAttr(attrs, "post-load");
                if (!StringUtils.isWhitespace(postLoadStr))
                {
                    fgmd.setPostLoad(Boolean.valueOf(postLoadStr));
                }
                if (md instanceof FetchGroupMetaData)
                {
                    FetchGroupMetaData fgmdParent = (FetchGroupMetaData) md;
                    fgmdParent.addFetchGroup(fgmd);
                }
                else if (md instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) md;
                    cmd.addFetchGroup(fgmd);
                }
                else if (md instanceof FetchPlanMetaData)
                {
                    FetchPlanMetaData fpmd = (FetchPlanMetaData) md;
                    fpmd.addFetchGroup(fgmd);
                }
                pushStack(fgmd);
            }
            else if (localName.equals("extension"))
            {
                MetaData md = getStack();
                String vendorName = getAttr(attrs, "vendor-name");
                if (StringUtils.isWhitespace(vendorName))
                {
                    throw new InvalidMetaDataException("044160", vendorName, getAttr(attrs, "key"), getAttr(attrs, "value"));
                }
                if (vendorName != null && vendorName.equalsIgnoreCase(MetaData.VENDOR_NAME))
                {
                    String extKey = getAttr(attrs, "key");
                    if (extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANT) || extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANCY_COLUMN_NAME) ||
                        extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANCY_JDBC_TYPE) || extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANCY_COLUMN_LENGTH))
                    {
                        // Multitenancy TODO Put this within a <multitenancy> block and process like that
                        AbstractClassMetaData cmd = (AbstractClassMetaData)md;
                        MultitenancyMetaData mtmd = cmd.getMultitenancyMetaData();
                        if (mtmd == null)
                        {
                            mtmd = cmd.newMultitenancyMetaData();
                        }
                        if (extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANCY_COLUMN_NAME))
                        {
                            mtmd.setColumnName(getAttr(attrs, "value"));
                        }
                        else if (extKey.equals(MetaData.EXTENSION_CLASS_MULTITENANCY_COLUMN_LENGTH))
                        {
                            ColumnMetaData colmd = mtmd.getColumnMetaData();
                            if (colmd == null)
                            {
                                colmd = mtmd.newColumnMetaData();
                            }
                            colmd.setLength(getAttr(attrs, "value"));
                        }
                    }
                    else if (extKey.equals(MetaData.EXTENSION_CLASS_SOFTDELETE) || extKey.equals(MetaData.EXTENSION_CLASS_SOFTDELETE_COLUMN_NAME))
                    {
                        // SoftDelete TODO Put this within a <soft-delete> block and process like that
                        AbstractClassMetaData cmd = (AbstractClassMetaData)md;
                        MultitenancyMetaData mtmd = cmd.getMultitenancyMetaData();
                        if (mtmd == null)
                        {
                            mtmd = cmd.newMultitenancyMetaData();
                        }
                        if (extKey.equals(MetaData.EXTENSION_CLASS_SOFTDELETE_COLUMN_NAME))
                        {
                            mtmd.setColumnName(getAttr(attrs, "value"));
                        }
                    }
                    else
                    {
                        md.addExtension(extKey, getAttr(attrs, "value"));
                    }
                }
            }
            else if (localName.equals("version"))
            {
                MetaData md = getStack();
                AbstractClassMetaData cmd = null;
                String memberName = null;
                if (md instanceof AbstractClassMetaData)
                {
                    cmd = (AbstractClassMetaData)md;
                }
                else if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData mmd = (AbstractMemberMetaData)md;
                    cmd = mmd.getAbstractClassMetaData();
                    memberName = mmd.getName();
                }

                if (cmd != null)
                {
                    VersionMetaData vermd = cmd.newVersionMetadata();
                    String strategy = getAttr(attrs, "strategy");
                    vermd.setStrategy(StringUtils.isWhitespace(strategy) ? "version-number" : strategy);
                    vermd.setColumnName(getAttr(attrs, "column"));
                    vermd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
                    if (memberName != null)
                    {
                        vermd.setFieldName(memberName);
                    }
                    pushStack(vermd);
                }
            }
            else if (localName.equals("index"))
            {
                MetaData md = getStack();
                IndexMetaData idxmd = new IndexMetaData();
                idxmd.setName(getAttr(attrs, "name"));
                idxmd.setTable(getAttr(attrs, "table"));
                String uniStr = getAttr(attrs, "unique");
                if (!StringUtils.isWhitespace(uniStr))
                {
                    idxmd.setUnique(Boolean.valueOf(uniStr));
                }
                if (md instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) md;
                    cmd.addIndex(idxmd);
                }
                else if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) md;
                    fmd.setIndexMetaData(idxmd);
                }
                else if (md instanceof JoinMetaData)
                {
                    JoinMetaData jmd = (JoinMetaData) md;
                    jmd.setIndexMetaData(idxmd);
                }
                else if (md instanceof AbstractElementMetaData)
                {
                    AbstractElementMetaData elmd = (AbstractElementMetaData) md;
                    elmd.setIndexMetaData(idxmd);
                }
                else if (md instanceof OrderMetaData)
                {
                    OrderMetaData omd = (OrderMetaData) md;
                    omd.setIndexMetaData(idxmd);
                }
                else if (md instanceof VersionMetaData)
                {
                    VersionMetaData vermd = (VersionMetaData) md;
                    vermd.setIndexMetaData(idxmd);
                }
                else if (md instanceof DiscriminatorMetaData)
                {
                    DiscriminatorMetaData dismd = (DiscriminatorMetaData) md;
                    dismd.setIndexMetaData(idxmd);
                }
                pushStack(idxmd);
            }
            else if (localName.equals("unique"))
            {
                MetaData md = getStack();
                UniqueMetaData unimd = new UniqueMetaData();
                unimd.setName(getAttr(attrs, "name"));
                unimd.setTable(getAttr(attrs, "table"));
                String defStr = getAttr(attrs, "deferred");
                if (!StringUtils.isWhitespace(defStr))
                {
                    unimd.setDeferred(Boolean.valueOf(defStr));
                }
                if (md instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) md;
                    cmd.addUniqueConstraint(unimd);
                }
                else if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) md;
                    fmd.setUniqueMetaData(unimd);
                }
                else if (md instanceof JoinMetaData)
                {
                    JoinMetaData jmd = (JoinMetaData) md;
                    jmd.setUniqueMetaData(unimd);
                }
                else if (md instanceof AbstractElementMetaData)
                {
                    AbstractElementMetaData elmd = (AbstractElementMetaData) md;
                    elmd.setUniqueMetaData(unimd);
                }
                pushStack(unimd);
            }
            else if (localName.equals("foreign-key"))
            {
                MetaData md = getStack();
                ForeignKeyMetaData fkmd = new ForeignKeyMetaData();
                fkmd.setName(getAttr(attrs, "name"));
                fkmd.setTable(getAttr(attrs, "table"));
                fkmd.setUnique(getAttr(attrs, "unique"));
                fkmd.setDeferred(getAttr(attrs, "deferred"));
                fkmd.setDeleteAction(ForeignKeyAction.getForeignKeyAction(getAttr(attrs, "delete-action")));
                fkmd.setUpdateAction(ForeignKeyAction.getForeignKeyAction(getAttr(attrs, "update-action")));
                if (md instanceof AbstractClassMetaData)
                {
                    AbstractClassMetaData cmd = (AbstractClassMetaData) md;
                    cmd.addForeignKey(fkmd);
                }
                else if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) md;
                    fmd.setForeignKeyMetaData(fkmd);
                }
                else if (md instanceof JoinMetaData)
                {
                    JoinMetaData jmd = (JoinMetaData) md;
                    jmd.setForeignKeyMetaData(fkmd);
                }
                else if (md instanceof AbstractElementMetaData)
                {
                    AbstractElementMetaData elmd = (AbstractElementMetaData) md;
                    elmd.setForeignKeyMetaData(fkmd);
                }
                pushStack(fkmd);
            }
            else if (localName.equals("order"))
            {
                OrderMetaData ordmd = new OrderMetaData();
                ordmd.setIndexed(IndexedValue.getIndexedValue(getAttr(attrs, "indexed")));
                ordmd.setColumnName(getAttr(attrs, "column"));
                ordmd.setMappedBy(getAttr(attrs, "mapped-by"));
                AbstractMemberMetaData fmd = (AbstractMemberMetaData) getStack();
                fmd.setOrderMetaData(ordmd);
                pushStack(ordmd);
            }
            else if (localName.equals("embedded"))
            {
                MetaData md = getStack();
                EmbeddedMetaData embmd = new EmbeddedMetaData();
                embmd.setOwnerMember(getAttr(attrs, "owner-field"));
                embmd.setNullIndicatorColumn(getAttr(attrs, "null-indicator-column"));
                embmd.setNullIndicatorValue(getAttr(attrs, "null-indicator-value"));
                if (md instanceof AbstractMemberMetaData)
                {
                    AbstractMemberMetaData fmd = (AbstractMemberMetaData) md;
                    fmd.setEmbeddedMetaData(embmd);
                }
                else if (md instanceof KeyMetaData)
                {
                    KeyMetaData kmd = (KeyMetaData) md;
                    kmd.setEmbeddedMetaData(embmd);
                }
                else if (md instanceof ValueMetaData)
                {
                    ValueMetaData vmd = (ValueMetaData) md;
                    vmd.setEmbeddedMetaData(embmd);
                }
                else if (md instanceof ElementMetaData)
                {
                    ElementMetaData elmd = (ElementMetaData) md;
                    elmd.setEmbeddedMetaData(embmd);
                }
                pushStack(embmd);
            }
            else
            {
                String message = Localiser.msg("044037", qName);
                NucleusLogger.METADATA.error(message);
                throw new RuntimeException(message);
            }
        }
        catch (RuntimeException ex)
        {
            NucleusLogger.METADATA.error(Localiser.msg("044042", qName, getStack(), uri), ex);
            throw ex;
        }
    }

    /**
     * Handler method called at the end of an element.
     * @param uri URI of the tag
     * @param localName local name
     * @param qName Name of element just ending
     * @throws SAXException in parsing errors
     */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (localName.length() < 1)
        {
            localName = qName;
        }

        // Save the current string for elements that have a body value
        String currentString = getString().trim();
        if (currentString.length() > 0)
        {
            MetaData md = getStack();
            if (localName.equals("query"))
            {
                ((QueryMetaData) md).setQuery(currentString);
            }
        }

        // Pop the tag
        // If startElement pushes an element onto the stack need a remove here for that type
        if (localName.equals("package") || localName.equals("fetch-plan") || localName.equals("class") || localName.equals("interface") || localName
                .equals("implements") || localName.equals("property") || localName.equals("datastore-identity") || localName.equals("inheritance") || localName
                .equals("primary-key") || localName.equals("version") || localName.equals("unmapped") || localName.equals("query") || localName
                .equals("sequence") || localName.equals("field") || localName.equals("map") || localName.equals("element") || localName.equals("embedded") || localName
                .equals("key") || localName.equals("value") || localName.equals("array") || localName.equals("collection") || localName.equals("join") || localName
                .equals("index") || localName.equals("unique") || localName.equals("foreign-key") || localName.equals("order") || localName
                .equals("fetch-group") || localName.equals("column") || localName.equals("discriminator"))
        {
            popStack();
        }
    }
}