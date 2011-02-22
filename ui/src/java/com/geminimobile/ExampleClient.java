/*
  Copyright 2011 Gemini Mobile Technologies (http://www.geminimobile.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.geminimobile;

/*import static me.prettyprint.cassandra.utils.StringUtils.bytes;
import static me.prettyprint.cassandra.utils.StringUtils.string;
import me.prettyprint.cassandra.service.*;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnPath;
*/

/**
* Example client that uses the cassandra hector client.
*
* @author Ran Tavory (rantav@gmail.com)
*
*/
public class ExampleClient {
/* DEPRECATED in Hector V2 API !!! 
  public static void main(String[] args) throws Exception {
    CassandraClientPool pool = CassandraClientPoolFactory.INSTANCE.get();
    CassandraClient client = pool.borrowClient("localhost", 9160);
    // A load balanced version would look like this:
    // CassandraClient client = pool.borrowClient(new String[] {"cas1:9160", "cas2:9160", "cas3:9160"});

    try {
      Keyspace keyspace = client.getKeyspace("Twitter");
      ColumnPath columnPath = new ColumnPath("User");
      columnPath.setColumn(bytes("username"));

      // insert
      keyspace.insert("key", columnPath, bytes("foobarsdafasdf"));

      // read
      Column col = keyspace.getColumn("key", columnPath);

      System.out.println("Read from cassandra: " + string(col.getValue()));

      // This line makes sure that even if the client had failures and recovered, a correct
      // releaseClient is called, on the up to date client.
      client = keyspace.getClient();
    } finally {
      // return client to pool. do it in a finally block to make sure it's executed
      pool.releaseClient(client);
    }
  }
  */
	
}

