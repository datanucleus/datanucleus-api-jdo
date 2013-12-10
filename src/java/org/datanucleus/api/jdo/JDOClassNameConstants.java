/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.identity.ByteIdentity;
import javax.jdo.identity.CharIdentity;
import javax.jdo.identity.IntIdentity;
import javax.jdo.identity.LongIdentity;
import javax.jdo.identity.ObjectIdentity;
import javax.jdo.identity.ShortIdentity;
import javax.jdo.identity.StringIdentity;
import javax.jdo.spi.PersistenceCapable;

/**
 * Class providing names of common JDO classes to aid performance.
 */
public class JDOClassNameConstants
{
    /** javax.jdo.identity.LongIdentity **/
    public static final String JAVAX_JDO_IDENTITY_LONG_IDENTITY = LongIdentity.class.getName();
    /** javax.jdo.identity.IntIdentity **/
    public static final String JAVAX_JDO_IDENTITY_INT_IDENTITY = IntIdentity.class.getName();
    /** javax.jdo.identity.StringIdentity **/
    public static final String JAVAX_JDO_IDENTITY_STRING_IDENTITY = StringIdentity.class.getName();
    /** javax.jdo.identity.CharIdentity **/
    public static final String JAVAX_JDO_IDENTITY_CHAR_IDENTITY = CharIdentity.class.getName();
    /** javax.jdo.identity.ByteIdentity **/
    public static final String JAVAX_JDO_IDENTITY_BYTE_IDENTITY = ByteIdentity.class.getName();
    /** javax.jdo.identity.ObjectIdentity **/
    public static final String JAVAX_JDO_IDENTITY_OBJECT_IDENTITY = ObjectIdentity.class.getName();
    /** javax.jdo.identity.ShortIdentity **/
    public static final String JAVAX_JDO_IDENTITY_SHORT_IDENTITY = ShortIdentity.class.getName();
    /** javax.jdo.PersistenceManagerFactory **/
    public static final String JAVAX_JDO_PersistenceManagerFactory = PersistenceManagerFactory.class.getName();
    /** javax.jdo.spi.PersistenceCapable **/
    public static final String JAVAX_JDO_SPI_PERSISTENCE_CAPABLE = PersistenceCapable.class.getName();

    /** org.datanucleus.api.jdo.JDOPersistenceManagerFactory **/
    public static final String JDOPersistenceManagerFactory = JDOPersistenceManagerFactory.class.getName();
}