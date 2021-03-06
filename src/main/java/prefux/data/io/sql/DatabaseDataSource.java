/*  
 * Copyright (c) 2004-2013 Regents of the University of California.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3.  Neither the name of the University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * Copyright (c) 2014 Martin Stockhammer
 */
package prefux.data.io.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import prefux.data.Table;
import prefux.data.io.DataIOException;

/**
 * Sends queries to a relational database and processes the results, storing
 * the results in prefux Table instances. This class should not be
 * instantiated directly. To access a database, the {@link ConnectionFactory}
 * class should be used to retrieve an appropriate instance of this class.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class DatabaseDataSource {

    // logger
    private static final Logger s_logger 
        = Logger.getLogger(DatabaseDataSource.class.getName());
    
    protected Connection       m_conn;
    protected Statement        m_stmt;
    DatabaseResultSetProcessor m_resultSetProcessor;
    
    // ------------------------------------------------------------------------
    
    /**
     * Creates a new DatabaseDataSource for reading data from a SQL relational
     * database. This constructor is only package visible and is not intended
     * for use by application level code. Instead, the
     * {@link ConnectionFactory} class should be used to create any number of
     * DatabaseDataSource connections.
     */
    DatabaseDataSource(Connection conn, SQLDataHandler handler) {
        m_conn = conn;
        m_resultSetProcessor = new DatabaseResultSetProcessor(handler);
    }
    
    // ------------------------------------------------------------------------
    // Synchronous Data Retrieval
    
    /**
     * Executes a query and returns the results in a Table instance.
     * @param query the text SQL query to execute
     * @return a Table of the query results
     * @throws DataIOException if an error occurs while executing the query 
     * or adding the query results in a prefux Table.
     */
    public synchronized Table getData(String query) throws DataIOException {
        return getData(null, query, null);
    }

    /**
     * Executes a query and returns the results in a Table instance.
     * @param query the text SQL query to execute
     * @param keyField the field to treat as a primary key, ensuring that this
     *  field is indexed in the resulting table instance.
     * @return a Table of the query results
     * @throws DataIOException if an error occurs while executing the query 
     * or adding the query results in a prefux Table.
     */
    public synchronized Table getData(String query, String keyField)
        throws DataIOException
    {
        return getData(null, query, keyField);
    }
    
    /**
     * Executes a query and returns the results in a Table instance.
     * @param t the Table to store the results in. If this value is null, a
     * new table will automatically be created.
     * @param query the text SQL query to execute
     * @return a Table of the query results
     * @throws DataIOException if an error occurs while executing the query 
     * or adding the query results in a prefux Table.
     */
    public synchronized Table getData(Table t, String query) 
        throws DataIOException
    {
        return getData(t, query, null);
    }
    
    /**
     * Executes a query and returns the results in a Table instance.
     * @param t the Table to store the results in. If this value is null, a
     * new table will automatically be created.
     * @param query the text SQL query to execute
     * @param keyField used to determine if the row already exists in the table
     * @return a Table of the query results
     * @throws DataIOException if an error occurs while executing the query 
     * or adding the query results in a prefux Table.
     */
    public synchronized Table getData(Table t, String query, String keyField) 
        throws DataIOException
    {
        return getData(t, query, keyField, null);
    }
    
    /**
     * Executes a query and returns the results in a Table instance.
     * @param t the Table to store the results in. If this value is null, a
     * new table will automatically be created.
     * @param query the text SQL query to execute
     * @param keyField used to determine if the row already exists in the table
     * @param lock an optional Object to use as a lock when performing data
     *  processing. This lock will be synchronized on whenever the Table is
     *  modified.
     * @return a Table of the query results
     * @throws DataIOException if an error occurs while executing the query 
     * or adding the query results in a prefux Table.
     */
    public synchronized Table getData(Table t, String query, 
                                      String keyField, Object lock) 
        throws DataIOException
    {
        ResultSet rs;
        try {
            rs = executeQuery(query);
        } catch ( SQLException e ) {
            throw new DataIOException(e);
        }
        return m_resultSetProcessor.process(t, rs, keyField, lock, false);
    }
    
    // ------------------------------------------------------------------------
    // Asynchronous Data Retrieval

    /**
     * Asynchronously executes a query and stores the results in the given 
     * table instance. All data processing is done in a separate thread of
     * execution.
     * @param t the Table in which to store the results
     * @param query the query to execute
     */
    public void loadData(Table t, String query) {
        loadData(t, query, null, null, null);
    }

    /**
     * Asynchronously executes a query and stores the results in the given 
     * table instance. All data processing is done in a separate thread of
     * execution.
     * @param t the Table in which to store the results
     * @param query the query to execute
     * @param keyField the primary key field, comparisons on this field are
     *  performed to recognize data records already present in the table.
     */
    public void loadData(Table t, String query, String keyField) {
        loadData(t, query, keyField, null, null);
    }
    
    /**
     * Asynchronously executes a query and stores the results in the given 
     * table instance. All data processing is done in a separate thread of
     * execution.
     * @param t the Table in which to store the results
     * @param query the query to execute
     * @param lock an optional Object to use as a lock when performing data
     *  processing. This lock will be synchronized on whenever the Table is
     *  modified.
     */
    public void loadData(Table t, String query, Object lock) {
        loadData(t, query, null, lock, null);
    }
    
    /**
     * Asynchronously executes a query and stores the results in the given 
     * table instance. All data processing is done in a separate thread of
     * execution.
     * @param t the Table in which to store the results
     * @param query the query to execute
     * @param keyField the primary key field, comparisons on this field are
     *  performed to recognize data records already present in the table.
     * @param lock an optional Object to use as a lock when performing data
     *  processing. This lock will be synchronized on whenever the Table is
     *  modified.
     */
    public void loadData(Table t, String query, String keyField, Object lock) {
        loadData(t, query, keyField, lock, null);
    }
    
    /**
     * Asynchronously executes a query and stores the results in the given 
     * table instance. All data processing is done in a separate thread of
     * execution.
     * @param t the Table in which to store the results
     * @param query the query to execute
     * @param keyField the primary key field, comparisons on this field are
     *  performed to recognize data records already present in the table.
     *  A null value will result in no key checking.
     * @param lock an optional Object to use as a lock when performing data
     *  processing. This lock will be synchronized on whenever the Table is
     *  modified. A null value will result in no locking.
     * @param listener an optional listener that will provide notifications
     *  before the query has been issued and after the query has been 
     *  processed. This is most useful for post-processing operations.
     */
    public void loadData(Table t, String query, String keyField, 
                         Object lock, DataSourceWorker.Listener listener) {
        DataSourceWorker.Entry e = new DataSourceWorker.Entry(
                this, t, query, keyField, lock, listener);
        DataSourceWorker.submit(e);
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Execute a query and return the corresponding result set
     * @param query the text SQL query to execute
     * @return the ResultSet of the query
     * @throws SQLException if an error occurs issuing the query
     */
    private ResultSet executeQuery(String query) throws SQLException {
        if ( m_stmt == null )
            m_stmt = m_conn.createStatement();
        
        // clock in
        long timein = System.currentTimeMillis();
        
        s_logger.info("Issuing query: "+query);
        ResultSet rset = m_stmt.executeQuery(query);
        
        // clock out
        long time = System.currentTimeMillis()-timein;
        s_logger.info("External query processing completed: "
                + (time/1000) + "." + (time%1000) + " seconds.");
        
        return rset;
    }
} // end of class DatabaseDataSource
