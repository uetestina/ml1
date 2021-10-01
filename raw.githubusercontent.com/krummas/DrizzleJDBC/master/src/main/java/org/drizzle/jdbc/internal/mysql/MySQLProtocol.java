/*
 * Drizzle-JDBC
 *
 * Copyright (c) 2009-2011, Marcus Eriksson, Stephane Giron, Marc Isambart, Trond Norbye
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided with the distribution.
 *  Neither the name of the driver nor the names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.drizzle.jdbc.internal.mysql;

import org.drizzle.jdbc.internal.SQLExceptionMapper;
import org.drizzle.jdbc.internal.common.*;
import org.drizzle.jdbc.internal.common.packet.EOFPacket;
import org.drizzle.jdbc.internal.common.packet.ErrorPacket;
import org.drizzle.jdbc.internal.common.packet.OKPacket;
import org.drizzle.jdbc.internal.common.packet.RawPacket;
import org.drizzle.jdbc.internal.common.packet.ResultPacket;
import org.drizzle.jdbc.internal.common.packet.ResultPacketFactory;
import org.drizzle.jdbc.internal.common.packet.ResultSetPacket;
import org.drizzle.jdbc.internal.common.packet.SyncPacketFetcher;
import org.drizzle.jdbc.internal.common.packet.buffer.ReadUtil;
import org.drizzle.jdbc.internal.common.packet.commands.ClosePacket;
import org.drizzle.jdbc.internal.common.packet.commands.SelectDBPacket;
import org.drizzle.jdbc.internal.common.packet.commands.StreamedQueryPacket;
import org.drizzle.jdbc.internal.common.query.DrizzleQuery;
import org.drizzle.jdbc.internal.common.query.Query;
import org.drizzle.jdbc.internal.common.queryresults.DrizzleQueryResult;
import org.drizzle.jdbc.internal.common.queryresults.DrizzleUpdateResult;
import org.drizzle.jdbc.internal.common.queryresults.NoSuchColumnException;
import org.drizzle.jdbc.internal.common.queryresults.QueryResult;
import org.drizzle.jdbc.internal.drizzle.packet.DrizzleRowPacket;
import org.drizzle.jdbc.internal.mysql.packet.MySQLFieldPacket;
import org.drizzle.jdbc.internal.mysql.packet.MySQLGreetingReadPacket;
import org.drizzle.jdbc.internal.mysql.packet.MySQLRowPacket;
import org.drizzle.jdbc.internal.mysql.packet.commands.AbbreviatedMySQLClientAuthPacket;
import org.drizzle.jdbc.internal.mysql.packet.commands.MySQLBinlogDumpPacket;
import org.drizzle.jdbc.internal.mysql.packet.commands.MySQLClientAuthPacket;
import org.drizzle.jdbc.internal.mysql.packet.commands.MySQLClientOldPasswordAuthPacket;
import org.drizzle.jdbc.internal.mysql.packet.commands.MySQLPingPacket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static org.drizzle.jdbc.internal.common.packet.buffer.WriteBuffer.intToByteArray;

/**
 * TODO: refactor, clean up TODO: when should i read up the resultset? TODO: thread safety? TODO: exception handling
 * User: marcuse Date: Jan 14, 2009 Time: 4:06:26 PM
 */
public class MySQLProtocol implements Protocol {
    private final static Logger log = Logger.getLogger(MySQLProtocol.class.getName());
    private boolean connected = false;
    private Socket socket;
    private BufferedOutputStream writer;
    private final String version;
    private boolean readOnly = false;
    private final String host;
    private final int port;
    private String database;
    private final String username;
    private final String password;
    private final List<Query> batchList;
    private PacketFetcher packetFetcher;
    private final Properties info;
    private final long serverThreadId;
    private volatile boolean queryWasCancelled = false;
    private volatile boolean queryTimedOut = false;
    private boolean hasMoreResults = false;
    /**
     * Get a protocol instance
     *
     * @param host     the host to connect to
     * @param port     the port to connect to
     * @param database the initial database
     * @param username the username
     * @param password the password
     * @param info
     * @throws org.drizzle.jdbc.internal.common.QueryException
     *          if there is a problem reading / sending the packets
     */
    public MySQLProtocol(final String host,
                         final int port,
                         final String database,
                         final String username,
                         final String password,
                         Properties info)
            throws QueryException {
        this.info = info;
        this.host = host;
        this.port = port;
        this.database = (database == null ? "" : database);
        this.username = (username == null ? "" : username);
        this.password = (password == null ? "" : password);

        final SocketFactory socketFactory = SocketFactory.getDefault();
        try {
            // Extract connectTimeout URL parameter
            String connectTimeoutString = info.getProperty("connectTimeout");
            Integer connectTimeout = null;
            if (connectTimeoutString != null) {
                try {
                    connectTimeout = Integer.valueOf(connectTimeoutString);
                } catch (Exception e) {
                    connectTimeout = null;
                }
            }

            // Create socket with timeout if required
            InetSocketAddress sockAddr = new InetSocketAddress(host, port);
            socket = socketFactory.createSocket();
            if (connectTimeout != null) {
                socket.connect(sockAddr, connectTimeout * 1000);
            } else {
                socket.connect(sockAddr);
            }
        } catch (IOException e) {
            throw new QueryException("Could not connect: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }
        batchList = new ArrayList<Query>();
        try {
            BufferedInputStream reader = new BufferedInputStream(socket.getInputStream(), 32768);
            packetFetcher = new SyncPacketFetcher(reader);
            writer = new BufferedOutputStream(socket.getOutputStream(), 32768);
            final MySQLGreetingReadPacket greetingPacket = new MySQLGreetingReadPacket(packetFetcher.getRawPacket());
            this.serverThreadId = greetingPacket.getServerThreadID();

            log.finest("Got greeting packet");
            this.version = greetingPacket.getServerVersion();
            byte packetSeq = 1;
            final Set<MySQLServerCapabilities> capabilities = EnumSet.of(MySQLServerCapabilities.LONG_PASSWORD,
                    MySQLServerCapabilities.IGNORE_SPACE,
                    MySQLServerCapabilities.CLIENT_PROTOCOL_41,
                    MySQLServerCapabilities.TRANSACTIONS,
                    MySQLServerCapabilities.SECURE_CONNECTION,
                    MySQLServerCapabilities.LOCAL_FILES);
            if(info.getProperty("allowMultiQueries") != null) {
                capabilities.add(MySQLServerCapabilities.MULTI_STATEMENTS);
                capabilities.add(MySQLServerCapabilities.MULTI_RESULTS);
            }
            // If a database is given, but createDB is not defined or is false,
            // then just try to connect to the given database
            if (this.database != null && !this.database.equals("") && !createDB())
                capabilities.add(MySQLServerCapabilities.CONNECT_WITH_DB);
            if (info.getProperty("useAffectedRows", "false").equals("false")) {
                capabilities.add(MySQLServerCapabilities.FOUND_ROWS);
            }
            if(info.getProperty("useSSL") != null && greetingPacket.getServerCapabilities().contains(MySQLServerCapabilities.SSL)) {
                capabilities.add(MySQLServerCapabilities.SSL);
                AbbreviatedMySQLClientAuthPacket amcap = new AbbreviatedMySQLClientAuthPacket(capabilities);
                amcap.send(writer);

                SSLSocketFactory sslSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket)sslSocketFactory.createSocket(socket,
                        socket.getInetAddress().getHostAddress(),
                        socket.getPort(),
                        false);
                sslSocket.setEnabledProtocols(new String [] {"TLSv1"});
                sslSocket.setUseClientMode(true);
                sslSocket.startHandshake();
                socket = sslSocket;
                writer = new BufferedOutputStream(socket.getOutputStream(), 32768);
                writer.flush();
                reader = new BufferedInputStream(socket.getInputStream(), 32768);
                packetFetcher = new SyncPacketFetcher(reader);

                packetSeq++;
            } else if(info.getProperty("useSSL") != null){
                throw new QueryException("Trying to connect with ssl, but ssl not enabled in the server");
            }


            final MySQLClientAuthPacket cap = new MySQLClientAuthPacket(this.username,
                    this.password,
                    this.database,
                    capabilities,
                    greetingPacket.getSeed(),
                    packetSeq);
            cap.send(writer);
            log.finest("Sending auth packet");

            RawPacket rp = packetFetcher.getRawPacket();

            if ((rp.getByteBuffer().get(0) & 0xFF) == 0xFE) {   // Server asking for old format password
                final MySQLClientOldPasswordAuthPacket oldPassPacket = new MySQLClientOldPasswordAuthPacket(
                        this.password, Utils.copyWithLength(greetingPacket.getSeed(),
                        8), rp.getPacketSeq() + 1);
                oldPassPacket.send(writer);

                rp = packetFetcher.getRawPacket();
            }

            final ResultPacket resultPacket = ResultPacketFactory.createResultPacket(rp);
            if (resultPacket.getResultType() == ResultPacket.ResultType.ERROR) {
                final ErrorPacket ep = (ErrorPacket) resultPacket;
                final String message = ep.getMessage();
                throw new QueryException("Could not connect: " + message);
            }

            // At this point, the driver is connected to the database, if createDB is true, 
            // then just try to create the database and to use it
            if (createDB()) {
                // Try to create the database if it does not exist
                executeQuery(new DrizzleQuery("CREATE DATABASE IF NOT EXISTS " + this.database));
                // and switch to this database
                executeQuery(new DrizzleQuery("USE " + this.database));
            }

            connected = true;
        } catch (IOException e) {
            throw new QueryException("Could not connect: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }
    }

    /**
     * Closes socket and stream readers/writers
     *
     * @throws org.drizzle.jdbc.internal.common.QueryException
     *          if the socket or readers/writes cannot be closed
     */
    public void close() throws QueryException {
        try {
            if(! (socket instanceof SSLSocket))
                socket.shutdownInput();
        } catch (IOException ignored) {
        }
        try {
            final ClosePacket closePacket = new ClosePacket();
            closePacket.send(writer);
            if (! (socket instanceof SSLSocket))
                socket.shutdownOutput();
            writer.close();
            packetFetcher.close();
        } catch (IOException e) {
            throw new QueryException("Could not close connection: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        } finally {
            try {
                this.connected = false;
                socket.close();
            } catch (IOException e) {
                log.warning("Could not close socket");
            }
        }
        this.connected = false;
    }

    /**
     * @return true if the connection is closed
     */
    public boolean isClosed() {
        return !this.connected;
    }

    /**
     * create a DrizzleQueryResult - precondition is that a result set packet has been read
     *
     * @param packet the result set packet from the server
     * @return a DrizzleQueryResult
     * @throws java.io.IOException when something goes wrong while reading/writing from the server
     */
    private QueryResult createDrizzleQueryResult(final ResultSetPacket packet) throws IOException, QueryException {
        final List<ColumnInformation> columnInformation = new ArrayList<ColumnInformation>();
        for (int i = 0; i < packet.getFieldCount(); i++) {
            final RawPacket rawPacket = packetFetcher.getRawPacket();
            final ColumnInformation columnInfo = MySQLFieldPacket.columnInformationFactory(rawPacket);
            columnInformation.add(columnInfo);
        }
        packetFetcher.getRawPacket();
        final List<List<ValueObject>> valueObjects = new ArrayList<List<ValueObject>>();

        while (true) {
            final RawPacket rawPacket = packetFetcher.getRawPacket();

            if (ReadUtil.isErrorPacket(rawPacket)) {
                ErrorPacket errorPacket = (ErrorPacket) ResultPacketFactory.createResultPacket(rawPacket);
                checkIfCancelled();
                throw new QueryException(errorPacket.getMessage(), errorPacket.getErrorNumber(), errorPacket.getSqlState());
            }

            if (ReadUtil.eofIsNext(rawPacket)) {
                final EOFPacket eofPacket = (EOFPacket) ResultPacketFactory.createResultPacket(rawPacket);
                this.hasMoreResults = eofPacket.getStatusFlags().contains(EOFPacket.ServerStatus.SERVER_MORE_RESULTS_EXISTS);
                checkIfCancelled();
                
                return new DrizzleQueryResult(columnInformation, valueObjects, eofPacket.getWarningCount());
            }

            if (getDatabaseType() == SupportedDatabases.MYSQL) {
                final MySQLRowPacket rowPacket = new MySQLRowPacket(rawPacket, columnInformation);
                valueObjects.add(rowPacket.getRow(packetFetcher));
            } else {
                final DrizzleRowPacket rowPacket = new DrizzleRowPacket(rawPacket, columnInformation);
                valueObjects.add(rowPacket.getRow());
            }
        }
    }

    private void checkIfCancelled() throws QueryException {
        if (queryWasCancelled) {
            queryWasCancelled = false;
            throw new QueryException("Query was cancelled by another thread", (short) -1, "JZ0001");
        }
        if (queryTimedOut) {
            queryTimedOut = false;
            throw new QueryException("Query timed out", (short) -1, "JZ0002");
        }
    }

    public void selectDB(final String database) throws QueryException {
        log.finest("Selecting db " + database);
        final SelectDBPacket packet = new SelectDBPacket(database);
        try {
            packet.send(writer);
            final RawPacket rawPacket = packetFetcher.getRawPacket();
            ResultPacketFactory.createResultPacket(rawPacket);
        } catch (IOException e) {
            throw new QueryException("Could not select database: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }
        this.database = database;
    }

    public String getServerVersion() {
        return version;
    }

    public void setReadonly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean getReadonly() {
        return readOnly;
    }

    public void commit() throws QueryException {
        log.finest("commiting transaction");
        executeQuery(new DrizzleQuery("COMMIT"));
    }

    public void rollback() throws QueryException {
        log.finest("rolling transaction back");
        executeQuery(new DrizzleQuery("ROLLBACK"));
    }

    public void rollback(final String savepoint) throws QueryException {
        log.finest("rolling back to savepoint " + savepoint);
        executeQuery(new DrizzleQuery("ROLLBACK TO SAVEPOINT " + savepoint));
    }

    public void setSavepoint(final String savepoint) throws QueryException {
        executeQuery(new DrizzleQuery("SAVEPOINT " + savepoint));
    }

    public void releaseSavepoint(final String savepoint) throws QueryException {
        executeQuery(new DrizzleQuery("RELEASE SAVEPOINT " + savepoint));
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean ping() throws QueryException {
        final MySQLPingPacket pingPacket = new MySQLPingPacket();
        try {
            pingPacket.send(writer);
            log.finest("Sent ping packet");
            final RawPacket rawPacket = packetFetcher.getRawPacket();
            return ResultPacketFactory.createResultPacket(rawPacket).getResultType() == ResultPacket.ResultType.OK;
        } catch (IOException e) {
            throw new QueryException("Could not ping: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }
    }

    public QueryResult executeQuery(final Query dQuery) throws QueryException {
        log.finest("Executing streamed query: " + dQuery);
        this.hasMoreResults = false;
        final StreamedQueryPacket packet = new StreamedQueryPacket(dQuery);

        try {
            // make sure we are in a good state
            packetFetcher.clearInputStream();
            packet.send(writer);
        } catch (IOException e) {
            throw new QueryException("Could not send query: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }

        final RawPacket rawPacket;
        final ResultPacket resultPacket;
        try {
            rawPacket = packetFetcher.getRawPacket();
            resultPacket = ResultPacketFactory.createResultPacket(rawPacket);
        } catch (IOException e) {
            throw new QueryException("Could not read resultset: " + e.getMessage(),
                    -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                    e);
        }

        switch (resultPacket.getResultType()) {
            case ERROR:
                final ErrorPacket ep = (ErrorPacket) resultPacket;
                checkIfCancelled();
                log.warning("Could not execute query " + dQuery + ": " + ((ErrorPacket) resultPacket).getMessage());
                throw new QueryException(ep.getMessage(),
                        ep.getErrorNumber(),
                        ep.getSqlState());
            case OK:
                final OKPacket okpacket = (OKPacket) resultPacket;
                this.hasMoreResults = okpacket.getServerStatus().contains(ServerStatus.MORE_RESULTS_EXISTS);
                final QueryResult updateResult = new DrizzleUpdateResult(okpacket.getAffectedRows(),
                        okpacket.getWarnings(),
                        okpacket.getMessage(),
                        okpacket.getInsertId());
                log.fine("OK, " + okpacket.getAffectedRows());
                return updateResult;
            case RESULTSET:
                log.fine("SELECT executed, fetching result set");

                try {
                    return this.createDrizzleQueryResult((ResultSetPacket) resultPacket);
                } catch (IOException e) {
                    throw new QueryException("Could not read result set: " + e.getMessage(),
                            -1,
                            SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION.getSqlState(),
                            e);
                }
            default:
                log.severe("Could not parse result..." + resultPacket.getResultType());
                throw new QueryException("Could not parse result", (short) -1, SQLExceptionMapper.SQLStates.INTERRUPTED_EXCEPTION.getSqlState());
        }

    }

    public void addToBatch(final Query dQuery) {
        batchList.add(dQuery);
    }

    public List<QueryResult> executeBatch() throws QueryException {
        final List<QueryResult> retList = new ArrayList<QueryResult>(batchList.size());

        for (final Query query : batchList) {
            retList.add(executeQuery(query));
        }
        clearBatch();
        return retList;

    }

    public void clearBatch() {
        batchList.clear();
    }

    public List<RawPacket> startBinlogDump(final int startPos, final String filename) throws BinlogDumpException {
        final MySQLBinlogDumpPacket mbdp = new MySQLBinlogDumpPacket(startPos, filename);
        try {
            mbdp.send(writer);
            final List<RawPacket> rpList = new LinkedList<RawPacket>();
            while (true) {
                final RawPacket rp = this.packetFetcher.getRawPacket();
                if (ReadUtil.eofIsNext(rp)) {
                    return rpList;
                }
                rpList.add(rp);
            }
        } catch (IOException e) {
            throw new BinlogDumpException("Could not read binlog", e);
        }
    }

    public SupportedDatabases getDatabaseType() {
        return SupportedDatabases.fromVersionString(version);
    }

    public boolean supportsPBMS() {
        return info != null && info.getProperty("enableBlobStreaming", "").equalsIgnoreCase("true");
    }

    public String getServerVariable(String variable) throws QueryException {
        DrizzleQueryResult qr = (DrizzleQueryResult) executeQuery(new DrizzleQuery("select @@" + variable));
        if (!qr.next()) {
            throw new QueryException("Could not get variable: " + variable);
        }

        try {
            String value = qr.getValueObject(0).getString();
            return value;
        } catch (NoSuchColumnException e) {
            throw new QueryException("Could not get variable: " + variable);
        }
    }

    public QueryResult executeQuery(Query dQuery,
                                    InputStream inputStream) throws QueryException {
        int packIndex = 0;
        if(hasMoreResults) {
            try {
                packetFetcher.clearInputStream();
            } catch (IOException e) {
                throw new QueryException("Could clear input stream: "
                                 + e.getMessage(), -1,
                                 SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                                         .getSqlState(), e);

            }
        }
        this.hasMoreResults = false;
        log.finest("Executing streamed query: " + dQuery);
        final StreamedQueryPacket packet = new StreamedQueryPacket(dQuery);

        try {
            packIndex = packet.send(writer);
            packIndex++;
        } catch (IOException e) {
            throw new QueryException("Could not send query: " + e.getMessage(),
                    -1, SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                    .getSqlState(), e);
        }

        RawPacket rawPacket;
        ResultPacket resultPacket;

        try {
            rawPacket = packetFetcher.getRawPacket();
            resultPacket = ResultPacketFactory.createResultPacket(rawPacket);
        } catch (IOException e) {
            throw new QueryException("Could not read resultset: "
                    + e.getMessage(), -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                            .getSqlState(), e);
        }

        if (rawPacket.getPacketSeq() != packIndex)
            throw new QueryException("Got out of order packet ", -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                            .getSqlState(), null);

        switch (resultPacket.getResultType()) {
            case ERROR:
                final ErrorPacket ep = (ErrorPacket) resultPacket;
                log.warning("Could not execute query " + dQuery + ": "
                        + ((ErrorPacket) resultPacket).getMessage());
                throw new QueryException(ep.getMessage(), ep.getErrorNumber(),
                        ep.getSqlState());
            case OK:
                break;
            case RESULTSET:
                break;
            default:
                log.severe("Could not parse result...");
                throw new QueryException("Could not parse result");
        }

        packIndex++;
        return sendFile(dQuery, inputStream, packIndex);
    }

    /**
     * cancels the current query - clones the current protocol and executes a query using the new connection
     * <p/>
     * thread safe
     *
     * @throws QueryException
     */
    public void cancelCurrentQuery() throws QueryException {
        Protocol copiedProtocol = new MySQLProtocol(host, port, database, username, password, info);
        queryWasCancelled = true;
        copiedProtocol.executeQuery(new DrizzleQuery("KILL QUERY " + serverThreadId));
        copiedProtocol.close();
    }

    public void timeOut() throws QueryException {
        Protocol copiedProtocol = new MySQLProtocol(host, port, database, username, password, info);
        queryTimedOut = true;
        copiedProtocol.executeQuery(new DrizzleQuery("KILL QUERY " + serverThreadId));
        copiedProtocol.close();

    }

    public boolean createDB() {
        return info != null
                && info.getProperty("createDB", "").equalsIgnoreCase("true");
    }

    public boolean noPrepStmtCache() {
        return info != null
                && info.getProperty("noPrepStmtCache", "").equalsIgnoreCase("true");
    }

    /**
     * Send the given file to the server starting with packet number packIndex
     *
     * @param dQuery          the query that was first issued
     * @param inputStream input stream used to read the file
     * @param packIndex       Starting index, which will be used for sending packets
     * @return the result of the query execution
     * @throws QueryException if something wrong happens
     */
    private QueryResult sendFile(Query dQuery, InputStream inputStream,
                                 int packIndex) throws QueryException {
        byte[] emptyHeader = Utils.copyWithLength(intToByteArray(0), 4);
        RawPacket rawPacket;
        ResultPacket resultPacket;

        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                inputStream);

        ByteArrayOutputStream bOS = new ByteArrayOutputStream();

        try {
            while (true) {
                int data = bufferedInputStream.read();
                if (data == -1) {
                    // Send the last packet
                    byte[] data1 = bOS.toByteArray();
                    byte[] byteHeader = Utils.copyWithLength(
                            intToByteArray(data1.length), 4);
                    byteHeader[3] = (byte) packIndex;
                    // Send the packet
                    writer.write(byteHeader);
                    writer.write(data1);
                    writer.flush();
                    packIndex++;
                    break;
                }

                // Add data into buffer
                bOS.write(data);

                if (bOS.size() >= 0xffffff) {
                    byte[] byteHeader = Utils.copyWithLength(intToByteArray(bOS.size()), 4);
                    byteHeader[3] = (byte) packIndex;
                    // Send the packet
                    writer.write(byteHeader);

                    bOS.writeTo(writer);
                    writer.flush();
                    packIndex++;
                    bOS.reset();

                }
            }
        } catch (IOException e) {
            throw new QueryException("Could not send query: " + e.getMessage(),
                    -1, SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                    .getSqlState(), e);
        }
        try {
            emptyHeader[3] = (byte) packIndex;
            writer.write(emptyHeader);
            writer.flush();
        } catch (IOException e) {
            throw new QueryException("Could not send query: " + e.getMessage(),
                    -1, SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                    .getSqlState(), e);
        }

        try {
            rawPacket = packetFetcher.getRawPacket();
            resultPacket = ResultPacketFactory.createResultPacket(rawPacket);
        } catch (IOException e) {
            throw new QueryException("Could not read resultset: "
                    + e.getMessage(), -1,
                    SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                            .getSqlState(), e);
        }

        switch (resultPacket.getResultType()) {
            case ERROR:
                final ErrorPacket ep = (ErrorPacket) resultPacket;
                checkIfCancelled();
                throw new QueryException(ep.getMessage(), ep.getErrorNumber(),
                        ep.getSqlState());
            case OK:
                final OKPacket okpacket = (OKPacket) resultPacket;
                this.hasMoreResults = okpacket.getServerStatus().contains(ServerStatus.MORE_RESULTS_EXISTS);

                final QueryResult updateResult = new DrizzleUpdateResult(
                        okpacket.getAffectedRows(), okpacket.getWarnings(),
                        okpacket.getMessage(), okpacket.getInsertId());
                log.fine("OK, " + okpacket.getAffectedRows());
                return updateResult;
            case RESULTSET:
                log.fine("SELECT executed, fetching result set");
                try {
                    return this.createDrizzleQueryResult((ResultSetPacket) resultPacket);
                } catch (IOException e) {
                    throw new QueryException("Could not read result set: "
                            + e.getMessage(), -1,
                            SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                                    .getSqlState(), e);
                }
            default:
                log.severe("Could not parse result...");
                throw new QueryException("Could not parse result");
        }
    }

    public QueryResult getMoreResults() throws QueryException {
        try {
            if(!hasMoreResults)
                return null;
            ResultPacket resultPacket = ResultPacketFactory.createResultPacket(packetFetcher.getRawPacket());
            switch(resultPacket.getResultType()) {
                case RESULTSET:
                    return createDrizzleQueryResult((ResultSetPacket) resultPacket);
                case OK:
                    OKPacket okpacket = (OKPacket) resultPacket;
                    this.hasMoreResults = okpacket.getServerStatus().contains(ServerStatus.MORE_RESULTS_EXISTS);                    
                    return new DrizzleUpdateResult(
                            okpacket.getAffectedRows(), okpacket.getWarnings(),
                            okpacket.getMessage(), okpacket.getInsertId());
                case ERROR:
                    ErrorPacket ep = (ErrorPacket) resultPacket;
                    checkIfCancelled();
                    throw new QueryException(ep.getMessage(), ep.getErrorNumber(),
                                        ep.getSqlState());

            }
        } catch (IOException e) {
            throw new QueryException("Could not read result set: "
                             + e.getMessage(), -1,
                             SQLExceptionMapper.SQLStates.CONNECTION_EXCEPTION
                                     .getSqlState(), e);
        }
        return null;
    }

    public static String hexdump(byte[] buffer, int offset) {
        StringBuffer dump = new StringBuffer();
        if ((buffer.length - offset) > 0) {
            dump.append(String.format("%02x", buffer[offset]));
            for (int i = offset + 1; i < buffer.length; i++) {
                dump.append("_");
                dump.append(String.format("%02x", buffer[i]));
            }
        }
        return dump.toString();
    }

    public static String hexdump(ByteBuffer bb, int offset) {
        byte[] b = new byte[bb.capacity()];
        bb.mark();
        bb.get(b);
        bb.reset();
        return hexdump(b, offset);
    }

    /**
     * Catalogs are not supported in drizzle so this is a no-op with a Drizzle
     * connection<br>
     * MySQL treats catalogs as databases. The only difference with
     * {@link MySQLProtocol#selectDB(String)} is that the catalog is switched
     * inside the connection using SQL 'USE' command
     */
    public void setCatalog(String catalog) throws QueryException
    {
        if (getDatabaseType() == SupportedDatabases.MYSQL)
        {
            executeQuery(new DrizzleQuery("USE `" + catalog + "`"));
            this.database = catalog;
        }
        // else (Drizzle protocol): silently ignored since drizzle does not
        // support catalogs
    }

    /**
     * Catalogs are not supported in drizzle so this will always return null
     * with a Drizzle connection<br>
     * MySQL treats catalogs as databases. This function thus returns the
     * currently selected database
     */
    public String getCatalog() throws QueryException
    {
        if (getDatabaseType() == SupportedDatabases.MYSQL)
        {
            return getDatabase();
        }
        // else (Drizzle protocol): retrun null since drizzle does not
        // support catalogs
        return null;
    }
}
