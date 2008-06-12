#!/bin/bash

# Start services necessary for running Aura.

CMD="$(readlink -f $0)"
cd $(dirname $0)/../
BASEDIR="$(pwd)"

LOGDIR="$BASEDIR/logs"
mkdir -p "$LOGDIR"

BASEPORT=33333
DATADIRS="/big/aura-test /scratch2/$(whoami) $TMPDIR /var/tmp /tmp"

[ -z "$AURAHOME" ] && AURAHOME="$BASEDIR/aura"
JARDIR="$BASEDIR/aura/dist"
LIBDIR="$BASEDIR/Libraries"

[ -z "$JSKPOLICY" ] && JSKPOLICY="$LIBDIR/jini/jsk-all.policy"

CONFIGPATH="$BASEDIR/aardvark/src/com/sun/labs/aura/aardvark/resource"
CONFIGPATH="/com/sun/labs/aura/datastore/util"

[ -z "$AURAJAR" ] && AURAJAR="$JARDIR/aura.jar"

AURALIBS=$AURAJAR
for file in $(find "$LIBDIR" -name *jar); do AURALIBS=$AURALIBS:$file; done

BASEJAVACMD="java -DauraHome=$AURAHOME -DauraPolicy=$JSKPOLICY -DcsDirs=$JARDIR  -cp $AURALIBS"
JAVACMD=$BASEJAVACMD

TIMESTAMP=$(date "+%Y:%m:%d:%H:%M:%S")

cmd=${1:-help}

trap killproc 1 2 3 6 15
killproc() {
    echo "Trapping Exit Signal"
}

until [ -z "$cmd" ]; do
    unset TORUN
    case "$cmd" in
        reg)
	    "$LIBDIR/jini/startReggie.sh"
            ;;
        dsh)
	    TORUN="$JAVACMD -Xmx1g com.sun.labs.aura.AuraServiceStarter $CONFIGPATH/dataStoreHeadConfig.xml dataStoreHeadStarter"
	    LOG=$LOGDIR/$TIMESTAMP.data_store_head.log
            echo "Starting data store head" > $LOG
            ;;
        pc*)
            PREFIX=${cmd#pc}
            PREFIX=${PREFIX:-0}
            PORT=$(( $BASEPORT + $PREFIX ))
	    TORUN="$JAVACMD -Xmx1g -Dprefix=$PREFIX -DcsPort=$PORT com.sun.labs.aura.AuraServiceStarter"
            TORUN="$TORUN $CONFIGPATH/partitionClusterConfig.xml partitionClusterStarter"
	    LOG=$LOGDIR/$TIMESTAMP.partition_cluster_$PREFIX.log
            echo "Starting partition cluster: $PREFIX on port $PORT" > $LOG
            ;;
        rep*)
            # Find a local directory to store the database in
	    for dir in $DATADIRS; do
		if [ -d $dir ]; then
		    DBDIR=$dir
		    break
		fi
	    done

            PREFIX=${cmd#rep}
            PREFIX=${PREFIX:-0}
            PORT=$(( $BASEPORT + 10 + $PREFIX ))
	    LOG=$LOGDIR/$TIMESTAMP.replicant_$PREFIX.log
            TORUN="$JAVACMD -d64 -Xmx4g -Dprefix=$PREFIX -DcsPort=$PORT -DdataDir=$DBDIR com.sun.labs.aura.AuraServiceStarter"
	    TORUN="$TORUN $CONFIGPATH/replicantConfig.xml replicantStarter"
            echo "Starting replicant $PREFIX on port $PORT in $DBDIR" > $LOG
            ;;
	-timestamp)
	    shift
	    TIMESTAMP=$1
	    ;;
	-baseport)
	    shift
	    BASEPORT=$1
	    ;;
	-datadir)
	    shift
	    DATADIRS="$1"
	    mkdir -p "$DATADIRS"
	    ;;
	-host)
	    # We want the environment variables evaluated on the remote server, so
	    # the script needs to be rerun in that environment
	    shift
	    HOST=$1
	    shift
	    ssh -X $HOST "$CMD" -timestamp "$TIMESTAMP" "$@"
	    exit
	    ;;
	-sleep)
	    shift
	    sleep $1
	    ;;
	killalltail)
	    # SIGTERM goes to SSH on the local machine rather than tail on the remote machine, so cleanup currently requires:
	    # ToDo: Clean this up and make it less prone to accidentally killing processes unintentionally.
	    #   startDataStore.sh -host search.east killalltail
	    for pid in $(ps -ef | grep $(whoami) | gawk '{if(match($8,/tail/)) print $2}'); do
		echo "Killing tail PID: $pid"
		kill $pid;
	    done
	    ;;
        all)
	    # ToDo: Make this stateless.
	    # Positions of arguments are currently important.
	    #  The following are not the same:
	    #      startDataStore.sh all -host search.east
	    #      startDataStore.sh -host search.east all
	    shift
            eval gnome-terminal --tab -e \"$CMD reg\" --title \"Interface Registration\" \
                           --tab -e \"$CMD -sleep 10 $@ dsh\" --title \"Data Store Head\" \
                           --tab -e \"$CMD -sleep 20 $@ pc0\" --title \"Partition Cluster \#0\" \
                           --tab -e \"$CMD -sleep 20 $@ pc1\" --title \"Partition Cluster \#1\" \
                           --tab -e \"$CMD -sleep 30 $@ rep0\" --title \"Replicant \#0\" \
                           --tab -e \"$CMD -sleep 30 $@ rep1\" --title \"Replicant \#1\"
	    exit
            ;;
        *)
            cat<<EOF
Usage: $(basename $0) <opt>* [ all | reg | dsh | pc[0|1]+ | rep[0|1]+ ]
   reg: Start the JINI registration service
   dsh: Start the Aura data store head
   pc[0|1]+: Start a partition cluster for the given bit string (pc1, pc010, pc10, etc.)
   rep[0|1]+: Start a replicant for the given bit string (rep0, rep101, rep10, etc.)
   all: Start all services in the tabs of a gnome terminal
   killalltail: Kill all tail processes
  
 Where opt is:
  -host <hostname> : Host to run the command on via ssh
  -timestamp <timestamp> : Timestamp to mark the logfile with
  -baseport <port num> : Port number to start JINI services relative to
  -datadir <directory> : Directory to store database in

Example:
  To start an interface registrar on the local machine and a complete datastore in
   separate processes on search.east:
    $(basename $0) all -host search.east
  To shut down the remote datastore processes: (term signals go to ssh rather than tail)
    $(basename $0) -host search.east killalltail

Notes:
  The output shown in the terminal is not the live output from the process, rather the
   output goes to a timestamped file in the logs directory and the information on the
   screen is from a tail of that file.

  Data directories in order of preference are:
    $DATADIRS
EOF
            ;;
    esac

    if [ ! -z "$TORUN" ]; then
	echo "Running: $TORUN to log '$LOG'" >> $LOG
	$TORUN >> $LOG 2>&1 &
	PID=$!
	echo "Started with PID: $PID" >> $LOG
	tail -f $LOG
	echo "Killing: $PID" >> $LOG
	[[ -z "$DONTKILL" && ! -z "$PID" ]] && kill $PID
    fi
    
    shift
    cmd=$1
done
