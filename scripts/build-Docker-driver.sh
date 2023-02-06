#!/bin/bash

#start at blazegraph project
#assumewe have two proejcts "shouldder to shoulder":
#./badatase-astr        - modified blazegraph
#./blazegraph-docker    - blazegraph-docker
#We will start at blazegraph and then   
echo "Building docker Image..."

echo "1. Assumed that version Nr is set"
#./updateVersion.sh 2.1.5-ASTAR

echo "2. assumed that up-to-date server is compiled by /scripts/my-compile.sh or my-recompile.sh"
#./my-recompile.sh

echo "3. build artifacts"
#./buildArifacts.sh

echo "4. cp recompiled to lazegraph-docker proejct"
#cp ./blazegraph-tgz/target/blazegraph-tgz-2.1.5-ASTAR-bundle.tar.gz ../blazegraph-docker/tgz/blazegraph-tgz-2.1.5-ASTAR-bundle.tar.gz        

echo "5. build, use custom imagetag"
cd ../blazegraph-docker
./my-build-docker
