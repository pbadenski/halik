#!/bin/bash
set -e

SRC_SESSION_DIR="/Users/pbadenski/Downloads"
DEST_SESSION_DIR=/Users/pbadenski/workspace/halik-demo/public/session

SESSION_ID="$1"
pushd .
cp "$SRC_SESSION_DIR/$SESSION_ID.zip" /tmp
cd /tmp
rm -rf $SESSION_ID && mkdir $SESSION_ID
unzip -d $SESSION_ID $SESSION_ID.zip
cp -rf $SESSION_ID $DEST_SESSION_DIR
cd $DEST_SESSION_DIR
pushd $SESSION_ID/threads
for thread in $( ls ); do
  mv $thread $(echo $thread | sed -e 's/\d*.dbg//')
done
popd
printf "[" > $SESSION_ID.json
THREADS_LIST=$( ls "$SESSION_ID/threads" | sed 's/\(.*\)/{"number":"\1","link":"\/session\/'$SESSION_ID'\/threads\/\1"}/' | perl -pe 's/\n/,/ unless eof' )
printf $( echo "$THREADS_LIST" | paste -sd ",\n" - ) >> $SESSION_ID.json
printf "]" >> $SESSION_ID.json
popd
