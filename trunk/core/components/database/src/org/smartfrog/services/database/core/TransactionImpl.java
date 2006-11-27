/** (C) Copyright 2006 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */

/**
 * Some parts of this class are derived from the SqlExec task of Apache Ant 1.7
 */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.smartfrog.services.database.core;

import org.smartfrog.services.filesystem.FileSystem;
import org.smartfrog.sfcore.common.SmartFrogDeploymentException;
import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Implement a SQL transaction. This class was written while looking at Ant's
 * SQLExec, though there wasnt any direct cut and paste. Its a lot simpler, as
 * it offers less options for SQL processing, and corrects some ambiguities in
 * Ant's task created 20-Sep-2006 17:25:59
 */

public class TransactionImpl extends JdbcOperationImpl implements Transaction {
    public static final String ERROR_NO_COMMANDS = "No commands declared";
    public static final String ERROR_TOO_MANY_COMMANDS = "Too many command attributes";

    private List<String> commands;
    private boolean escapeProcessing = false;
    private String delimiter;
    private int expectedStatementCount = -1;
    private boolean failOnSqlError;
    protected static final String ENCODING = "UTF-8";
    protected static final Charset UTF8 = Charset.forName(ENCODING);
    private boolean printHeaders;
    private boolean printResults;


    public TransactionImpl() throws RemoteException {
    }

    public synchronized void sfStart()
            throws SmartFrogException, RemoteException {
        super.sfStart();

        delimiter = sfResolve(ATTR_DELIMITER, delimiter, true);
        escapeProcessing = sfResolve(ATTR_ESCAPE, escapeProcessing, true);
        expectedStatementCount = sfResolve(ATTR_EXPECTED_STATEMENT_COUNT,
                expectedStatementCount, false);
        failOnSqlError = sfResolve(ATTR_FAIL_ON_SQL_ERROR,
                failOnSqlError,
                true);
        printResults = sfResolve(ATTR_PRINTRESULTS, printResults, true);
        printHeaders = sfResolve(ATTR_PRINTHEADERS, printHeaders, true);
        int count = 0;
        String command = sfResolve(ATTR_COMMAND, (String) null, false);
        if (command != null) {
            count++;
        }
        File commandFile = FileSystem.lookupAbsoluteFile(this,
                ATTR_COMMAND_FILE,
                null,
                null,
                false,
                null);
        if (commandFile != null) {
            count++;
        }
        String resource = sfResolve(ATTR_COMMAND_RESOURCE,
                (String) null,
                false);
        if (resource != null) {
            count++;
        }
        Vector commandList = null;
        commandList = sfResolve(ATTR_SQL_COMMANDS, commandList, false);
        if (commandList != null) {
            count++;
        }
        if (count == 0) {
            throw new SmartFrogDeploymentException(ERROR_NO_COMMANDS);
        }
        if (count > 1) {
            throw new SmartFrogDeploymentException(ERROR_TOO_MANY_COMMANDS);
        }

        //everything is validated. read in the commands from file, resource or string
        if (commandFile != null) {
            try {
                StringBuffer buffer;
                buffer = FileSystem.readFile(commandFile, UTF8);
                command = buffer.toString();
            } catch (IOException e) {
                throw new SmartFrogDeploymentException("Failed to read " + commandFile,
                        e);
            }
        }
        if (resource != null) {
            command = getHelper().loadResourceToString(resource, UTF8);
        }

        //at this point, command contains the entire value of the command attribute, resource or file.
        //we now crack it into a List of commands

        if (command != null) {
            try {
                commands = crackCommands(command, delimiter);
            } catch (IOException e) {
                throw new SmartFrogDeploymentException("when parsing\n " + command,
                        e);
            }
        } else {
            if (commandList != null) {
                //the commands are copied to the command list as strings.
                commands = new ArrayList<String>(commandList.size());
                for (Object o : commandList) {
                    commands.add(o.toString());
                }
            } else {
                //no commands at all. That's not really allowed. 
                commands = new ArrayList<String>(0);
                command = "";
            }
        }

        int size = commands.size();
        if (expectedStatementCount >= 0 && expectedStatementCount != size) {
            throw new SmartFrogDeploymentException("Expected " + expectedStatementCount
                    + " statements, but after parsing, there were " + size
                    + " from \n" + command);
        }
        if (size > 0) {
            //work to do. check the connection (synchronously), then start the worker
            checkConnection();
            startWorkerThread();
        } else {
            //no work to do.
            getLog().debug("No SQL statements to execute; skipping");
        }
    }

    /**
     * this runs in the other tread. Run the commands one by one
     *
     * @param connection the open connection.
     *
     * @throws SQLException
     * @throws SmartFrogException
     */
    public void performOperation(Connection connection)
            throws SQLException, SmartFrogException {
        executeCommands(connection, commands.iterator());
    }

    /**
     * Crack the command string into a list of commands (which may be zero), at
     * every line-ending delimiter. delimiters not at tne d
     *
     * @param sql
     * @param commandDelimiter
     *
     * @return a (possibly empty) list of commands.
     *
     * @throws IOException
     */
    public List<String> crackCommands(String sql, String commandDelimiter)
            throws IOException {
        List<String> list = new ArrayList<String>();
        int delimiterSize = commandDelimiter.length();
        String line;
        StringBuffer buffer = new StringBuffer();
        BufferedReader in = new BufferedReader(new StringReader(sql));

        while ((line = in.readLine()) != null) {
            if (buffer.length() > 0) {
                //add a newline if the buffer is ongoing
                buffer.append("\n");
            }
            //add the line
            buffer.append(line);
            String trimmed = line.trim();
            if (trimmed.endsWith(commandDelimiter)) {
                //this is the end of the line, so we break it here
                String sqlcommand = buffer.substring(0,
                        buffer.length() - delimiterSize);
                //don't add empty commands, but dont strip off trailing whitespace for the sake
                //of comments.
                if (sqlcommand.trim().length() > 0) {
                    list.add(sqlcommand);
                }
                //reset the buffer
                buffer.replace(0, buffer.length(), "");
            }
        }
        //end of the line. If there isnt a trailing semicolon, it is still a command
        String tail = buffer.toString();
        if (tail.trim().length() > 0) {
            list.add(tail);
        }
        return list;
    }

    /**
     * Execute a sequence of commands. It takes an iterator so anything can act
     * as a source of commands; there isnt even any need for the type to be
     * string, as long as the toString() method of each iterated value returns a
     * single SQL command (without delimeter)
     *
     * @param connection
     * @param commandIterator
     *
     * @throws SmartFrogDeploymentException
     */
    public void executeCommands(Connection connection,
                                Iterator<String> commandIterator)
            throws SmartFrogDeploymentException {
        while (commandIterator.hasNext()) {
            String command = commandIterator.next();
            executeOneCommand(connection, command);
        }
    }

    /**
     * Directly derived from apache code, has apache copyright until rewritten
     * better
     *
     * @param connection connection to use
     * @param command    comand to issue
     *
     * @throws SmartFrogDeploymentException
     */
    public void executeOneCommand(Connection connection, String command)
            throws SmartFrogDeploymentException {
        ResultSet resultSet = null;

        try {
            Statement statement = connection.createStatement();
            statement.setEscapeProcessing(escapeProcessing);
            boolean hasResultSet;
            int updateCount;
            int updateCountTotal = 0;

            hasResultSet = statement.execute(command);
            updateCount = statement.getUpdateCount();
            resultSet = statement.getResultSet();
            do {
                if (!hasResultSet) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                } else {
                    processResults(resultSet);
                }
                hasResultSet = statement.getMoreResults();
                if (hasResultSet) {
                    updateCount = statement.getUpdateCount();
                    resultSet = statement.getResultSet();
                }
            } while (hasResultSet);
            if (updateCountTotal > 0) {
                getLog().info(updateCountTotal + " rows affected");
            }

            SQLWarning warning = connection.getWarnings();
            while (warning != null) {
                getLog().warn(warning.toString());
                warning = warning.getNextWarning();
            }
            connection.clearWarnings();
        } catch (SQLException e) {
            getLog().error("Failed to execute: " + command, e);
            if (failOnSqlError) {
                throw translate("When executing " + command, e);
            }
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    getLog().error("when clsing the result set", e);
                }
            }
        }
    }

    /**
     * Override point, act on the results
     *
     * @param results the results of the operation
     *
     * @throws SQLException
     */
    protected void processResults(ResultSet results)
            throws SQLException {
        if (printResults) {
            printResults(results);
        }
    }


    /**
     * Print a result set to the log at info level.
     *
     * @param results to process
     *
     * @throws SQLException if something failed
     */
    protected void printResults(ResultSet results) throws SQLException {
        ResultSetMetaData md = results.getMetaData();
        int columnCount = md.getColumnCount();
        StringBuffer line = new StringBuffer();
        if (printHeaders) {
            for (int col = 1; col < columnCount; col++) {
                line.append(md.getColumnName(col));
                line.append(",");
            }
            line.append(md.getColumnName(columnCount));
            getLog().info(line);
            line = new StringBuffer();
        }
        while (results.next()) {
            boolean first = true;
            for (int col = 1; col <= columnCount; col++) {
                String columnValue = results.getString(col);
                if (columnValue != null) {
                    columnValue = columnValue.trim();
                }

                if (first) {
                    first = false;
                } else {
                    line.append(",");
                }
                line.append(columnValue);
            }
            getLog().info(line);
            line = new StringBuffer();
        }
    }
}
