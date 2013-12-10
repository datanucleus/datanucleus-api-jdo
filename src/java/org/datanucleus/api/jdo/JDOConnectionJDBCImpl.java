/**********************************************************************
Copyright (c) 2012 Andy Jefferson and others. All rights reserved.
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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.datanucleus.store.NucleusConnection;

/**
 * Implementation of a JDO connection that is also a wrapper for "java.sql.Connection".
 */
public class JDOConnectionJDBCImpl extends JDOConnectionImpl implements Connection
{
    /** The JDBC connection (shortcut for the native connection of the NucleusConnection). **/
    private final Connection conn;

    /** whether this connection is available to the developer */
    private boolean isAvailable = true;

    /**
     * Constructor.
     * @param nconn The Nucleus Connection delegate
     */
    public JDOConnectionJDBCImpl(NucleusConnection nconn)
    {
        super(nconn);
        conn = (Connection) nconn.getNativeConnection();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.NucleusConnection#isAvailable()
     */
    public boolean isAvailable()
    {
        return nucConn.isAvailable();
    }

    /**
     * Access the holdability of the connection.
     * @return The holdability
     * @throws SQLException if there is a problem
     */
    public int getHoldability() throws SQLException
    {
        assertAvailable();
        return this.conn.getHoldability();
    }

    /**
     * Accessor for the transaction isolation level.
     * @return The isolation level
     * @throws SQLException if there is a problem
     */
    public int getTransactionIsolation() throws SQLException
    {
        assertAvailable();
        return this.conn.getTransactionIsolation();
    }

    /**
     * Method to clear warnings from the connection.
     * @throws SQLException thrown if there is a problem
     */
    public void clearWarnings() throws SQLException
    {
        assertAvailable();
        this.conn.clearWarnings();
    }

    /**
     * Method to commit the connection.
     * @throws SQLException thrown if an error occurs.
     */
    public void commit() throws SQLException
    {
        super.throwExceptionUnsupportedOperation("commit");
    }

    /**
     * Method to rollback the connection.
     * @throws SQLException thrown if an error occurs.
     */
    public void rollback() throws SQLException
    {
        super.throwExceptionUnsupportedOperation("rollback");
    }

    /**
     * Accessor for the autocommit setting.
     * @return Whether to autocommit
     * @throws SQLException thrown if an error occurs.
     */
    public boolean getAutoCommit() throws SQLException
    {
        assertAvailable();
        return this.conn.getAutoCommit();
    }

    /**
     * Accessor for whether the connection is closed.
     * @return Whether it is closed.
     * @throws SQLException thrown if an error occurs.
     */
    public boolean isClosed() throws SQLException
    {
        if (nucConn.isAvailable())
        {
            // When the connection is available we use the underlying connection response
            return this.conn.isClosed();
        }
        return true;
    }

    /**
     * Accessor for whether the connection is read only.
     * @return Whether it is read only.
     * @throws SQLException thrown if an error occurs.
     */
    public boolean isReadOnly() throws SQLException
    {
        assertAvailable();
        return this.conn.isReadOnly();
    }

    /**
     * Method to set the holdability for the connection.
     * @param holdability The holdability
     * @throws SQLException thrown if an error occurs.
     */
    public void setHoldability(int holdability) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setHoldability");
    }

    /**
     * Method to set the transaction isolation for the connection.
     * @param level The transaction isolation
     * @throws SQLException thrown if an error occurs.
     */
    public void setTransactionIsolation(int level) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setTransactionIsolation");
    }

    /**
     * Method to set the autocommit for the connection.
     * @param autoCommit Whether to autocommit
     * @throws SQLException thrown if an error occurs.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setAutoCommit");
    }

    /**
     * Method to set the read-only nature for the connection.
     * @param readOnly whether it is read-only
     * @throws SQLException thrown if an error occurs.
     */
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setReadOnly");
    }

    /**
     * Accessor for the catalog.
     * @return The catalog
     * @throws SQLException Thrown if an error occurs
     */
    public String getCatalog() throws SQLException
    {
        assertAvailable();
        return this.conn.getCatalog();
    }

    /**
     * Mutator for the catalog
     * @param catalog The catalog
     * @throws SQLException Thrown if an error occurs
     */
    public void setCatalog(String catalog) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setCatalog");
    }

    /**
     * Accessor for the database metadata.
     * @return The database metadata
     * @throws SQLException Thrown if an error occurs
     */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        super.throwExceptionUnsupportedOperation("getMetaData");
        return null;
    }

    /**
     * Accessor for the warnings.
     * @return The warnings
     * @throws SQLException Thrown if an error occurs
     */
    public SQLWarning getWarnings() throws SQLException
    {
        assertAvailable();
        return this.conn.getWarnings();
    }

    /**
     * Mutator to set the savepoint.
     * @return The savepoint
     * @throws SQLException Thrown if an error occurs
     */
    public Savepoint setSavepoint() throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setSavepoint");
        return null;
    }

    /**
     * Mutator to release the savepoint.
     * @param pt The savepoint
     * @throws SQLException Thrown if an error occurs
     */
    public void releaseSavepoint(Savepoint pt) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("releaseSavepoint");
    }

    /**
     * Mutator to rollback the savepoint.
     * @param pt The savepoint
     * @throws SQLException Thrown if an error occurs
     */
    public void rollback(Savepoint pt) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("rollback");
    }

    /**
     * Accessor for a statement.
     * @return The statement
     * @throws SQLException Thrown if an error occurs
     */
    public Statement createStatement() throws SQLException
    {
        assertAvailable();
        return this.conn.createStatement();
    }

    /**
     * Accessor for a statement.
     * @param resultSetType type of results
     * @param resultSetConcurrency The concurrency
     * @return The statement
     * @throws SQLException Thrown if an error occurs
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        assertAvailable();
        return this.conn.createStatement(resultSetType,resultSetConcurrency);
    }

    /**
     * Accessor for a statement.
     * @param resultSetType type of results
     * @param resultSetConcurrency The concurrency
     * @param resultSetHoldability The holdability
     * @return The statement
     * @throws SQLException Thrown if an error occurs
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        assertAvailable();
        return this.conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Accessor for the type map.
     * @return The type map
     * @throws SQLException Thrown if an error occurs
     */
    public Map getTypeMap() throws SQLException
    {
        assertAvailable();
        return this.conn.getTypeMap();
    }

    /**
     * Method to set the type map
     * @param map The type map
     * @throws SQLException Thrown if an error occurs
     */
    public void setTypeMap(Map map) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setTypeMap");
    }

    /**
     * Accessor for using native SQL.
     * @param sql The sql
     * @return The native SQL
     * @throws SQLException Thrown if an error occurs
     */
    public String nativeSQL(String sql) throws SQLException
    {
        assertAvailable();
        return this.conn.nativeSQL(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareCall(sql);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareCall(sql,resultSetType,resultSetConcurrency);
    }

    public CallableStatement prepareCall(String arg0, int arg1, int arg2, int arg3) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareCall(arg0,arg1,arg2,arg3);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(String arg0, int arg1) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(arg0,arg1);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(sql,resultSetType,resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2, int arg3) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(arg0,arg1,arg2,arg3);
    }

    public PreparedStatement prepareStatement(String arg0, int[] arg1) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(arg0,arg1);
    }

    public Savepoint setSavepoint(String arg0) throws SQLException
    {
        super.throwExceptionUnsupportedOperation("setSavepoint");
        return null;
    }

    public PreparedStatement prepareStatement(String arg0, String[] arg1) throws SQLException
    {
        assertAvailable();
        return this.conn.prepareStatement(arg0,arg1);
    }

    /**
     * Assert the JDOConnection is available
     * @throws Exception if the connection is no longer available
     */
    public void assertAvailable()
    {
        if (!isAvailable)
        {
            throwExceptionNotAvailable();
        }
    }

    // JDK 1.6 methods

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        return conn.createArrayOf(typeName, elements);
    }

    public Blob createBlob() throws SQLException
    {
        return conn.createBlob();
    }

    public Clob createClob() throws SQLException
    {
        return conn.createClob();
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        return conn.createStruct(typeName, attributes);
    }

    public Properties getClientInfo() throws SQLException
    {
        return conn.getClientInfo();
    }

    public String getClientInfo(String name) throws SQLException
    {
        return conn.getClientInfo(name);
    }

    public boolean isValid(int timeout) throws SQLException
    {
        return conn.isValid(timeout);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException
    {
        try
        {
            conn.setClientInfo(properties);
        }
        catch (Exception e)
        {
            // Ignore this
        }
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException
    {
        try
        {
            conn.setClientInfo(name, value);
        }
        catch (Exception e)
        {
            // Ignore this
        }
    }

    public java.sql.NClob createNClob() throws SQLException
    {
        return conn.createNClob();
    }

    public java.sql.SQLXML createSQLXML() throws SQLException
    {
        return conn.createSQLXML();
    }

    // Implementation of JDBC 4.0's Wrapper interface

    public boolean isWrapperFor(Class iface) throws SQLException
    {
        return Connection.class.equals(iface);
    }

    public Object unwrap(Class iface) throws SQLException
    {
        if (!Connection.class.equals(iface))
        {
            throw new SQLException("Connection of type [" + getClass().getName() +
                   "] can only be unwrapped as [java.sql.Connection], not as [" + iface.getName() + "]");
        }
        return this;
    }

    public void setSchema(String schema) throws SQLException
    {
//        conn.setSchema(schema);
    }

    public String getSchema() throws SQLException
    {
//        return conn.getSchema();
        return null;
    }

    public void abort(Executor executor) throws SQLException
    {
//        conn.abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
    {
//        conn.setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException
    {
//        return conn.getNetworkTimeout();
        return 0;
    }
}
