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

package com.geminimobile.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import com.geminimobile.RawCdrAccess;

public class MdrCreater
{
    private static Calendar CAL = Calendar.getInstance(TimeZone.getTimeZone("GMT+0"));
    private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHH");

    private RawCdrAccess client;
    private URI outputPath;
    private OutputStream[] os;
    private String target;

    /**
     * Constructor, create cassendra client
     */
    public MdrCreater()
    {
        super();
        client = new RawCdrAccess();
    }

    /**
     * open client connection and create all output streams
     */
    public void init(Properties p) throws IOException
    {
        if (!p.isEmpty())
            client.setProperties(p);

        client.open();

        if (client.numMarkets() == 0)
            return;
        else
            os = new OutputStream[client.numMarkets()];

        if (outputPath == null) {
            // no output path specified, print screen
            for (int i = 0; i < client.numMarkets(); i++)
                os[i] = System.out;

        } else if (outputPath.getScheme() == null ||
                outputPath.getScheme().equalsIgnoreCase("file")) {
            // local file system
            int i = 0;
            File basePath = new File(outputPath);
            for (String market : client.getMarkets()) {
                File dir = new File(basePath, market);
                if (dir.exists() == false && dir.mkdirs() == false)
                    throw new IOException("Unable to create path:" + dir.getAbsolutePath());
                os[i++] = new FileOutputStream(new File(dir, "hmg-cdr_comb1-1_" + target + 
                         "_" + System.currentTimeMillis() + ".mdr"));
            }

        } else {
            throw new IOException("Unsupported output path: " + outputPath.toASCIIString());
        }
    }

    /**
     * close client connection
     */
    public void cleanup()
    {
        client.close();
        if (os != null) {
            for (OutputStream o : os) {
                try {
                    o.close();
                } catch (IOException ioe) {
                    // swallow it anyway
                }
            }
        }
    }
    
    /**
     * create mdrs from cdrs<br/>
     * cdr: 0 - op,
     *      1 - market,
     *      2 - tid,
     *      3 - mdr_type,
     *      4 - msg_ts,
     *      5 - imsi,
     *      6 - mo_ip,
     *      7 - mt_ip,
     *      8 - ptn,
     *      9 - msg_type,
     *      10 - mo_domain,
     *      11 - mt_domain
     *      
     * @param os output stream where the mdrs should write to
     * @param market is the id (integer) representing the market
     * @param hour is the mdr to create.  hour is also the key used in the
     * cassendra table
     * @throws IOException 
     */
    public void mdr(OutputStream os, int market, String hour)
    throws IOException {
        client.reset();

        List<String> cdrs;
        while (!(cdrs = client.getRawCdr(market, hour)).isEmpty()) {
            for (String cdr : cdrs) {
                String[] cdrFields = cdr.split(",");
                if (cdrFields.length < 9) {
                    // bad cdr
                    System.err.println("bad cdr " + cdrFields.length);
                    continue;

                } else {
                    for (String recipient : cdrFields[4].split(":")) {
                        if (recipient == null | recipient.isEmpty())
                            continue;

                        os.write(String.format(
                                "%6.6s%17.17s%15.15s%19.19s%19.19s%15.15s%2.2s%256.256s%256.256s",
                                        cdrFields[3], cdrFields[4], cdrFields[5], cdrFields[6],
                                        recipient, cdrFields[8], cdrFields[9], cdrFields[10],
                                        cdrFields[11])
                                .getBytes());
                        os.write('\n');
                    }
                    os.flush();
                }
            }
        }
    }

    /**
     * Helper function to loop over all markets and create the corresponding
     * output stream.
     * @throws IOException
     */
    public void mdr()
    throws IOException {
        for (int market = 0; market < client.numMarkets(); market++) {
            mdr(os[market], market, target);
        }
    }

    public void setMarkets(String markets) {
        client.setMarket(markets.split(","));
    }

    public void setOutputPath(String path) throws URISyntaxException {
        outputPath = new URI(path);
    }

    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Print usage to System.out
     */
    public static void usage()
    {
        System.out.println("usage: MdrCreater [help] [-t hour] [-o uri] [-m markets] [-p port] [-h servers]");
        System.out.println("\t-t\t\tYYYYMMDDHH Default last hour of the local time");
        System.out.println("\t-o\t\toutput stream for example file://tmp. Default: screen");
        System.out.println("\t-h\t\tcomma saparated list of cassandra hosts. Default: localhost");
        System.out.println("\t-p\t\tport to cassandra Default: 9160");
        System.out.println("\t-m\t\tcomma separated list of markets. Default: region1,region2,region3,region4");
        System.out.println("\t\t\turi file://<path>");
        System.out.println("\t\t\turi <username>@<host>:<path>");
    }

    public static void main(String[] args)
    {
        MdrCreater creator = new MdrCreater();
        Properties prop = new Properties();

        CAL.setTimeInMillis(CAL.getTimeInMillis() - 3600000);
        creator.setTarget(SDF.format(CAL.getTime()));

        for (int i = 0; i < args.length;) {
            if (args[i].equalsIgnoreCase("help")) {
                usage();
                System.exit(0);

            } else if (args[i].equals("-t")) {
                creator.setTarget(args[i+1]);
                i += 2;

            } else if (args[i].equals("-o")) {
                try {
                    creator.setOutputPath(args[i+1]);
                } catch (URISyntaxException urie) {
                    System.err.println("Unknown path: " + args[i+1]);
                    System.exit(0);
                }
                i += 2;

            } else if (args[i].equals("-h")) {
                prop.setProperty("hosts", args[i+1]);
                i += 2;

            } else if (args[i].equals("-p")) {
                prop.setProperty("port", args[i+1]);
                i += 2;
                
            } else if (args[i].equals("-m")) {
                creator.setMarkets(args[i+1]);        
                i += 2;

            } else {
                System.err.println("Unknown input:" + args[i]);
                usage();
                System.exit(0);
            }
        }

        try {
            creator.init(prop);
            creator.mdr();
            creator.cleanup();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        System.exit(0);
    }
}
