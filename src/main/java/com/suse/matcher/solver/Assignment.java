package com.suse.matcher.solver;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A set of {@link Match}es which is a subset of all
 * {@link Match}es, as produced by OptaPlanner.
 */
@PlanningSolution
public class Assignment {

    /** Score of this assignment. */
    private HardSoftScore score;

    /** Match objects that the OptaPlanner will try to assign Kinds to. */
    private List<Match> matches;

    /** Other problem facts passed by Drools. */
    private Collection<Object> problemFacts;

    /** Maps every {@link Match} id to all conflicting sets where it appears. */
    private Map<Integer, List<List<Integer>>> conflictMap;

    /**
     * Default constructor, required by OptaPlanner.
     */
    public Assignment() {
    }

    /**
     * Standard constructor.
     *
     * @param matchesIn fact corresponding to possible matches
     * @param problemFactsIn any other problem facts
     * @param conflictMapIn maps every {@link Match} id to any conflicting sets where it appears
     */
    public Assignment(List<Match> matchesIn, Collection<Object> problemFactsIn,
            Map<Integer, List<List<Integer>>> conflictMapIn) {
        matches = matchesIn;
        problemFacts = problemFactsIn;
        conflictMap = conflictMapIn;
    }

    /**
     * @return the problem facts
     */
    @ProblemFactCollectionProperty
    public Collection<Object> getProblemFacts() {
        // those will be inserted in the private OptaPlanner Drools instance
        // so that they can be used in score rules
        return problemFacts;
    }

    /**
     * Returns a stream of problem facts filtered by type.
     *
     * @param <T> type of the facts
     * @param type of the facts
     * @return the facts as stream
     */
    @SuppressWarnings("unchecked") // no way around this in Java 8
    public <T> Stream<T> getProblemFactStream(Class<T> type) {
        return getProblemFacts().stream()
            .filter(o -> type.isAssignableFrom(o.getClass()))
            .map(o -> (T)o);
    }

    /**
     * Returns a Collection of problem facts filtered by type.
     *
     * @param <T> type of the facts
     * @param type of the facts
     * @return the facts as stream
     */
    public <T> Collection<T> getProblemFacts(Class<T> type) {
        return getProblemFactStream(type)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    /**
     * Set score
     *
     * @param scoreIn score
     */
    public void setScore(HardSoftScore scoreIn) {
        score = scoreIn;
    }

    /**
     * Returns Match objects that the OptaPlanner will try to confirm.
     *
     * @return the matches
     */
    @PlanningEntityCollectionProperty
    public List<Match> getMatches() {
        return matches;
    }

    /**
     * Returns values for a {@link Match} confirmed field that OptaPlanner will
     * change.
     *
     * @return the boolean values
     */
    @ValueRangeProvider(id = "booleanRange")
    public List<Boolean> getBooleans() {
        return new ArrayList<Boolean>(){{ add(Boolean.FALSE); add(Boolean.TRUE); }};
    }


    /**
     * Returns {@link Match} ids conflicting with the specified {@link Match}.
     * @param matchId a {@link Match} id
     * @return the conflicting ids
     */
    public Stream<Integer> getConflictingMatchIds(Integer matchId) {
        return conflictMap.get(matchId).stream()
            .flatMap(s -> s.stream())
            .filter(id -> id != matchId);
    }
}
