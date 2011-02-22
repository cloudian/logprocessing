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
import java.util.List;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;
import static me.prettyprint.hector.api.factory.HFactory.createColumnQuery;
import static me.prettyprint.hector.api.factory.HFactory.createKeyspace;
import static me.prettyprint.hector.api.factory.HFactory.createMutator;
import static me.prettyprint.hector.api.factory.HFactory.getOrCreateCluster;
import static me.prettyprint.hector.api.factory.HFactory.createSliceQuery;
import static me.prettyprint.hector.api.factory.HFactory.createRangeSlicesQuery;

import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.api.query.RangeSlicesQuery;



/**
 * An example DAO (data access object) which uses the Command pattern.
 * <p/>
 * This DAO is simple, it provides a get/insert/delete API for String values.
 * The underlying cassandra implementation stores the values under Keyspace1.key.Standard1.v
 * where key is the value's key, Standard1 is the name of the column family and "v" is just a column
 * name that's used to hold the value.
 * <p/>
 * what's interesting to notice here is that ease of operation that the command pattern provides.
 * The pattern assumes only one keyspace is required to perform the operation (get/insert/remove)
 * and injects it to the {@link Command#execute(Keyspace)} abstract method which is implemented
 * by all the dao methods.
 * The {@link Command#execute(String, int, String)} which is then invoked, takes care of creating
 * the {@link Keyspace} instance and releasing it after the operation completes.
 *
 * @author Ran Tavory (rantav@gmail.com)
 */
/**
 * @author root
 *
 */
public class DataAccessObject {

	private String m_cf;

	private final StringSerializer m_serializer = StringSerializer.get();
	private final LongSerializer m_longserializer = LongSerializer.get();
	
	private final Keyspace m_keyspace;
	
	public static void main(String[] args) {		
		DataAccessObject obj = new DataAccessObject("Keyspace1", "localhost", 9160, "Standard1");
		//obj.insert("key1", "col1", "testvalue1");
	    obj.insert("key1", "col1", "value1", StringSerializer.get());
	    
		//System.out.println(obj.get("key1", "col1"));
		System.out.println(obj.get("key1", "col1", StringSerializer.get()));
	}


	public DataAccessObject(String strKeyspace, String host, int port, String columnFamily) {
		this.m_cf = columnFamily;
		
		Cluster c = getOrCreateCluster("MyCluster", host + ":" + port);
		m_keyspace = createKeyspace(strKeyspace, c);
	}
	
	/**
	 * Insert a new value keyed by key (column name is a string)
	 *
	 * @param key Key for the value
	 * @param value the String value to insert
	 */
	public <K> void insert(final K key, final String column_name, final String value, Serializer<K> keySerializer) {
		createMutator(m_keyspace, keySerializer).insert(key, m_cf,
				createColumn(column_name, value, m_serializer, m_serializer));		
	}

	/**
	 * Insert a new value keyed by key. Column name is a long (e.g. timestamp)
	 *
	 * @param key Key for the value
	 * @param value the String value to insert
	 */
	@SuppressWarnings("unchecked")
	public <K> void insert(final K key, final long column_name, final String value, Serializer<K> keySerializer) {
		Mutator m = createMutator(m_keyspace, keySerializer);
		m.insert(key, m_cf, createColumn(column_name, value, m_longserializer, m_serializer));		
	}
	

	/**
	 * Get a string value.
	 *
	 * @return The string value; null if no value exists for the given key.
	 */
	  public <K> String get(final K key, final String column_name, Serializer<K> keySerializer) {
		ColumnQuery<K, String, String> q = createColumnQuery(m_keyspace, keySerializer, m_serializer, m_serializer);
		QueryResult<HColumn<String, String>> r = q.setKey(key).setName(column_name).setColumnFamily(m_cf).execute();
		HColumn<String, String> c = r.get();
		return c == null ? null : c.getValue();
	  }

	
	
	/**
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public List<HColumn<Long,String>> getSlice(final String key) {
		SliceQuery<String, Long, String> q = createSliceQuery(m_keyspace, m_serializer, m_longserializer, m_serializer);
		
		q.setColumnFamily(m_cf).setKey(key).setRange(0L, Long.MAX_VALUE, false, 100); // get up to 100 columns
		QueryResult<ColumnSlice<Long, String>> r = q.execute();
	    return r.get().getColumns();	    
	}


	public List<HColumn<String,String>> getSlice(final String key, final String startCol, final String endCol, final int limit) {
		SliceQuery<String, String, String> q = createSliceQuery(m_keyspace, m_serializer, m_serializer, m_serializer);
		
		q.setColumnFamily(m_cf).setKey(key).setRange(startCol, endCol, false, limit); 
		QueryResult<ColumnSlice<String, String>> r = q.execute();
	    return r.get().getColumns();	    
	}

	/**
	 * @param key
	 * @param ts - Time stamp - used for specifying range.  This is the max time stamp to use
	 * @param limit - max number of records to get
	 * @return
	 * @throws Exception
	 */
	public List<HColumn<String,String>> getSliceUsingTimestamp(final String key, final String minTS, final String maxTS, final int limit, final boolean reverse) throws Exception {

		SliceQuery<String, String, String> q = createSliceQuery(m_keyspace, m_serializer, m_serializer, m_serializer);
		
		if (reverse)
			q.setColumnFamily(m_cf).setKey(key).setRange(maxTS, minTS, true, limit); // // Reverse the order of results (newest to oldest)
		else
			q.setColumnFamily(m_cf).setKey(key).setRange(minTS, maxTS, false, limit); // // Reverse the order of results (newest to oldest)			
		QueryResult<ColumnSlice<String, String>> r = q.execute();
	    return r.get().getColumns();
	}
	
	public List<HColumn<Long,String>> getSliceUsingTimestamp(final String key, final long minTS, final long maxTS, final int limit, final boolean reverse) {

		SliceQuery<String, Long, String> q = createSliceQuery(m_keyspace, m_serializer, m_longserializer, m_serializer);
		
		if (reverse)
			q.setColumnFamily(m_cf).setKey(key).setRange(maxTS, minTS, true, limit); // // Reverse the order of results (newest to oldest)
		else
			q.setColumnFamily(m_cf).setKey(key).setRange(minTS, maxTS, false, limit); // // Reverse the order of results (newest to oldest)			
		QueryResult<ColumnSlice<Long, String>> r = q.execute();
	    return r.get().getColumns();
	}
	
	/*
	public List<Row<String, String, String>> getRangeSlice(final String begRange, final String endRange, final String begColumn, final int limit) throws Exception {
		RangeSlicesQuery<String, String, String> q = createRangeSlicesQuery(m_keyspace, m_serializer, m_serializer, m_serializer);
		q.setColumnFamily(m_cf).setKeys(begRange, endRange).setRange(begColumn, "", false, limit);
		QueryResult<OrderedRows<String, String, String>> r = q.execute();
		
		List<Row<String, String, String>> rows = r.get().getList();
		
		return rows;
	}*/
	public List<Row<String, Long, String>> getRangeSlice(final String begRange, final String endRange, final long begColumn, final int limit, 
														 final boolean reverse) {
		RangeSlicesQuery<String, Long, String> q = createRangeSlicesQuery(m_keyspace, m_serializer, m_longserializer, m_serializer);
		
		if (reverse)
			q.setColumnFamily(m_cf).setKeys(begRange, endRange).setRange(begColumn, 0L, true, limit);
		else
			q.setColumnFamily(m_cf).setKeys(begRange, endRange).setRange(begColumn, Long.MAX_VALUE, false, limit);
			
		QueryResult<OrderedRows<String, Long, String>> r = q.execute();
		
		List<Row<String, Long, String>> rows = r.get().getList();
		
		return rows;
	}
	
	public List<Row<String, String, String>> getRangeSlice(final String begRange, final String endRange, final String begColumn, final int limit,
														 final boolean reverse) {
		RangeSlicesQuery<String, String, String> q = createRangeSlicesQuery(m_keyspace, m_serializer, m_serializer, m_serializer);

		if (reverse)
			q.setColumnFamily(m_cf).setKeys(begRange, endRange).setRange(begColumn, "", true, limit);
		else
			q.setColumnFamily(m_cf).setKeys(begRange, endRange).setRange(begColumn, "", false, limit);

		QueryResult<OrderedRows<String, String, String>> r = q.execute();

		List<Row<String, String, String>> rows = r.get().getList();

		return rows;
	}
	
	/**
	* Delete multiple values
	*/
	  public <K> void delete(String column_name, Serializer<K> keySerializer, K... keys) {
	    Mutator<K> m = createMutator(m_keyspace, keySerializer);
	    for (K key: keys) {
	      m.addDeletion(key, m_cf, column_name, m_serializer);	      
	    }
	    m.execute();
	  }
	  
	  
	  
	/**
	 * Delete a column for a particular row from cassandra. If column_name is null, it
	 * should delete the entire row.
	 */
	@SuppressWarnings("unchecked")
	public <K> void delete(final String key, final String column_name, Serializer<K> keySerializer)  {
	    Mutator m = createMutator(m_keyspace, keySerializer);

	    m.addDeletion(key, m_cf, column_name, m_serializer);	     
	    m.execute();
	}

	@SuppressWarnings("unchecked")
	public <K> void delete(final String key, final long column_name, Serializer<K> keySerializer) {
	    Mutator m = createMutator(m_keyspace, keySerializer);

	    m.addDeletion(key, m_cf, column_name, m_longserializer);	     
	    m.execute();
	}
		
		
	/**
	 * 	Deletes all keys in a ColumnFamily.  Note that the keys may still show up in range scan. (http://wiki.apache.org/cassandra/FAQ#range_ghosts)
	 * @throws Exception
	 */	
	public void deleteAll() {
		
	}
	
	
	/**
	 * 	Deletes all keys in a ColumnFamily.  Note that the keys may still show up in range scan. (http://wiki.apache.org/cassandra/FAQ#range_ghosts)
	 * @throws Exception
	 */	
	/*
	protected void deleteAll() throws Exception {
		execute(new Command<Void>() {
			public Void execute(final Keyspace ks) {
				ColumnParent columnParent = new ColumnParent(CF_NAME);
				ColumnPath columnPath = new ColumnPath(CF_NAME);
				
				SlicePredicate slicePredicate = new SlicePredicate();
				SliceRange columnRange = new SliceRange();
				columnRange.setStart(new byte[0]);
				columnRange.setFinish(new byte[0]);
				columnRange.setReversed(false);
				slicePredicate.setSlice_range(columnRange);

				KeyRange keyRange = new KeyRange(500); // max of 500 keys
				keyRange.setStart_key("");
				keyRange.setEnd_key("");

				Map<String, List<Column>> map = ks.getRangeSlices(columnParent, slicePredicate, keyRange);

				for (String key : map.keySet()) {
					ks.remove(key, columnPath);
				}
				return null;
			}
		});
	}
	*/
	

}

