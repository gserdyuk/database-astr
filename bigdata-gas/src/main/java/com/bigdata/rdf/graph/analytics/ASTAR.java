/**
   Copyright (C) SYSTAP, LLC 2006-2012.  All rights reserved.
   Copyright (C) EPAM, 2022.  All rights reserved.

   
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
package com.bigdata.rdf.graph.analytics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import com.bigdata.rdf.graph.BinderBase;
import com.bigdata.rdf.graph.EdgesEnum;
import com.bigdata.rdf.graph.Factory;
import com.bigdata.rdf.graph.FrontierEnum;
import com.bigdata.rdf.graph.IBinder;
import com.bigdata.rdf.graph.IBindingExtractor;
import com.bigdata.rdf.graph.IGASContext;
import com.bigdata.rdf.graph.IGASScheduler;
import com.bigdata.rdf.graph.IGASState;
import com.bigdata.rdf.graph.IPredecessor;
import com.bigdata.rdf.graph.impl.BaseGASProgram;

/**
 *ASTAR modification /GS EPAM
 * Breadth First Search (BFS) is an iterative graph traversal primitive. The
 * frontier is expanded iteratively until no new vertices are discovered. Each
 * visited vertex is marked with the round (origin ZERO) in which it was
 * visited. This is its distance from the initial frontier.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public class ASTAR extends BaseGASProgram<ASTAR.VS, ASTAR.ES, Void> implements
        IPredecessor<ASTAR.VS, ASTAR.ES, Void> {

//    private static final Logger log = Logger.getLogger(ASTAR.class); 

    public static class VS {

        /**
         * <code>-1</code> until visited. When visited, set to the current round
         * in order to assign each vertex its traversal depth.
         * <p>
         * Note: It is possible that the same vertex may be visited multiple
         * times in a given expansion (from one or more source vertices that all
         * target the same destination vertex). However, in this case the same
         * value will be assigned by each visitor. Thus, synchronization is only
         * required for visibility of the update within the round. As long as
         * one thread reports that it modified the depth, the vertex will be
         * scheduled.
         */
        private final AtomicInteger depth = new AtomicInteger(-1);
        
        /**
         * The predecessor is the first source vertex to visit a given target
         * vertex.
         */
        private final AtomicReference<Value> predecessor = new AtomicReference<Value>();
        
        /**
         * The depth at which this vertex was first visited (origin ZERO) and
         * <code>-1</code> if the vertex has not been visited.
         */
        public int depth() {
//            synchronized (this) {
                return depth.get();
//            }
        }

        /**
         * Return the first vertex to discover this vertex during ASTAR traversal.
         */
        public Value predecessor() {

            return predecessor.get();
            
        }

        /**
         * Note: This marks the vertex at the current traversal depth.
         * 
         * @return <code>true</code> if the vertex was visited for the first
         *         time in this round and the calling thread is the thread that
         *         first visited the vertex (this helps to avoid multiple
         *         scheduling of a vertex).
         */
        public boolean visit(final int depth, final Value predecessor) {
            if (this.depth.compareAndSet(-1/* expect */, depth/* newValue */)) {
                this.predecessor.set(predecessor);
                // Scheduled by this thread.
                return true;
            }
            return false;
//            synchronized (this) {
//                if (this.depth == -1) {
//                    this.depth = depth;
//                    return true;
//                }
//                return false;
//            }
        }

        @Override
        public String toString() {
            return "{depth=" + depth() + "}";
        }

    }// class VS

    /**
     * Edge state is not used.
     */
    public static class ES {

    }

    private static final Factory<Value, ASTAR.VS> vertexStateFactory = new Factory<Value, ASTAR.VS>() {

        @Override
        public ASTAR.VS initialValue(final Value value) {

            return new VS();

        }

    };

    @Override
    public Factory<Value, ASTAR.VS> getVertexStateFactory() {

        return vertexStateFactory;

    }

    @Override
    public Factory<Statement, ASTAR.ES> getEdgeStateFactory() {

        return null;

    }

    @Override
    public FrontierEnum getInitialFrontierEnum() {

        return FrontierEnum.SingleVertex;
        
    }
    
    @Override
    public EdgesEnum getGatherEdges() {

        return EdgesEnum.NoEdges;

    }

    @Override
    public EdgesEnum getScatterEdges() {

        return EdgesEnum.OutEdges;

    }

    /**
     * Not used.
     */
    @Override
    public void initVertex(final IGASContext<ASTAR.VS, ASTAR.ES, Void> ctx,
            final IGASState<ASTAR.VS, ASTAR.ES, Void> state, final Value u) {

        state.getState(u).visit(0, null/* predecessor */);

    }
    
    /**
     * Not used.
     */
    @Override
    public Void gather(IGASState<ASTAR.VS, ASTAR.ES, Void> state, Value u, Statement e) {
        
        throw new UnsupportedOperationException();
        
    }

    /**
     * Not used.
     */
    @Override
    public Void sum(final IGASState<ASTAR.VS, ASTAR.ES, Void> state,
            final Void left, final Void right) {

        throw new UnsupportedOperationException();
        
    }

    /**
     * NOP
     */
    @Override
    public ASTAR.VS apply(final IGASState<ASTAR.VS, ASTAR.ES, Void> state, final Value u, 
            final Void sum) {

        return null;
        
    }

    /**
     * Returns <code>true</code>.
     */
    @Override
    public boolean isChanged(IGASState<VS, ES, Void> state, Value u) {

        return true;
        
    }
    /**
     * 
     * st = "  http://sunflower.test/asn_cidr/86.102.213.0%2F24(TermId(5119U)), http://sunflower.test/admin_c(TermId(476U)), http://sunflower.test/handle/IS111-RIPE(TermId(5577U)), file:/home/gserdyuk/epddp/database-astr/mods-astar/data/triples_dump.ttl(TermId(8404U))  "
     * st = 
     * [0] "http://sunflower.test/asn_cidr/86.102.213.0%2F24
     *     "TermId"
     *     "5119U))"
     * [3] "http://sunflower.test/admin_c"
     *     "TermId"
     *     "476U))"
     *     "http://sunflower.test/handle/IS111-RIPE:
     *     "TermId"
     *     "5577U))""
     *     "file:/home/gserdyuk/epddp/database-astr/mods-astar/data/triples_dump.ttl"
     *     "TermId"
     *     "8404U))"
     */
    String parseStatementString2Predicate(String st){
        // st = "< http://sunflower.test/asn_cidr/86.102.213.0%2F24(TermId(5119U)), http://sunflower.test/admin_c(TermId(476U)), http://sunflower.test/handle/IS111-RIPE(TermId(5577U)), file:/home/gserdyuk/epddp/database-astr/mods-astar/data/triples_dump.ttl(TermId(8404U)) >"
        String[] tk=st.replaceAll("[<>]"," ").split("[,(]");
        String predicate=tk[3].trim();        
        System.out.println("### ### ParsedStatement predicate="+ predicate);
        return predicate;   // returns
    }

    /*
    String getPredicateString(String stmnt){
        String[] tk=stmnt.replaceAll("<|>"," ").split(",");
        String   pr=tk[1].trim();
        System.out.println("### ### PredicateSTring ="+pr);
        return pr;   

    }

    String getPredicateName(String pr){
        String[] list=pr.split("(");
        System.out.println("### ### predicateName="+list[0]);
        return list[0];
    }
    */

    /**
     * The remote vertex is scheduled for activation unless it has already been
     * visited.
     * <p>
     * Note: We are scattering to out-edges. Therefore, this vertex is
     * {@link Statement#getSubject()}. The remote vertex is
     * {@link Statement#getObject()}.
     */

    /* naive implementation- but very flexible

    boolean matchOneOf(String s){

        boolean r=false;
        
        r=s.matches("(.*)?rdf:type(.*)?http://sunflower\\.test/Handle(.*)?");
        if (r == true){
            return true;
            }

        //r=s.matches("(.*)?http://sunflower\\.test/asn_cidr/(.*)?");
        if (r == true){
            return true;
            }
        
        //r=s.matches("(.*)?http://sunflower\\.test/asn_cidr/86\\.102\\.188(.*)?");
        if (r == true){
            return true;
            }
    
        return r;
    }
    */

    /*GS: more efficient */
    boolean matchOneOf(String s){
        List<String> rej = Arrays.asList("rdf:type","http://sunflower.test/is_admin_c_of");
        /*List<String> rej = Arrays.asList("");
        System.out.println( " ## ## ## matchOneOf ");
        System.out.println( " s= "+s);
        System.out.println( "    -- rejected array --");
        for (String rj: rej){
            System.out.println(rj);
        }
        */   
        if (rej.contains(s)) {
        //    System.out.println( "returning true");
            return true;
        }
        else {   
        //    System.out.println( "returning false");
            return false;
        }
    }
   
    boolean matchOneOf2(String s, String[] sa){
        List<String> rej = Arrays.asList(sa);
        /* List<String> rej = Arrays.asList("");
        System.out.println( " ## ## ## matchOneOf2 ");
        System.out.println( " s = "+s);
        System.out.println( " -- rejected array --");
        for (String rj: rej){
            System.out.println(rj);
        }
        */
        if (rej.contains(s)) {
        //    System.out.println( "returning true");
            return true;
        }
        else {   
        //    System.out.println( "returning false");
            return false;
        }
    }

    @Override
    public void scatter(final IGASState<ASTAR.VS, ASTAR.ES, Void> state,
            final IGASScheduler sch, final Value u, final Statement e) {

        System.out.println(" ==scatter== ");
        String statementStr=state.toString(e);
        String[] toReject=state.getEpv().toString().
            replaceAll("\\[","").replaceAll("\\]","").replaceAll("\\ ","").
            split(",");
        
        boolean matches2=matchOneOf2(parseStatementString2Predicate(statementStr),toReject);      
        
        //boolean matches=matchOneOf(parseStatementString2Predicate(statementStr));
            
        boolean matches_new= state.getEpv().contains(e.getPredicate());

        //System.out.println("    *** Value_u         = "+u.getClass()+" "+u.toString());
        //System.out.println("    *** Statement_e     = "+e.getClass()+" "+e.toString());
        //System.out.println("    *** e.getObject()   = "+e.getObject().getClass()+" "+e.getObject().toString());
        //System.out.println("    *** e.getSubject()  = "+e.getSubject().getClass()+" "+e.getSubject().toString());
        //System.out.println("    *** state.getClass    = " + state.getGraphAccessor().getKB().toString((ISPO)e));
        //System.out.println("    *** state.getClass    = " + state.getClass());
        // /System.out.println(" exclude frominput:" + state.getClass());
        System.out.println(" *** e            = " + statementStr);
        System.out.println(" *** matches2      = " + matches2);
        //System.out.println(" *** matches       = " + matches); 

        /*
        System.out.println(" *** exclude frominput:" + state.getEpv().toString());    
        System.out.println(" *** splitted         :" + toReject.toString());    
        System.out.println(" *** predicate        = " + e.getPredicate());
        System.out.println(" *** matches_new      = " + matches_new);
        */


        if (matches2) { // GS filter mock  
            System.out.println("    *** - eliminated "+statementStr);
            return;
        }
        
        // remote vertex state.
        final Value v = state.getOtherVertex(u, e);
        final VS otherState = state.getState(v);
//        final VS otherState = state.getState(e.getObject()/* v */);

        // visit.
        if (otherState.visit(state.round() + 1, u/* predecessor */)) {

            /*
             * This is the first visit for the remote vertex. Add it to the
             * schedule for the next iteration.
             */

            sch.schedule(v);

        }

    }

    @Override
    public boolean nextRound(final IGASContext<ASTAR.VS, ASTAR.ES, Void> ctx) {
        
        //
        System.out.println("GS : next round");
        return true;
        
    }

    /**
     * {@inheritDoc}
     * <p>
     * <dl>
     * <dt>{@value Bindings#DEPTH}</dt>
     * <dd>The depth at which the vertex was first encountered during traversal.
     * </dd>
     * <dt>{@value Bindings#PREDECESSOR}</dt>
     * <dd>The predecessor is the first vertex that discovers a given vertex
     * during traversal.</dd>
     * </dl>
     */
    @Override
    public List<IBinder<ASTAR.VS, ASTAR.ES, Void>> getBinderList() {

        final List<IBinder<ASTAR.VS, ASTAR.ES, Void>> tmp = super.getBinderList();

        tmp.add(new BinderBase<ASTAR.VS, ASTAR.ES, Void>() {
            
            @Override
            public int getIndex() {
                return Bindings.DEPTH;
            }
            
            @Override
            public Value bind(final ValueFactory vf,
                    final IGASState<ASTAR.VS, ASTAR.ES, Void> state, final Value u) {

                return vf.createLiteral(state.getState(u).depth.get());

            }
            
        });

        tmp.add(new BinderBase<ASTAR.VS, ASTAR.ES, Void>() {
            
            @Override
            public int getIndex() {
                return Bindings.PREDECESSOR;
            }
            
            @Override
            public Value bind(final ValueFactory vf,
                    final IGASState<ASTAR.VS, ASTAR.ES, Void> state, final Value u) {

                return state.getState(u).predecessor.get();

            }

        });

        return tmp;

    }

    /**
     * Additional {@link IBindingExtractor.IBinder}s exposed by {@link ASTAR}.
     * 
     * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
     */
    public interface Bindings extends BaseGASProgram.Bindings {
        
        /**
         * The depth at which the vertex was visited.
         */
        int DEPTH = 1;
        
        /**
         * The ASTAR predecessor is the first vertex to discover a given vertex.
         * 
         */
        int PREDECESSOR = 2;
        
    }

//    /**
//     * Reduce the active vertex state, returning a histogram reporting the #of
//     * vertices at each distance from the starting vertex. There will always be
//     * one vertex at depth zero - this is the starting vertex. For each
//     * successive depth, the #of vertices that were labeled at that depth is
//     * reported. This is essentially the same as reporting the size of the
//     * frontier in each round of the traversal, but the histograph is reported
//     * based on the vertex state.
//     * 
//     * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan
//     *         Thompson</a>
//     */
//    protected static class HistogramReducer implements
//            IReducer<VS, ES, Void, Map<Integer, AtomicLong>> {
//
//        private final ConcurrentHashMap<Integer, AtomicLong> values = new ConcurrentHashMap<Integer, AtomicLong>();
//
//        @Override
//        public void visit(final IGASState<VS, ES, Void> state, final Value u) {
//
//            final VS us = state.getState(u);
//
//            if (us != null) {
//
//                final Integer depth = Integer.valueOf(us.depth());
//
//                AtomicLong newval = values.get(depth);
//
//                if (newval == null) {
//
//                    final AtomicLong oldval = values.putIfAbsent(depth,
//                            newval = new AtomicLong());
//
//                    if (oldval != null) {
//
//                        // lost data race.
//                        newval = oldval;
//
//                    }
//
//                }
//
//                newval.incrementAndGet();
//
//            }
//
//        }
//
//        @Override
//        public Map<Integer, AtomicLong> get() {
//
//            return Collections.unmodifiableMap(values);
//            
//        }
//        
//    }

    /*
     * TODO Do this in parallel for each specified target vertex.
     */
    @Override
    public void prunePaths(final IGASContext<VS, ES, Void> ctx,
            final Value[] targetVertices) {

        if (ctx == null)
            throw new IllegalArgumentException();

        if (targetVertices == null)
            throw new IllegalArgumentException();
        
        final IGASState<ASTAR.VS, ASTAR.ES, Void> gasState = ctx.getGASState();

        final Set<Value> retainSet = new HashSet<Value>();

        for (Value v : targetVertices) {

            if (!gasState.isVisited(v)) {

                // This target was not reachable.
                continue;

            }

            /*
             * Walk the precessors back to a starting vertex.
             */
            Value current = v;

            while (current != null) {

                retainSet.add(current);

                final ASTAR.VS currentState = gasState.getState(current);

                final Value predecessor = currentState.predecessor();

                current = predecessor;

            }
            
        } // next target vertex.
        
        gasState.retainAll(retainSet);
        
    }

//    @Override
//    public <T> IReducer<VS, ES, Void, T> getDefaultAfterOp() {
//
//        class NV implements Comparable<NV> {
//            public final int n;
//            public final long v;
//            public NV(final int n, final long v) {
//                this.n = n;
//                this.v = v;
//            }
//            @Override
//            public int compareTo(final NV o) {
//                if (o.n > this.n)
//                    return -1;
//                if (o.n < this.n)
//                    return 1;
//                return 0;
//            }
//        }
//
//        final IReducer<VS, ES, Void, T> outerReducer = new IReducer<VS, ES, Void, T>() {
//
//            final HistogramReducer innerReducer = new HistogramReducer();
//
//            @Override
//            public void visit(IGASState<VS, ES, Void> state, Value u) {
//
//                innerReducer.visit(state, u);
//                
//            }
//
//            @Override
//            public T get() {
//                
//                final Map<Integer, AtomicLong> h = innerReducer.get();
//
//                final NV[] a = new NV[h.size()];
//
//                int i = 0;
//
//                for (Map.Entry<Integer, AtomicLong> e : h.entrySet()) {
//
//                    a[i++] = new NV(e.getKey().intValue(), e.getValue().get());
//
//                }
//
//                Arrays.sort(a);
//
//                System.out.println("distance, frontierSize, sumFrontierSize");
//                long sum = 0L;
//                for (NV t : a) {
//
//                    System.out.println(t.n + ", " + t.v + ", " + sum);
//
//                    sum += t.v;
//
//                }
//
//                return null;
//            }
//            
//        };
//        
//        return outerReducer;
//
//    }

}
