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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.Configuration;
import org.datanucleus.PersistenceNucleusContextImpl;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPropertyNames;
import org.datanucleus.enhancer.EnhancementHelper;
import org.datanucleus.enhancer.EnhancementHelper.RegisterClassListener;
import org.datanucleus.enhancer.EnhancementNucleusContextImpl;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.DiscriminatorMetaData;
import org.datanucleus.metadata.DiscriminatorStrategy;
import org.datanucleus.metadata.FileMetaData;
import org.datanucleus.metadata.ImplementsMetaData;
import org.datanucleus.metadata.InterfaceMetaData;
import org.datanucleus.metadata.MetaDataManagerImpl;
import org.datanucleus.metadata.MetaDataMerger;
import org.datanucleus.metadata.MetaDataFileType;
import org.datanucleus.metadata.PackageMetaData;
import org.datanucleus.metadata.QueryMetaData;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.metadata.xml.XmlMetaDataParser;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Manager of JDO MetaData information in DataNucleus. 
 * <P>
 * Acts as a registry of JDO metadata so that metadata files don't need to be 
 * parsed multiple times. MetaData is stored as a FileMetaData, which contains
 * PackageMetaData, which contains ClassMetaData, and so on. This maps exactly
 * to the users model of their metadata. The users access point is 
 * <B>getMetaDataForClass()</B> which will check the known classes without metadata,
 * then check the existing registered metadata, then check the valid locations for 
 * metadata files. This way, the metadata is managed from this single point.
 * </P>
 * <P>
 * When the MetaData is requested for a class, if it isn't already found, the valid
 * file locations are checked for that class and the file containing it will be
 * read. The MetaData for all classes, queries, sequences etc in that file are
 * loaded at that point. In addition, all classes will be "populated" (meaning that
 * their superclasses are assigned, and unspecified fields are added, and any related
 * objects are linked). The MetaData of these classes are only initialised when
 * they are absolutely needed - to avoid generating circular references in the
 * initialisation process.
 * </P>
 * <P>
 * Each NucleusContext typically will have its own MetaDataManager so allowing
 * Meta-Data to be for different datastores. In addition, each PMF can allow
 * MetaData files to use a particular suffix, hence we allow the JDO/ORM file
 * suffices to be specifiable at construction.
 * </P>
 */
public class JDOMetaDataManager extends MetaDataManagerImpl
{
    private static final long serialVersionUID = -2276240352978344222L;

    protected boolean allowXmlLocationsFromJDO1_0 = false;

    /** Parser for XML MetaData. */
    protected XmlMetaDataParser xmlMetaDataParser = null;

    /** Map of ClassMetaData from ORM files, keyed by the class name. */
    protected Map<String, AbstractClassMetaData> ormClassMetaDataByClass = new ConcurrentHashMap<String, AbstractClassMetaData>();

    /**
     * Map of ClassMetaData, keyed by the interface class name (for "persistent-interface"s).
     * Keyed by the persistent-interface name.
     */
    protected Map<String, AbstractClassMetaData> classMetaDataByInterface = new ConcurrentHashMap<String, AbstractClassMetaData>();

    /** Listener for persistent class initialisations (since JDO uses a discovery process). */
    protected MetaDataRegisterClassListener registerListener;

    /**
     * Constructor.
     * @param ctxt NucleusContext that this metadata manager operates in
     */
    public JDOMetaDataManager(NucleusContext ctxt)
    {
        super(ctxt);

        allowXmlLocationsFromJDO1_0 = ctxt.getConfiguration().getBooleanProperty(JDOPropertyNames.PROPERTY_METADATA_XML_JDO_1_0, false);

        // Do we want to use the JDO class initialisation listener ?
        boolean useMetadataListener = false;
        Configuration conf = ctxt.getConfiguration();
        if (conf.getStringProperty(PropertyNames.PROPERTY_PERSISTENCE_UNIT_NAME) == null &&
            ctxt instanceof PersistenceNucleusContextImpl &&
            conf.getBooleanProperty(PropertyNames.PROPERTY_METADATA_AUTOREGISTER))
        {
            useMetadataListener = true;
        }

        // Log the current configuration
        if (NucleusLogger.METADATA.isDebugEnabled())
        {
            if (allowXML && allowAnnotations)
            {
                if (allowORM)
                {
                    String mappingName = getORMMappingName();
                    NucleusLogger.METADATA.debug("MetaDataManager : Input=(XML,Annotations)" +
                        ", XML-Validation=" + validateXML +
                        ", XML-Suffices=(persistence=*." + getJDOFileSuffix() +
                        ", orm="+getORMFileSuffix() +
                        ", query=*." + getJDOQueryFileSuffix() + ")" +
                        (mappingName != null ? (" mapping-name=" + mappingName) : "") +
                        ", JDO-listener=" + useMetadataListener);
                }
                else
                {
                    NucleusLogger.METADATA.debug("MetaDataManager : Input=(XML,Annotations)" +
                        ", XML-Validation=" + validateXML +
                        ", XML-Suffices=(persistence=*." + getJDOFileSuffix() +
                        ", query=*." + getJDOQueryFileSuffix() + ")" +
                        ", JDO-listener=" + useMetadataListener);
                }
            }
            else if (allowXML && !allowAnnotations)
            {
                if (allowORM)
                {
                    String mappingName = getORMMappingName();
                    NucleusLogger.METADATA.debug("MetaDataManager : Input=(XML)" +
                        ", XML-Validation=" + validateXML +
                        ", XML-Suffices=(persistence=*." + getJDOFileSuffix() +
                        ", orm="+getORMFileSuffix() +
                        ", query=*." + getJDOQueryFileSuffix() + ")" +
                        (mappingName != null ? (" mapping-name=" + mappingName) : "") +
                        ", JDO-listener=" + useMetadataListener);
                }
                else
                {
                    NucleusLogger.METADATA.debug("MetaDataManager : Input=(XML)" +
                        ", XML-Validation=" + validateXML +
                        ", XML-Suffices=(persistence=*." + getJDOFileSuffix() +
                        ", query=*." + getJDOQueryFileSuffix() + ")" +
                        ", JDO-listener=" + useMetadataListener);
                }
            }
            else if (!allowXML && allowAnnotations)
            {
                NucleusLogger.METADATA.debug("MetaDataManager : Input=(Annotations), JDO-listener=" + useMetadataListener);
            }
            else
            {
                NucleusLogger.METADATA.debug("MetaDataManager : Input=(NONE), JDO-listener=" + useMetadataListener);
            }
        }

        if (useMetadataListener)
        {
            // Register listener for persistent class initialisations
            NucleusLogger.METADATA.debug("Registering listener for metadata initialisation");
            registerListener = new MetaDataRegisterClassListener();
            EnhancementHelper.getInstance().addRegisterClassListener(registerListener);
        }
    }

    /**
     * Register to persistent class load
     */
    private class MetaDataRegisterClassListener implements RegisterClassListener
    {
        public void registerClass(Class registeredClass)
        {
            // register the class / interface in metadata, and make sure it is initialised
            NucleusLogger.METADATA.debug("Listener found initialisation for persistable class " + registeredClass.getName());
            try
            {
                getMetaDataForClass(registeredClass, nucleusContext.getClassLoaderResolver(registeredClass.getClassLoader()));
            }
            catch (Exception e)
            {
                NucleusLogger.METADATA.warn("Listener attempted to load metadata for " + registeredClass.getName() + " but an exception was thrown. Ignoring this class", e);
            }
        }
    }

    /**
     * Clear resources
     */
    public void close()
    {
        if (registerListener != null)
        {
            NucleusLogger.METADATA.debug("Deregistering listener for metadata initialisation");
            EnhancementHelper.getInstance().removeRegisterClassListener(registerListener);
        }

        super.close();
        ormClassMetaDataByClass.clear();
        ormClassMetaDataByClass = null;
    }

    @Override
    public void unloadMetaDataForClass(String className)
    {
        super.unloadMetaDataForClass(className);

        ormClassMetaDataByClass.remove(className);
    }

    /**
     * Utility to parse an XML file, using the "jdo" MetaData handler.
     * @param fileURL URL of the file
     * @return The FileMetaData for this file
     */
    protected FileMetaData parseXmlFile(URL fileURL)
    {
        if (xmlMetaDataParser == null)
        {
            xmlMetaDataParser = new XmlMetaDataParser(this, nucleusContext.getPluginManager(), validateXML, supportXMLNamespaces);
        }
        return (FileMetaData)xmlMetaDataParser.parseXmlMetaDataURL(fileURL, "jdo");
    }

    @Override
    public void registerFile(String fileURLString, FileMetaData filemd, ClassLoaderResolver clr)
    {
        if (fileURLString == null)
        {
            // Null file
            return;
        }
        if (fileMetaDataByURLString.get(fileURLString) != null)
        {
            // Already registered!
            return;
        }

        fileMetaDataByURLString.put(fileURLString, filemd);

        registerQueriesForFile(filemd);
        registerFetchPlansForFile(filemd);
        registerSequencesForFile(filemd);
        registerTableGeneratorsForFile(filemd);

        // Register the classes and interfaces for later use
        if (filemd.getType() != MetaDataFileType.JDO_QUERY_FILE)
        {
            int numPackages = filemd.getNoOfPackages();
            for (int i = 0; i < numPackages; i++)
            {
                PackageMetaData pmd = filemd.getPackage(i);

                // Register all classes into the respective lookup maps
                int numClasses = pmd.getNoOfClasses();
                for (int j = 0; j < numClasses; j++)
                {
                    ClassMetaData cmd = pmd.getClass(j);
                    if (classesWithoutPersistenceInfo.contains(cmd.getFullClassName()))
                    {
                        // Remove from unknown classes now that we have some metadata
                        classesWithoutPersistenceInfo.remove(cmd.getFullClassName());
                    }
                    if (filemd.getType() == MetaDataFileType.JDO_FILE || filemd.getType() == MetaDataFileType.ANNOTATIONS)
                    {
                        registerMetaDataForClass(cmd.getFullClassName(), cmd);
                    }
                    else if (filemd.getType() == MetaDataFileType.JDO_ORM_FILE)
                    {
                        ormClassMetaDataByClass.put(cmd.getFullClassName(), cmd);
                    }
                    if (cmd.getEntityName() != null)
                    {
                        // Register the metadata under the entity name
                        classMetaDataByEntityName.put(cmd.getEntityName(), cmd);
                    }
                    if (cmd.getInheritanceMetaData() != null)
                    {
                        // Register the metadata under the discriminator name
                        DiscriminatorMetaData dismd = cmd.getInheritanceMetaData().getDiscriminatorMetaData();
                        if (dismd != null)
                        {
                            if (dismd.getStrategy() == DiscriminatorStrategy.CLASS_NAME)
                            {
                                classMetaDataByDiscriminatorName.put(cmd.getFullClassName(), cmd);
                            }
                            else if (dismd.getStrategy() == DiscriminatorStrategy.VALUE_MAP && dismd.getValue() != null)
                            {
                                classMetaDataByDiscriminatorName.put(dismd.getValue(), cmd);
                            }
                        }
                    }
                }

                // Register all interfaces into the respective lookup maps
                int numInterfaces = pmd.getNoOfInterfaces();
                for (int j = 0; j < numInterfaces; j++)
                {
                    InterfaceMetaData intfmd = pmd.getInterface(j);
                    if (filemd.getType() == MetaDataFileType.JDO_FILE || filemd.getType() == MetaDataFileType.ANNOTATIONS)
                    {
                        registerMetaDataForClass(intfmd.getFullClassName(), intfmd);
                    }
                    else if (filemd.getType() == MetaDataFileType.JDO_ORM_FILE)
                    {
                        ormClassMetaDataByClass.put(intfmd.getFullClassName(), intfmd);
                    }
                }
            }
        }
    }

    /**
     * Load the metadata for the specified class (if available).
     * With JDO we check for XML metadata for the class (in one of the standard locations), or annotations on the class itself.
     * @param c The class
     * @param clr ClassLoader resolver
     * @return The metadata for this class (if found)
     */
    @Override
    protected AbstractClassMetaData loadMetaDataForClass(Class c, ClassLoaderResolver clr)
    {
        if (!allowMetaDataLoad)
        {
            // Not allowing further metadata load so just return
            return null;
        }

        String className = c.getName();
        try
        {
            // TODO What if a different thread starts loading this class just before we do? it will load then we do too
            updateLock.lock();

            if (allowXML)
            {
                // Search valid location for a JDO XML definition (package.jdo etc) for this class and load all classes specified in the file
                FileMetaData filemd = loadXMLMetaDataForClass(c, clr, null, getJDOFileSuffix(), MetaDataFileType.JDO_FILE, true);
                if (filemd != null)
                {
                    // Class has had its metadata loaded
                    utilisedFileMetaData.add(filemd);

                    // If not MetaData complete will also merge in annotations in populate()

                    // Retrieve the MetaData for the requested class
                    AbstractClassMetaData the_md = classMetaDataByClass.get(className);

                    return the_md;
                }
            }

            if (allowAnnotations)
            {
                // No JDO XML definition (package.jdo), so check for annotations. Note package-{mapping}.orm will be added later in populate().
                FileMetaData annFilemd = loadAnnotationsForClass(c, clr, true, true);
                if (annFilemd != null)
                {
                    // No MetaData but annotations present so use that
                    if (c.isInterface())
                    {
                        return annFilemd.getPackage(0).getInterface(0);
                    }
                    return annFilemd.getPackage(0).getClass(0);
                }
            }

            // Not found, so add to known classes/interfaces without MetaData
            if (NucleusLogger.METADATA.isDebugEnabled())
            {
                NucleusLogger.METADATA.debug(Localiser.msg("044043", className)); 
            }
            classesWithoutPersistenceInfo.add(className);

            return null;
        }
        finally
        {
            updateLock.unlock();
        }
    }

    /**
     * Accessor for the MetaData for a named query for a class.
     * If the query is not found, will check all valid JDO file locations and try to load it.
     * @param cls The class which has the query defined for it
     * @param clr the ClassLoaderResolver
     * @param queryName Name of the query
     * @return The QueryMetaData for the query for this class
     */
    @Override
    public QueryMetaData getMetaDataForQuery(Class cls, ClassLoaderResolver clr, String queryName)
    {
        QueryMetaData qmd = super.getMetaDataForQuery(cls, clr, queryName);
        if (qmd != null)
        {
            return qmd;
        }

        String query_key = queryName;
        if (cls != null)
        {
            query_key = cls.getName() + "_" + queryName;
        }

        // No query found, so try to load one from a valid JDO location
        if (cls != null)
        {
            // Query is scoped to a candidate class, so load the class as necessary
            AbstractClassMetaData cmd = getMetaDataForClass(cls, clr);
            if (cmd == null)
            {
                // No metadata for this class so no chance of finding the query for it
                return null;
            }

            if (queryMetaDataByName != null)
            {
                Object obj = queryMetaDataByName.get(query_key);
                if (obj != null)
                {
                    return (QueryMetaData)obj;
                }
            }

            if (allowXML)
            {
                // Query not stored in JDO/ORM files so try JDOQUERY
                List<String> locations = new ArrayList<>();
                locations.addAll(getValidMetaDataLocationsForClass(getJDOQueryFileSuffix(), null, cls.getName()));

                for (String location : locations)
                {
                    // Process all resources for this location
                    Enumeration resources;
                    try
                    {
                        resources = clr.getResources(location, cls.getClassLoader());
                    }
                    catch (IOException e)
                    {
                        throw new NucleusException("Error loading resource", e).setFatal();
                    }
                    while (resources.hasMoreElements())
                    {
                        URL fileURL = (URL) resources.nextElement();
                        if (fileMetaDataByURLString.get(fileURL.toString()) == null)
                        {
                            // File hasn't been loaded so load it
                            FileMetaData filemd = parseXmlFile(fileURL);
                            filemd.setType(MetaDataFileType.JDO_QUERY_FILE); // TODO Remove this since set in the parser at <jdoquery>
                            registerFile(fileURL.toString(), filemd, clr);

                            // Populate all classes in this file we've just parsed
                            // TODO Populate the classes found in this file
                        }
                    }
                    cmd = getMetaDataForClass(cls, clr);

                    if (queryMetaDataByName != null)
                    {
                        qmd = queryMetaDataByName.get(query_key);
                        if (qmd != null)
                        {
                            if (NucleusLogger.METADATA.isDebugEnabled())
                            {
                                NucleusLogger.METADATA.debug(Localiser.msg("044053",query_key,location));
                            }
                            return qmd;
                        }
                        if (NucleusLogger.METADATA.isDebugEnabled())
                        {
                            NucleusLogger.METADATA.debug(Localiser.msg("044050",query_key,location));
                        }
                    }
                }
            }
            return null;
        }

        // Query isn't scoped to a candidate class, so search the valid package-independent locations
        List<String> locations = new ArrayList<>();
        locations.addAll(getValidMetaDataLocationsForItem(getJDOFileSuffix(), null, null, false));
        locations.addAll(getValidMetaDataLocationsForItem(getORMFileSuffix(), getORMMappingName(), null, false));
        locations.addAll(getValidMetaDataLocationsForItem(getJDOQueryFileSuffix(), null, null, false));

        for (String location : locations)
        {
            // Process all resources for this location
            Enumeration resources;
            try
            {
                resources = clr.getResources(location, null);
            } 
            catch (IOException e) 
            {
                throw new NucleusException("Error loading resources", e).setFatal();
            }
            while (resources.hasMoreElements())
            {
                URL fileURL = (URL) resources.nextElement();
                if (fileMetaDataByURLString.get(fileURL.toString()) == null)
                {
                    // File hasn't been loaded so load it
                    FileMetaData filemd = parseXmlFile(fileURL);
                    registerFile(fileURL.toString(), filemd, clr);

                    // Populate all classes in this file we've just parsed
                    // TODO Populate the classes found in this file
                }
            }

            if (queryMetaDataByName != null)
            {
                qmd = queryMetaDataByName.get(query_key);
                if (qmd != null)
                {
                    if (NucleusLogger.METADATA.isDebugEnabled())
                    {
                        NucleusLogger.METADATA.debug(Localiser.msg("044053",query_key,location));
                    }
                    return qmd;
                }
            }
            if (NucleusLogger.METADATA.isDebugEnabled())
            {
                NucleusLogger.METADATA.debug(Localiser.msg("044050",query_key,location));
            }
        }
        return null;
    }

    /**
     * Accessor for the MetaData for a Sequence in a package.
     * If the sequence is not yet known will search the valid locations for the passed name.
     * @param clr the ClassLoaderResolver
     * @param packageSequenceName Fully qualified name of the sequence (inc package name)
     * @return The SequenceMetaData for this named sequence
     */
    @Override
    public SequenceMetaData getMetaDataForSequence(ClassLoaderResolver clr, String packageSequenceName)
    {
        SequenceMetaData seqmd = super.getMetaDataForSequence(clr, packageSequenceName);
        if (seqmd != null)
        {
            return seqmd;
        }

        // MetaData not found so maybe just not yet loaded
        String packageName = packageSequenceName;
        if (packageSequenceName.lastIndexOf('.') >= 0)
        {
            packageName = packageSequenceName.substring(0, packageSequenceName.lastIndexOf('.'));
        }

        // Search valid JDO file locations ("jdo" and "orm" files for the specified package)
        List<String> locations = new ArrayList<>();
        locations.addAll(getValidMetaDataLocationsForItem(getJDOFileSuffix(), null, packageName, false));
        locations.addAll(getValidMetaDataLocationsForItem(getORMFileSuffix(), getORMMappingName(), packageName, false));

        for (String location : locations)
        {
            // Process all resources for this location
            Enumeration resources;
            try
            {
                resources = clr.getResources(location, null);
            } 
            catch (IOException e) 
            {
                throw new NucleusException("Error loading resource", e).setFatal();
            }
            while (resources.hasMoreElements()) 
            {
                URL fileURL = (URL) resources.nextElement();
                if (fileMetaDataByURLString.get(fileURL.toString()) == null)
                {
                    // File hasn't been loaded so load it
                    FileMetaData filemd = parseXmlFile(fileURL);
                    registerFile(fileURL.toString(), filemd, clr);

                    // Populate all classes in this file we've just parsed
                    // TODO Populate the classes found in this file
                }
            }

            if (sequenceMetaDataByPackageSequence != null)
            {
                // Try lookup using package name
                seqmd = sequenceMetaDataByPackageSequence.get(packageSequenceName);
            }
            if (seqmd != null)
            {
                if (NucleusLogger.METADATA.isDebugEnabled())
                {
                    NucleusLogger.METADATA.debug(Localiser.msg("044053", packageSequenceName, location));
                }
                return seqmd;
            }
            if (NucleusLogger.METADATA.isDebugEnabled())
            {
                NucleusLogger.METADATA.debug(Localiser.msg("044051", packageSequenceName, location));
            }
        }
        return null;
    }

    /**
     * Load up and add the O/R mapping info for the specified class to the stored JDO ClassMetaData.
     * @param c The class
     * @param clr the ClassLoaderResolver
     */
    @Override
    public void addORMDataToClass(Class c, ClassLoaderResolver clr)
    {
        if (getNucleusContext() instanceof EnhancementNucleusContextImpl)
        {
            // We don't need ORM data when enhancing
            return;
        }
        if (!allowORM)
        {
            // StoreManager doesn't "map" to the datastore so don't use ORM info
            return;
        }

        // Get the JDO MetaData for this class/interface
        AbstractClassMetaData cmd = classMetaDataByClass.get(c.getName());

        // See if we already have a file registered with the ORM metadata for this class
        AbstractClassMetaData ormCmd = ormClassMetaDataByClass.get(c.getName());
        if (ormCmd != null)
        {
            // Merge the ORM class into the JDO class
            MetaDataMerger.mergeClassORMData(cmd, ormCmd, this);

            // Remove it from the map since no longer needed
            ormClassMetaDataByClass.remove(c.getName());

            return;
        }

        if (allowXML)
        {
            // No ORM loaded for this class, so find if there is any ORM metadata available
            FileMetaData filemdORM = loadXMLMetaDataForClass(c, clr, getORMMappingName(), getORMFileSuffix(), MetaDataFileType.JDO_ORM_FILE, false);
            if (filemdORM != null)
            {
                // The ORM file has now been registered, so find the class and merge it into the JDO definition
                ormCmd = ormClassMetaDataByClass.get(c.getName());
                if (ormCmd != null)
                {
                    // Merge the ORM file into the JDO file
                    MetaDataMerger.mergeFileORMData((FileMetaData)cmd.getPackageMetaData().getParent(), (FileMetaData)ormCmd.getPackageMetaData().getParent());

                    // Merge the ORM class into the JDO class
                    MetaDataMerger.mergeClassORMData(cmd, ormCmd, this);

                    // Remove it from the map since no longer needed
                    ormClassMetaDataByClass.remove(c.getName());
                }
            }
        }
    }

    /**
     * Method to find the Meta-Data file for a specified class.
     * Checks the locations one-by-one, and checks for existence of the specified class in the file. 
     * If a valid file is found it is loaded no matter if the file contains the actual class. 
     * When a file is found containing the class the process stops and the FileMetaData for that file (containing the class) returned.
     * <P>
     * Allows 2 variations on the naming above. 
     * The first is a modifier which caters for a JDO 2.0 requirement whereby the user can specify a modifier such as "mysql", 
     * which would mean that this should search for filenames "package-mysql.jdo". The second variation is the suffix of the file.
     * This is "jdo" by default, but JDO 2.0 has situations where "orm", or "jdoquery" are required as a suffix.
     * </P>
     * @param pc_class The class/interface to retrieve the metadata file for
     * @param clr the ClassLoaderResolver
     * @param mappingModifier Any modifier for the filename for mapping
     * @param metadataFileExtension File extension of metadata files (e.g "jdo")
     * @param metadataType Type of metadata file to load
     * @param populate Whether to populate any loaded MetaData classes
     * @return FileMetaData for the file containing the class
     */
    protected FileMetaData loadXMLMetaDataForClass(Class pc_class, ClassLoaderResolver clr,
            String mappingModifier, String metadataFileExtension, MetaDataFileType metadataType, boolean populate)
    {
        // MetaData file locations
        List<String> validLocations = getValidMetaDataLocationsForClass(metadataFileExtension, mappingModifier, pc_class.getName());
        for (String location : validLocations)
        {
            Enumeration resources;
            try 
            {
                resources = clr.getResources(location, pc_class.getClassLoader());
            } 
            catch (IOException e) 
            {
                throw new NucleusException("Error loading resource", e).setFatal();
            }
            if (!resources.hasMoreElements() && NucleusLogger.METADATA.isDebugEnabled())
            {
                NucleusLogger.METADATA.debug(Localiser.msg("044049", metadataFileExtension, pc_class.getName(), location));
            }

            while (resources.hasMoreElements())
            {
                URL url = (URL) resources.nextElement();
                if (url != null)
                {
                    // Check if we already have this file parsed/registered
                    FileMetaData filemd = fileMetaDataByURLString.get(url.toString());
                    if (filemd == null)
                    {
                        // Not registered so load the file from the URL
                        filemd = parseXmlFile(url);
                        if (filemd.getType() != metadataType)
                        {
                            // Wrong type of file so ignore it
                            NucleusLogger.METADATA.warn(Localiser.msg("044045", url, filemd.getType(), metadataType));
                            filemd = null;
                            break;
                        }

                        registerFile(url.toString(), filemd, clr);
                        if (populate)
                        {
                            // Populate all classes in this file we've just parsed
                            populateFileMetaData(filemd, clr, pc_class.getClassLoader());
                        }
                    }

                    if (((filemd.getType() == MetaDataFileType.JDO_FILE && classMetaDataByClass.get(pc_class.getName()) != null) ||
                         (filemd.getType() == MetaDataFileType.JDO_ORM_FILE && ormClassMetaDataByClass.get(pc_class.getName()) != null)))
                    {
                        // We now have the class, so it must have been in this file
                        if (NucleusLogger.METADATA.isDebugEnabled())
                        {
                            NucleusLogger.METADATA.debug(Localiser.msg("044052", metadataFileExtension, pc_class.getName(), url));
                        }
                        return filemd;
                    }
                }
            }
        }

        if (NucleusLogger.METADATA.isDebugEnabled())
        {
            NucleusLogger.METADATA.debug(Localiser.msg("044048", metadataFileExtension, pc_class.getName()));
        }
        return null;
    }

    /**
     * Method to return the valid metadata locations to contain a particular package.
     * @param fileExtension File extension (e.g "jdo")
     * @param fileModifier Any modifier (for use when using ORM files package-mysql.orm, this is the "mysql" part)
     * @param packageName The package name to look for
     * @return The list of valid locations
     */
    public List<String> getValidMetaDataLocationsForPackage(String fileExtension, String fileModifier, String packageName)
    {
        return getValidMetaDataLocationsForItem(fileExtension, fileModifier, packageName, false);
    }

    /**
     * Method to return the valid metadata locations to contain a particular class.
     * @param fileExtension File extension (e.g "jdo")
     * @param fileModifier Any modifier (for use when using ORM files package-mysql.orm, this is the "mysql" part)
     * @param className The class name to look for
     * @return The list of valid locations
     */
    public List<String> getValidMetaDataLocationsForClass(String fileExtension, String fileModifier, String className)
    {
        return getValidMetaDataLocationsForItem(fileExtension, fileModifier, className, true);
    }

    /**
     * Method to return the valid metadata locations to contain a particular item. 
     * The "item" can be a package or a class. Will look in the locations appropriate for the setting of "locationDefintion".
     * @param fileExtension File extension (e.g "jdo") accepts comma separated list
     * @param fileModifier Any modifier (for use when using ORM files package-mysql.orm, this is the "mysql" part)
     * @param itemName The name of the item (package or class)
     * @param isClass Whether this is a class
     * @return The list of valid locations
     */
    List<String> getValidMetaDataLocationsForItem(String fileExtension, String fileModifier, String itemName, boolean isClass)
    {
        // Build up a list of valid locations
        List<String> locations = new ArrayList<>();

        if (fileExtension == null)
        {
            fileExtension = "jdo";
        }
        StringTokenizer tokens = new StringTokenizer(fileExtension, ",");
        while (tokens.hasMoreTokens())
        {
            locations.addAll(getValidMetaDataLocationsForSingleExtension(tokens.nextToken(), fileModifier, itemName, isClass));
        }
        return locations;
    }

    // Parameters used in the definition of MetaData file location
    private static final char CLASS_SEPARATOR = '.';
    private static final char PATH_SEPARATOR = '/';
    private static final char EXTENSION_SEPARATOR = '.';
    private static final String METADATA_PACKAGE = "package";
    private static final String METADATA_LOCATION_METAINF = "/META-INF/" + METADATA_PACKAGE;
    private static final String METADATA_LOCATION_WEBINF = "/WEB-INF/" + METADATA_PACKAGE;

    /**
     * Method to return the valid metadata locations to contain a particular item. 
     * The "item" can be a package or a class. Will look in the locations appropriate for the setting of "locationDefintion".
     * @param fileExtension File extension (e.g "jdo")
     * @param fileModifier Any modifier (for use when using ORM files package-mysql.orm, this is the "mysql" part)
     * @param itemName The name of the item (package or class)
     * @param isClass Whether this is a class
     * @return The list of valid locations
     */
    private List<String> getValidMetaDataLocationsForSingleExtension(String fileExtension, String fileModifier, String itemName, boolean isClass)
    {
        // Build up a list of valid locations
        List<String> locations = new ArrayList<>();

        String suffix = null;
        if (fileExtension == null)
        {
            fileExtension = "jdo";
        }
        if (fileModifier != null)
        {
            // This will be something like "-mysql.orm" (suffix for ORM files)
            suffix = "-" + fileModifier + EXTENSION_SEPARATOR + fileExtension;
        }
        else
        {
            suffix = EXTENSION_SEPARATOR + fileExtension;
        }

        locations.add((METADATA_LOCATION_METAINF + suffix)); // "/META-INF/package.jdo"
        locations.add((METADATA_LOCATION_WEBINF + suffix)); // "/WEB-INF/package.jdo"
        locations.add(PATH_SEPARATOR + METADATA_PACKAGE + suffix); // "/package.jdo"

        if (itemName != null && itemName.length() > 0)
        {
            int separatorPosition = itemName.indexOf('.');
            if (separatorPosition < 0)
            {
                // No dot, so just use top-level location(s)
                // "/com/package.jdo"
                locations.add(PATH_SEPARATOR + itemName + PATH_SEPARATOR + METADATA_PACKAGE + suffix);
                if (allowXmlLocationsFromJDO1_0)
                {
                    // "/com.jdo" (JDO 1.0.0)
                    locations.add(PATH_SEPARATOR + itemName + suffix);
                }
            }
            else
            {
                while (separatorPosition >= 0)
                {
                    String name = itemName.substring(0, separatorPosition);

                    // "/com/xyz/package.jdo"
                    locations.add(PATH_SEPARATOR + name.replace(CLASS_SEPARATOR, PATH_SEPARATOR) + PATH_SEPARATOR + METADATA_PACKAGE + suffix);

                    if (allowXmlLocationsFromJDO1_0)
                    {
                        // "/com/xyz.jdo" (JDO 1.0.0)
                        locations.add(PATH_SEPARATOR + name.replace(CLASS_SEPARATOR, PATH_SEPARATOR) + suffix);
                    }

                    separatorPosition = itemName.indexOf('.', separatorPosition+1);
                    if (separatorPosition < 0)
                    {
                        if (!isClass)
                        {
                            // "/com/xyz/uvw/package.jdo"
                            locations.add(PATH_SEPARATOR + itemName.replace(CLASS_SEPARATOR, PATH_SEPARATOR) + PATH_SEPARATOR + METADATA_PACKAGE + suffix);

                            if (allowXmlLocationsFromJDO1_0)
                            {
                                // "/com/xyz/uvw.jdo"
                                locations.add(PATH_SEPARATOR + itemName.replace(CLASS_SEPARATOR, PATH_SEPARATOR) + suffix);
                            }
                        }
                        else
                        {
                            // "/com/xyz/uvw.jdo"
                            locations.add(PATH_SEPARATOR + itemName.replace(CLASS_SEPARATOR, PATH_SEPARATOR) + suffix);
                        }
                    }
                }
            }
        }

        return locations;
    }

    /**
     * Convenience accessor for the mapping name.
     * @return ORM mapping name
     */
    private String getORMMappingName()
    {
        String mappingName = nucleusContext.getConfiguration().getStringProperty(PropertyNames.PROPERTY_MAPPING);
        return (StringUtils.isWhitespace(mappingName) ? null : mappingName);
    }

    /**
     * Convenience accessor for the JDO file suffix.
     * @return JDO file suffix
     */
    private String getJDOFileSuffix()
    {
        String suffix = nucleusContext.getConfiguration().getStringProperty(PropertyNames.PROPERTY_METADATA_JDO_SUFFIX);
        return (StringUtils.isWhitespace(suffix) ? "jdo" : suffix);
    }

    /**
     * Convenience accessor for the ORM file suffix.
     * @return ORM file suffix
     */
    private String getORMFileSuffix()
    {
        String suffix = nucleusContext.getConfiguration().getStringProperty(PropertyNames.PROPERTY_METADATA_ORM_SUFFIX);
        return (StringUtils.isWhitespace(suffix) ? "orm" : suffix);
    }

    /**
     * Convenience accessor for the JDOQuery file suffix.
     * @return JDOQuery file suffix
     */
    private String getJDOQueryFileSuffix()
    {
        String suffix = nucleusContext.getConfiguration().getStringProperty(PropertyNames.PROPERTY_METADATA_JDOQUERY_SUFFIX);
        return (StringUtils.isWhitespace(suffix) ? "jdoquery" : suffix);
    }

    // ------------------------------- Persistent Interfaces ---------------------------------------

    /**
     * Main accessor for the MetaData for a "persistent-interface".
     * All MetaData returned from this method will be initialised and ready for full use.
     * @param c The interface to find MetaData for
     * @param clr the ClassLoaderResolver
     * @return The InterfaceMetaData for this interface (or null if not found)
     */
    public InterfaceMetaData getMetaDataForInterface(Class c, ClassLoaderResolver clr)
    {
        if (c == null || !c.isInterface())
        {
            return null;
        }

        InterfaceMetaData imd = (InterfaceMetaData)getMetaDataForClassInternal(c, clr);
        if (imd != null)
        {
            // Make sure that anything returned is populated/initialised
            populateAbstractClassMetaData(imd, clr, c.getClassLoader());
            initialiseAbstractClassMetaData(imd, clr);

            if (!utilisedFileMetaData.isEmpty())
            {
                // Initialise all FileMetaData that were processed in this call
                Iterator iter = utilisedFileMetaData.iterator();
                while (iter.hasNext())
                {
                    FileMetaData filemd = (FileMetaData)iter.next();
                    initialiseFileMetaData(filemd, clr, c.getClassLoader());
                }
            }
        }

        utilisedFileMetaData.clear();
        return imd;
    }

    /**
     * Convenience method to return if the passed class name is a "persistent-interface".
     * @param name Name if the interface
     * @return Whether it is a "persistent-interface"
     */
    public boolean isPersistentInterface(String name)
    {
        // Find if this class has <interface> metadata
        AbstractClassMetaData acmd = classMetaDataByClass.get(name);
        return (acmd != null && acmd instanceof InterfaceMetaData);
    }

    /**
     * Convenience method to return if the passed class name is an implementation of the passed "persistent-interface".
     * @param interfaceName Name of the persistent interface
     * @param implName The implementation name
     * @return Whether it is a (generated) impl of the persistent interface
     */
    public boolean isPersistentInterfaceImplementation(String interfaceName, String implName)
    {
        ClassMetaData cmd = (ClassMetaData)classMetaDataByInterface.get(interfaceName);
        return (cmd != null && cmd.getFullClassName().equals(implName));
    }

    /**
     * Accessor for the implementation name for the specified "persistent-interface".
     * @param interfaceName The name of the persistent interface
     * @return The name of the implementation class
     */
    public String getImplementationNameForPersistentInterface(String interfaceName)
    {
        ClassMetaData cmd = (ClassMetaData)classMetaDataByInterface.get(interfaceName);
        return (cmd != null ? cmd.getFullClassName() : null);
    }

    /**
     * Accessor for the metadata for the implementation of the specified "persistent-interface".
     * @param interfaceName The name of the persistent interface
     * @return The ClassMetaData of the implementation class
     */
    public ClassMetaData getClassMetaDataForImplementationOfPersistentInterface(String interfaceName)
    {
        return (ClassMetaData)classMetaDataByInterface.get(interfaceName);
    }

    /**
     * Method to register a persistent interface and its implementation with the MetaData system.
     * @param imd MetaData for the interface
     * @param implClass The implementation class
     * @param clr ClassLoader Resolver to use
     */
    public void registerPersistentInterface(InterfaceMetaData imd, Class implClass, ClassLoaderResolver clr)
    {
        // Create ClassMetaData for the implementation
        ClassMetaData cmd = new ClassMetaData(imd, ClassUtils.getClassNameForClass(implClass), true);
        cmd.addImplements(new ImplementsMetaData(imd.getFullClassName()));

        // Register the ClassMetaData for the implementation
        registerMetaDataForClass(cmd.getFullClassName(), cmd);

        // Register the metadata for the implementation against this persistent interface
        classMetaDataByInterface.put(imd.getFullClassName(), cmd);

        initialiseClassMetaData(cmd, implClass, clr);

        // Deregister the metadata for the implementation from those "not found"
        if (NucleusLogger.METADATA.isDebugEnabled())
        {
            NucleusLogger.METADATA.debug(Localiser.msg("044044",implClass.getName())); 
        }
        classesWithoutPersistenceInfo.remove(implClass.getName());
    }

    /**
     * Method to register the metadata for an implementation of a persistent abstract class.
     * @param cmd MetaData for the abstract class
     * @param implClass The implementation class
     * @param clr ClassLoader resolver
     */
    public void registerImplementationOfAbstractClass(ClassMetaData cmd, Class implClass, ClassLoaderResolver clr)
    {
        ClassMetaData implCmd = new ClassMetaData(cmd, ClassUtils.getClassNameForClass(implClass));

        // Register the ClassMetaData for the implementation
        registerMetaDataForClass(implCmd.getFullClassName(), implCmd);
        initialiseClassMetaData(implCmd, implClass, clr);

        // Deregister the metadata for the implementation from those "not found"
        if (NucleusLogger.METADATA.isDebugEnabled())
        {
            NucleusLogger.METADATA.debug(Localiser.msg("044044", implClass.getName())); 
        }
        classesWithoutPersistenceInfo.remove(implClass.getName());
    }
}