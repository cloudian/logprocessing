#!/usr/bin/env /bin/bash

# Copyright: (c) 2008-2010 Gemini Mobile Technologies, Inc.  All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This script starts flume as a master. It sets up the proper jar
# library paths and defaults to a java class name.  This script reuses
# flume scripts.  For advance settings, use their scripts...


# This stuff here makes CMDPATH absolutely path to bin correctly
CMDPATH=`dirname $0`
pushd $CMDPATH >/dev/null
CMDPATH=`pwd`
popd >/dev/null

# generic path... Here
BINPATH=`dirname $0`
if [ -f "${BINPATH}/gemini-env.sh" ]; then
    source "$BINPATH/gemini-env.sh"
fi

# flume run 
if [ -z "$FLUME_RUNTIME" ]; then
  export FLUME_RUNTIME="$CMDPATH"
fi

export FLUME_LOG_DIR=${FLUME_RUNTIME}/master/log
export FLUME_PID_DIR=${FLUME_RUNTIME}/master/run
export FLUME_CONF_DIR=${FLUME_HOME}/conf


if [[ ! -e  ${FLUME_LOG_DIR} ]]; then
   mkdir -p ${FLUME_LOG_DIR}
fi

if [[ ! -e ${FLUME_PID_DIR} ]]; then
   mkdir -p ${FLUME_PID_DIR}
fi

# ./bin/flume collector
${FLUME_HOME}/bin/flume node_nowatch -n agent1
