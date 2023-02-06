# README-2 - modification of Blazegraph database #


## * What was modified - A* algorithm ##
modifications were made first of all in the code 

## * Essence of modifications, what requests do we like ##

## * Build to make new database binary ## 
the whole quasy-scipt to builddocekris at ./scripts/build-Docker-drivers.sh - stages 1-3

## * Buid to make new container ##
./scripts/my-compile.sh or my-recompile.sh
first compiles everything, second (presumabely) only chnages
 
## * Tests - A* tests ##
First - server shall be started: 
./mods-astar/targets/my-start-ASTAR
It starts server at http://172.19.0.1:9999/blazegraph/

This server can be tested by

## tests- container tests ##
