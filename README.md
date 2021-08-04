# datanucleus-api-jdo

Support for DataNucleus persistence using the [JDO API](http://db.apache.org/jdo) (JSR0012, JSR0243).

This is built using Maven, by executing `mvn clean install` which installs the built jar in your local Maven repository.


## KeyFacts

__License__ : Apache 2 licensed  
__Issue Tracker__ : http://github.com/datanucleus/datanucleus-api-jdo/issues  
__Javadocs__ : [6.0](http://www.datanucleus.org/javadocs/api.jdo/6.0/), [5.2](http://www.datanucleus.org/javadocs/api.jdo/5.2/), [5.1](http://www.datanucleus.org/javadocs/api.jdo/5.1/), [5.0](http://www.datanucleus.org/javadocs/api.jdo/5.0/), [4.2](http://www.datanucleus.org/javadocs/api.jdo/4.2/), [4.1](http://www.datanucleus.org/javadocs/api.jdo/4.1/), [4.0](http://www.datanucleus.org/javadocs/api.jdo/4.0/)  
__Download__ : [Maven Central](https://repo1.maven.org/maven2/org/datanucleus/datanucleus-api-jdo)  
__Dependencies__ : See file [pom.xml](pom.xml)  
__Support__ : [DataNucleus Support Page](http://www.datanucleus.org/support.html)  


## JDO Future

The Apache JDO project only develops new features relatively slowly, whilst providing an issue tracker for people to request new features.
DataNucleus has gone ahead and developed support for the following features beyond the current standard JDO 3.2 release.

These issues are supported from DN 5.0+

[JDO-483](https://issues.apache.org/jira/projects/JDO/issues/JDO-483) : Add JDOHelper.isLoaded methods. See NucleusJDOHelper.  
[JDO-589](https://issues.apache.org/jira/projects/JDO/issues/JDO-589) : Allow makePersistent and deletePersistent outside a transaction. Done.  
[JDO-610](https://issues.apache.org/jira/projects/JDO/issues/JDO-610) : Support Nested Transaction or savePoint in JDO 2.3. Savepoints added for RDBMS.  
[JDO-625](https://issues.apache.org/jira/projects/JDO/issues/JDO-625) : Support for streams. Support for 'File' with RDBMS.  
[JDO-677](https://issues.apache.org/jira/projects/JDO/issues/JDO-677) : Ability to mark a class as read-only. Done.  
[JDO-697](https://issues.apache.org/jira/projects/JDO/issues/JDO-697) : Support integration with javax.validation (JSR303). Done.  
[JDO-703](https://issues.apache.org/jira/projects/JDO/issues/JDO-703) : Support datastore multitenancy. Done.  
[JDO-727](https://issues.apache.org/jira/projects/JDO/issues/JDO-727) : Provide definition of default table/column/FK/PK/index identifier names. DatastoreIdentifier for RDBMS and NamingFactory for non-RDBMS.  


These issues are supported from DN 6.0+

[JDO-638](https://issues.apache.org/jira/projects/JDO/issues/JDO-638) : Add annotations for instance callbacks. Done.  
[JDO-683](https://issues.apache.org/jira/projects/JDO/issues/JDO-683) : Allow version field/property to be visible to application. Done.  
