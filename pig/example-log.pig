msisdn = LOAD 'cassandra://CDRLogs/MSISDNTimeline' USING CassandraStorage();
cdrs = FOREACH msisdn GENERATE flatten($1);
cdrtime = FOREACH cdrs GENERATE $0 as hour;
givenhourcdr = FILTER cdrtime BY hour > '20110101000000';
msisdnByHour = GROUP givenhourcdr BY $0;
msisdnByHourCount = FOREACH msisdnByHour GENERATE COUNT($1), group;
orderedMsisdn = ORDER msisdnByHourCount BY $0;
topUserAfterNewYear = LIMIT orderedMsisdn 100;
dump topUserAfterNewYear;
