#!/bin/bash

# Start services necessary for running Aura.

cd $(dirname $0)/../ > /dev/null

BASEDIR="$(pwd)"
CMD="$(readlink -f $0)"

LOGDIR="$BASEDIR/logs"
mkdir -p "$LOGDIR"

# Find a local directory to store the database in
for dir in /big/aura-test /scratch2/$(whoami) /var/tmp; do
    if [ -d $dir ]; then
	DBDIR=$dir
	break
    fi
done

AURAHOME="$BASEDIR/aura"
JARDIR="$BASEDIR/aura/dist"
LIBDIR="$BASEDIR/Libraries"
JSKPOLICY="$LIBDIR/jini/jsk-all.policy"

CONFIGPATH="$BASEDIR/aardvark/src/com/sun/labs/aura/aardvark/resource"
CONFIGPATH="/com/sun/labs/aura/datastore/util"
JAR="$JARDIR/aura.jar"
BASEJAVACMD="java -DauraHome=$AURAHOME -DauraPolicy=$JSKPOLICY -DcsDirs=$JARDIR"
JAVACMD=$BASEJAVACMD

for file in $(find "$JARDIR" -name *jar); do AURALIBS=$AURALIBS:$file; done

export CLASSPATH=AURALIBS

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
	    TORUN="$JAVACMD -Xmx1g -jar $JAR $CONFIGPATH/dataStoreHeadConfig.xml dataStoreHeadStarter"
	    LOG=$LOGDIR/$TIMESTAMP.data_store_head.log
            echo "Starting data store head" > $LOG
            ;;
        pc*)
            PREFIX=${cmd#pc}
            PREFIX=${PREFIX:-0}
            PORT=$(( 44444 + $PREFIX ))
	    TORUN="$JAVACMD -Xmx1g -Dprefix=$PREFIX -DcsPort=$PORT -jar $JAR"
            TORUN="$TORUN $CONFIGPATH/partitionClusterConfig.xml partitionClusterStarter"
	    LOG=$LOGDIR/$TIMESTAMP.partition_cluster_$PREFIX.log
            echo "Starting partition cluster: $PREFIX on port $PORT" > $LOG
            ;;
        rep*)
            PREFIX=${cmd#rep}
            PREFIX=${PREFIX:-0}
            PORT=$(( 33333 + $PREFIX ))
	    LOG=$LOGDIR/$TIMESTAMP.replicant_$PREFIX.log
            TORUN="$JAVACMD -d64 -Xmx4g -Dprefix=$PREFIX -DcsPort=$PORT -DdataDir=$DBDIR -jar $JAR"
	    TORUN="$TORUN $CONFIGPATH/replicantConfig.xml replicantStarter"
            echo "Starting replicant $PREFIX on port $PORT in $DBDIR" > $LOG
            ;;
	-timestamp)
	    shift
	    TIMESTAMP=$1
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
Usage: $(basename $0) [ all | reg | dsh | pc[0|1]+ | rep[0|1]+ ]
   reg: Start the JINI registration service
   dsh: Start the Aura data store head
   pc[0|1]+: Start a partition cluster for the given bit string (pc1, pc010, pc10, etc.)
   rep[0|1]+: Start a replicant for the given bit string (rep0, rep101, rep10, etc.)
   all: Start all services in the tabs of a gnome terminal
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