/**
   Copyright (C) SYSTAP, LLC 2006-2012.  All rights reserved.

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
package com.bigdata.rdf.graph.impl.scheduler;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Iterator;
import java.util.Map;

import org.openrdf.model.Value;

import com.bigdata.rdf.graph.IGASSchedulerImpl;
import com.bigdata.rdf.graph.IStaticFrontier;
import com.bigdata.rdf.graph.impl.GASEngine;

/**
 * A simple scheduler based on a {@link ConcurrentHashMap}.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan
 *         Thompson</a>
 */
public class CHMScheduler implements IGASSchedulerImpl {

    private final ConcurrentHashMap<Value,Value> vertices;

    private final boolean sortFrontier;
    
    public CHMScheduler(final GASEngine gasEngine) {

        vertices = new ConcurrentHashMap<Value, Value>(gasEngine.getNThreads());

        sortFrontier = gasEngine.getSortFrontier();
        
    }

    @Override
    public void schedule(final Value v) {

        vertices.putIfAbsent(v,v);

    }

    @Override
    public void clear() {
        
        vertices.clear();
        
    }

    @Override
    public void compactFrontier(final IStaticFrontier frontier) {
        System.out.println("CHM Scheduler - vertex array");
/*
        Iterator iter= vertices.keySet().iterator();
        while(iter.hasNext()){
            System.out.println("  iter:  "+iter+" : "+ vertices.get(iter));
            iter.next();
        }


        for ( Value key: vertices.keySet()) {
            System.out.println("  iter:  "+key+" : "+ vertices.get(key).toString() );
        }
*/
        for ( Map.Entry<Value,Value>  entry: vertices.entrySet()) {
            System.out.println(
            "  iter: "+
            "   "+ entry+
            " : "+ entry.getKey().getClass()   + " : "+ entry.getKey().toString() + 
            " | "+ entry.getValue().getClass() + " : "+ entry.getValue().toString() +
            " / "+ entry.getClass()            + " : "+ entry.toString()
            );
        }
        System.out.println("-- =delete all except 1st key---");

        Iterator<Value>  iter =vertices.keySet().iterator();
        
        if(iter.hasNext()) 
            iter.next();

        while(iter.hasNext()) {
            Value kelem=iter.next();
            Value velem= vertices.get(kelem);
            // System.out.println( "  remove : " +  kelem +" / " +velem+ " ");  
            //  vertices.remove(kelem,velem);
    
        }


        Iterator<Value>  iter1 =vertices.keySet().iterator();        
        while(iter1.hasNext()) {
            Value kelem1=iter1.next();
            System.out.println(
            "  iter2 : " +  kelem1 +" / " +vertices.get(kelem1)+ " "
            );  
    
        }


        frontier.resetFrontier(vertices.size()/* minCapacity */, sortFrontier,
                vertices.keySet().iterator());

    }

} // CHMScheduler