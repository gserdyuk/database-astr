import json
from pymantic.rdf import *
from pymantic.parsers import turtle_parser
from pymantic.sparql import SPARQLServer
from urllib.request import urlopen

server = SPARQLServer('http://172.17.0.1:9999/blazegraph/sparql')

u='''
#graph3
clear all ;

PREFIX node: <http://sun.flower.ua/> 
PREFIX edge: <http://sun.flower.ua#> 

INSERT DATA {
node:a edge:p node:b . 
  
node:c edge:p node:b .
  
node:e edge:p node:c .
  
node:d edge:p node:e .
  
node:b edge:q node:d .

}
'''

ans1=server.update(u)

print(json.dumps(ans1, indent=4, default=str))

print('----------------------')

q='''
PREFIX node: <http://sun.flower.ua/> 
PREFIX edge: <http://sun.flower.ua#> 
PREFIX gas: <http://www.bigdata.com/rdf/gas#>
SELECT ?depth ?out ?p ?o 
  where {
    ?out ?p ?o . # figure out what link type(s) connect a vertex with a predecessor 
    
    SERVICE gas:service {
     gas:program gas:gasClass "com.bigdata.rdf.graph.analytics.ASTAR" . #ASTAR
     gas:program gas:in node:a . # one or more times, specifies the initial frontier.
     gas:program gas:target node:d . # only retain vertices along paths to these target vertices.
     gas:program gas:out ?out . # exactly once - will be bound to the visited vertices.
     gas:program gas:out1 ?depth . # exactly once - will be bound to the depth of the visited vertices.
     gas:program gas:out2 ?predecessor . # exactly once - will be bound to the predecessor.
     gas:program gas:maxIterations 6 . # optional limit on breadth first expansion.
     gas:program gas:maxVisited 100 . # optional limit on the #of visited vertices.
     gas:program gas:traversalDirection 'Undirected' . #'Reversed' #'Forward' #''
     gas:program gas:nthreads 1 .
     gas:program gas:epv <http://sun.flower.ua#q> .           
     } .
            
   #FILTER(?p!=edge:q) . # remove q links  - "epv" removes them only in ASTAR algorithm.
}
order by (?depth)

'''
ans=server.query(q)

print(json.dumps(ans, indent=4, default=str))

for iter in ans['results']['bindings']:
    print ( iter['out']['value'] + " <--(" + iter.get('p',{'value':' '})['value'] + ")--<< "+ iter.get('o',{'value':' '})['value'])

