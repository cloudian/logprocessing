package org.apache.cassandra.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Map;
import java.util.List;
import java.nio.ByteBuffer;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.Mutation;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * Maintains a connection to a Cassandra server and facilitates Thrift
 * calls to it.
 * 
 * Supports automatic retries.
 */
public class CassandraClient {

  private ServerSet serverSet;
  private String currentServer;

  private String keyspace;

  private Cassandra.Client client;
  private TTransport transport;

  /** How long we wait before retrying a dead server. */
  private static final long DEFAULT_RETRY_TIME = 500; // 500ms

  public CassandraClient(String keyspace, String[] servers) {
    this.keyspace = keyspace;
    this.serverSet = new ServerSet(servers, DEFAULT_RETRY_TIME);
    this.currentServer = null;
  }

  public void open() throws IOException {
    try {
      this.currentServer = this.serverSet.get();
    } catch (ServerSet.NoServersAvailableException e) {
      throw new IOException("No Cassandra servers available.");
    }

    int splitIndex = this.currentServer.indexOf(':');
    if(splitIndex == -1) {
      throw new IOException("Bad host:port pair: " + this.currentServer);
    }
    String host = this.currentServer.substring(0, splitIndex);
    int port = Integer.parseInt(this.currentServer.substring(splitIndex + 1));

    TSocket sock = new TSocket(host, port);
    this.transport = new TFramedTransport(sock);
    TProtocol protocol = new TBinaryProtocol(transport);
    this.client = new Cassandra.Client(protocol);

    try {
      this.transport.open();
      this.client.set_keyspace(this.keyspace);
    } catch(TException texc) {
      throw new IOException(texc.getMessage());
    } catch(InvalidRequestException exc) {
      throw new IOException(exc.getMessage());
    }
  }

  public void close() throws IOException {
    this.transport.close();
  }

  /** Inserts columns into a column family in a given row. */
  public void insert(byte[] key, String columnFamily, Column[] columns, ConsistencyLevel consistencyLevel) throws IOException
  {
    List<Mutation> mutationList = new ArrayList<Mutation>();
    for(int i = 0; i < columns.length; i++) {
      Mutation mutation = new Mutation();
      ColumnOrSuperColumn cosc = new ColumnOrSuperColumn();
      cosc.column = columns[i];
      mutation.setColumn_or_supercolumn(cosc);
      mutationList.add(mutation);
    }
    Map<String, List<Mutation>> innerMutationMap = new HashMap<String, List<Mutation>>();
    innerMutationMap.put(columnFamily, mutationList);
    //Map<byte[], Map<String, List<Mutation>>> mutationMap = new HashMap<byte[], Map<String, List<Mutation>>>();
    //balaji
    Map<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
    mutationMap.put(ByteBuffer.wrap(key), innerMutationMap);

    batchMutate(mutationMap, consistencyLevel);
  }

  /** Attempts to perform a batch mutation and retries upon failure. */
  private void batchMutate(
      Map<ByteBuffer, Map<String, List<Mutation>>> mutationMap,
      ConsistencyLevel consistencyLevel)
  throws IOException
  {
    try {
      this.client.batch_mutate(mutationMap, consistencyLevel);
    } catch (UnavailableException exc) {
      this.serverSet.markDead(this.currentServer);
      this.open();
      batchMutate(mutationMap, consistencyLevel);
    } catch (TimedOutException exc) {
      this.serverSet.markDead(this.currentServer);
      this.open();
      batchMutate(mutationMap, consistencyLevel);
    } catch (InvalidRequestException exc) {
      throw new IOException(exc.toString()); 
    } catch (TException exc) {
      throw new IOException(exc.toString());
    }
  }

  /** Holds a set of servers that may be marked dead and reinstated. */
  private class ServerSet {

    private ArrayList<String> servers;
    private int serverIndex;
    private Queue<Pair> dead = new LinkedList<Pair>();
    private long retryTime;

    ServerSet(String[] servers, long retryTime) {
      this.retryTime = retryTime;

      // Uniformly randomly permute the list()
      Random rand = new Random();
      for(int i=0; i < servers.length; i++) {
        int j = rand.nextInt(servers.length);
        String temp = servers[i];
        servers[i] = servers[j];
        servers[j] = temp;
      }

      this.servers = new ArrayList<String>();
      for(int i=0; i < servers.length; i++) {
        this.servers.add(servers[i]);
      }
      this.serverIndex = 0;
    }

    /** Gets the next available server. */
    String get() throws NoServersAvailableException {
      if(!this.dead.isEmpty()) {
        Pair pair = this.dead.remove();
        if (pair.l > System.currentTimeMillis())
          this.dead.add(pair);
        else
          this.servers.add(pair.str);
      }
      if(this.servers.isEmpty()) {
        throw new NoServersAvailableException();
      }
      return this.servers.get(this.serverIndex++);
    }

    /**
     * Marks the server as dead.  It will be reinstated after retryTime
     * has passed.
     */
    void markDead(String server) {
      this.servers.remove(server);
      this.dead.add(new Pair(server, System.currentTimeMillis() + this.retryTime));
    }

    private class Pair {
      String str;
      Long l;

      Pair(String s, Long l) {
        this.str = s;
        this.l = l;
      }
    }

    public class NoServersAvailableException extends Exception {      
      private static final long serialVersionUID = 1L;
      NoServersAvailableException() {
        super();
      }
    }
  }
}
