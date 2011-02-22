package org.apache.cassandra.plugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.lang.Number;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.util.Date;
import java.text.SimpleDateFormat; 

import com.cloudera.flume.conf.Context;
import com.cloudera.flume.conf.SinkFactory.SinkBuilder;
import com.cloudera.flume.core.Event;
import com.cloudera.flume.core.EventSink;
import com.cloudera.util.Pair;
import org.apache.cassandra.thrift.*;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Allows Cassandra to be used as a sink, primarily for log messages.
 * 
 * When the Cassandra sink receives an event, it does the following:
 *
 * 1. Creates a column where the name is a type 1 UUID (timestamp based) and the
 *    value is the event body.
 * 2. Inserts it into row "YYYYMMDD" (the current date) in the given ColumnFamily.
 *
 * CDRCassandraSink primarily targets log storage right now.
 */
public class CDRCassandraSink extends EventSink.Base {
  
    private final static int BYTES_PER_LONG = Long.SIZE / Byte.SIZE;

    private CassandraClient cClient;
    private static final UUIDGenerator uuidGen = UUIDGenerator.getInstance();

    private static final String entryColumnFamily = "CDREntry";
    private static final String msisdnColumnFamily = "MSISDNTimeline";
    private static final String hourlyColumnFamily = "HourlyTimeline";
    private static String rawColumnFamily;

    private static final long MILLI_TO_MICRO = 1000; // 1ms = 1000us
    private static final Logger LOGGER = LoggerFactory.getLogger(CDRCassandraSink.class.getName());
    private static final String[] CDRENTRY_NAME = {"type",
                           "market",
                           "id",
                           "timestamp",
                           "moipaddress",
                           "mtipaddress",
                           "msisdn",
                           "senderdomain",
                           "recipientdomain"};
    private static final int[] CDRENTRY_MAP = {0,1,2,4,6,7,8,10,11};
    public static final String TimeStampFormat = "yyyyMMddHHmmssSSS"; // Change this to proper format
    public static final String HourlyTimeStampFormat = "yyyyMMddHH"; 

    public static SimpleDateFormat m_sdf = new SimpleDateFormat(TimeStampFormat); 
    public static SimpleDateFormat m_hdf = new SimpleDateFormat(HourlyTimeStampFormat); 

    public CDRCassandraSink(String keyspace, String columnFamilyForRaw, String[] servers) {
        super();
        cClient = new CassandraClient(keyspace, servers);
        rawColumnFamily = columnFamilyForRaw;
    }

    @Override
    public void open() throws IOException {
        cClient.open();
    }

   /**
    * Writes the message to Cassandra.
    * The key is the current date (YYYYMMDD) and the column
    * name is a type 1 UUID, which includes a time stamp
    * component.
    *
    * CDR format is
    * op,market,tid,mdr_type,msg_ts,imsi,mo_ip,mt_ip,ptn,msg_type,mo_domain,mt_domain
    */
    @Override
    public void append(Event event) throws IOException {

    if (event.getBody().length > 0) {
        long timestamp = System.currentTimeMillis() * MILLI_TO_MICRO;

        // Make the index column
        UUID uuid = uuidGen.generateTimeBasedUUID();

        Column[] entryColumn = new Column[CDRENTRY_NAME.length];
        String rawEntry = new String(event.getBody());
        String[] rawEntries = rawEntry.split("\\,");
        long longTime = 0;
        String rawTimeLabel = null;
        String webTimeLabel = null;
        String longTimeLabel = null;
        for (int i = 0; i < CDRENTRY_NAME.length; i++) {
            entryColumn[i] = new Column();
            entryColumn[i].setName(CDRENTRY_NAME[i].getBytes());
            if (2 == i) {
                entryColumn[i].setValue(uuid.toString().getBytes());
            } else if (3 == i) {
                try {
                    Date date = m_sdf.parse(rawEntries[CDRENTRY_MAP[i]]);
                    longTime = date.getTime();
                    longTimeLabel = Long.toString(longTime);                    
                    rawTimeLabel = m_hdf.format(date);
                    longTime = (longTime / 3600000) * 3600000;
                    webTimeLabel = String.format("%013d_%s", longTime, uuid.toString());
                    LOGGER.debug(
                                " webTimeLabel:" + webTimeLabel +
                                " rawColumnFamily:" + rawColumnFamily +
                                " rawTimeLabel:" + rawTimeLabel);
                    entryColumn[i].setValue(longTimeLabel.getBytes());
                } catch (java.text.ParseException e) {
                    System.out.println(e);
                }
            } else {
                entryColumn[i].setValue(rawEntries[CDRENTRY_MAP[i]].getBytes());
            }

            entryColumn[i].setTimestamp(timestamp);
        }

        //MSISDNTimeLine & HourlyTimeLine
        String msisdn = new String(rawEntries[8]);
        Column msisdnTimeLine = new Column();
        msisdnTimeLine.setName(webTimeLabel.getBytes());
        msisdnTimeLine.setValue(uuid.toString().getBytes());
        msisdnTimeLine.setTimestamp(timestamp);

        Column hourlyTimeLine = new Column();
        hourlyTimeLine.setName(webTimeLabel.getBytes());
        hourlyTimeLine.setValue(uuid.toString().getBytes());
        hourlyTimeLine.setTimestamp(timestamp);

        //Rawentry timeline;
        StringBuilder rawFamily = new StringBuilder(rawColumnFamily);
        rawFamily.append("_");
        rawFamily.append(rawEntries[1]);
        Column rawColumn = new Column();
        rawColumn.setName(uuid.toString().getBytes());
        rawColumn.setValue(event.getBody());
        rawColumn.setTimestamp(timestamp);
        
        //Insert CDREntry
        cClient.insert(uuid.toString().getBytes(), entryColumnFamily, entryColumn, ConsistencyLevel.QUORUM);
        //Insert MSISDNTimeLine
        cClient.insert(msisdn.getBytes(), msisdnColumnFamily, new Column[] {msisdnTimeLine}, ConsistencyLevel.QUORUM);
        //Insert HourlyTimeLine
        cClient.insert(rawTimeLabel.getBytes(), hourlyColumnFamily, new Column[] {hourlyTimeLine}, ConsistencyLevel.QUORUM);
        //Insert raw
        cClient.insert(rawTimeLabel.getBytes(), rawColumnFamily, new Column[] {rawColumn}, ConsistencyLevel.QUORUM);
    }
    super.append(event);
  }

  @Override
  public void close() throws IOException {
    cClient.close();
  }

  public static SinkBuilder builder() {
    return new SinkBuilder() {
      @Override
      public EventSink build(Context context, String ... args) {
        if (args.length < 3) {
          throw new IllegalArgumentException(
              "usage: CDRCassandraSink(\"keyspace\"," +
              "\"column_family_for_raw_cdr\", " +
              "\"host:port\"...");
        }
        String[] servers = Arrays.copyOfRange(args, 2, args.length);
        return new CDRCassandraSink(args[0], args[1], servers);
      }
    };
  }

  /**
   * This is a special function used by the SourceFactory to pull in this class
   * as a plugin sink.
   */
  public static List<Pair<String, SinkBuilder>> getSinkBuilders() {
    List<Pair<String, SinkBuilder>> builders =
      new ArrayList<Pair<String, SinkBuilder>>();
    builders.add(new Pair<String, SinkBuilder>("CDRCassandraSink", builder()));
    return builders;
  }
}
