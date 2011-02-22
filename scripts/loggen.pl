#   Copyright 2011, Gemini Mobile Technologies (http://www.geminimobile.com)
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

#!/usr/bin/perl

use strict;
use FileHandle;
use warnings;

my $fh = new FileHandle();
$fh->open("/tmp/cdr.log", "w+") || die $!;

my $i = 1;

for(;;) {

    my (@date)=localtime;
    $date[5]+=1900;$date[4]++;
    my $imsi = int(rand(3340100)) + 3340100;
    my $tid = int(rand(234632)) + 923463;
    my $msisdn = int(rand(99999)) + 16500000000;
    
    my $tstamp = sprintf("%0.4d%0.2d%0.2d%0.2d%0.2d%0.2d%0.3d", $date[5], $date[4], $date[3], $date[2],$date[1],$date[0],$date[6]);

    # log a line every 1 sec.

    if ($i % 2) {
	$fh->print("R1Rt,uscarrier,$tid,000002,$tstamp,$imsi,10.10.2.9,10.10.1.248,$msisdn,AXC,admin\@uscarrier.com,$msisdn\@uscarrier.com\n");
	$i++;
    } else {
	$fh->print("O1S,uscarrier,<$tstamp\@$imsi>,000001,$tstamp,$imsi,10.10.2.9,000.000.000.000,$msisdn,AXC,$msisdn\@uscarrier.com,+919844937636\@intlcarrier.co.in\n");
	$i = 1;
    }
    $fh->flush();
    
    sleep 1;
}
$fh->close()
