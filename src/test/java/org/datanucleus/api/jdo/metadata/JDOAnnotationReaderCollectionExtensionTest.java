package org.datanucleus.api.jdo.metadata;

import java.util.List;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import junit.framework.TestCase;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.PersistenceNucleusContextImpl;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;

/**
 * Tests that vendor extensions on collection/map fields are propagated to
 * the container (CollectionMetaData / MapMetaData) by the annotation reader.
 */
public class JDOAnnotationReaderCollectionExtensionTest extends TestCase
{
    public JDOAnnotationReaderCollectionExtensionTest(String name)
    {
        super(name);
    }

    /**
     * Model class with a collection field annotated with a vendor extension.
     */
    @PersistenceCapable
    public static class ExtensionCollectionModel
    {
        @Persistent
        @Extension(vendorName = "datanucleus", key = "cache", value = "false")
        List<String> tags;
    }

    /**
     * Verify that @Extension(key="cache", value="false") on a collection field
     * reaches the CollectionMetaData.
     */
    public void testExtensionPropagatedToCollectionMetaData()
    {
        PersistenceNucleusContextImpl ctx = new PersistenceNucleusContextImpl("JDO", null);
        MetaDataManager mgr = new JDOMetaDataManager(ctx);
        ClassLoaderResolver clr = ctx.getClassLoaderResolver(getClass().getClassLoader());

        mgr.loadClasses(new String[] { ExtensionCollectionModel.class.getName() }, getClass().getClassLoader());

        AbstractClassMetaData cmd = mgr.getMetaDataForClass(ExtensionCollectionModel.class, clr);
        AbstractMemberMetaData mmd = cmd.getMetaDataForMember("tags");

        assertNotNull("Member metadata for 'tags' should not be null", mmd);
        assertNotNull("Collection metadata should not be null", mmd.getCollection());
        assertTrue("Collection metadata should have 'cache' extension",
                mmd.getCollection().hasExtension("cache"));
        assertEquals("Collection 'cache' extension should be 'false'",
                "false", mmd.getCollection().getValueForExtension("cache"));
    }
}
