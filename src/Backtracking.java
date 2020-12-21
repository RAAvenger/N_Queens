import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class Backtracking {
    private int variablesCount;
    private Random randomGenerator;
    private ThreadPoolExecutor executor;

    public Backtracking(int variablesCount, Random randomGenerator, ThreadPoolExecutor executor) {
        this.variablesCount = variablesCount;
        this.randomGenerator = randomGenerator;
        this.executor = executor;
    }

    /**
     * run recursive backtracking algorithm to fill all variables using N_Queen constraints.
     *
     * @param assignments pairs of variables and their values.
     * @return "true" if we found an result, otherwise "false".
     */
    public boolean FindResult(HashMap<Integer, Integer> assignments) throws InterruptedException {
        System.out.println(assignments.size());
        if (assignments.size() == variablesCount)
            return true;
        /** using multithreading to find variable. */
        HashSet<Integer> conflictedValues1 = new HashSet<>();
        AtomicInteger variable1 = new AtomicInteger();
        Future future1 = executor.submit(() -> variable1.set(
                FindVariableWithMRV(
                        assignments,
                        conflictedValues1,
                        (int) Math.ceil((float) (variablesCount - assignments.size()) / 2),
                        1
                )
        ));
        HashSet<Integer> conflictedValues2 = new HashSet<>();
        AtomicInteger variable2 = new AtomicInteger();
        Future future2 = executor.submit(() -> variable2.set(
                FindVariableWithMRV(
                        assignments,
                        conflictedValues2,
                        (int) Math.floor((float) (variablesCount - assignments.size()) / 2),
                        -1
                )
        ));
        try {
            future1.get();
            future2.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        int variable;
        HashSet<Integer> conflictedValues;
        if (variable2.get() != -1 && conflictedValues1.size() < conflictedValues2.size()) {
            variable = variable2.get();
            conflictedValues = conflictedValues2;
        } else {
            variable = variable1.get();
            conflictedValues = conflictedValues1;
        }
        if (conflictedValues.size() == variablesCount)
            return false;
        /** using multithreading to fill queue of values. */
        PriorityQueue<CandidateValue> candidateValues = new PriorityQueue<>();
        future1 = executor.submit(() -> candidateValues.addAll(GetCandidateValuesForVariable(
                assignments,
                variable,
                conflictedValues,
                (int) Math.ceil((float) (variablesCount - conflictedValues.size()) / 2),
                1
        )));
        future2 = executor.submit(() -> candidateValues.addAll(GetCandidateValuesForVariable(
                assignments,
                variable,
                conflictedValues,
                (int) Math.floor((float) (variablesCount - conflictedValues.size()) / 2),
                -1
        )));
        try {
            future1.get();
            future2.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        while (!candidateValues.isEmpty()) {
            int value = candidateValues.poll().value;
            assignments.put(variable, value);


            System.out.println("gf " + variable + ": " + value);


            if (FindResult(assignments))
                return true;
            assignments.remove(variable);
        }
        return false;
    }

    /**
     * find a variable with smallest domain of values( Minimum Remaining Values ).
     *
     * @param assignments             pairs of variables and their values.
     * @param conflictedValues        set of conflicted values for best variable.
     * @param checkableVariablesCount it says how many variable should we check.
     * @param directionOfCheck        if DOF is equal with "1" check direction is forward
     *                                otherwise( DOF equals "-1" ) check direction is backward.
     * @return the variable with Minimum Remaining Values( the one with smallest domain )
     * or "-1" if checkableVariablesCount is equal with 0.
     */
    private int FindVariableWithMRV(
            HashMap<Integer, Integer> assignments,
            HashSet<Integer> conflictedValues,
            int checkableVariablesCount,
            int directionOfCheck
    ) {
        if (assignments.isEmpty()) {
            return randomGenerator.nextInt(variablesCount);
        } else if (checkableVariablesCount == 0)
            return -1;
        int maxConflictedValuesCount = 0;
        int bestVariable = -1;
        for (
                int i = directionOfCheck == 1 ? 0 : variablesCount - 1, cvc = 0;
                (i < variablesCount && i >= 0) && cvc < checkableVariablesCount;
                i += directionOfCheck
        ) {
            if (!assignments.containsKey(i)) {
                cvc++;
                HashSet<Integer> conflictedValuesOfCurrentVariable = new HashSet<>();
                for (int variable : assignments.keySet()) {
                    conflictedValuesOfCurrentVariable.add(assignments.get(variable));
                    /** first line equation( y2-y1=m*(x2-x1) ):
                     *  "x1=variable", "x2=i", "m=1", "y1=assignment.get(variable)" and "y2=value=?".
                     */
                    int value = variable - i + assignments.get(variable);
                    if (value < variablesCount && value >= 0)
                        conflictedValuesOfCurrentVariable.add(value);
                    /** second line equation:
                     *  "x1=variable", "x2=i", "m=-1", "y1=assignment.get(variable)" and "y2=value=?".
                     */
                    value = -variable + i + assignments.get(variable);
                    if (value < variablesCount && value >= 0)
                        conflictedValuesOfCurrentVariable.add(value);
                }
                if (conflictedValuesOfCurrentVariable.size() > maxConflictedValuesCount) {
                    maxConflictedValuesCount = conflictedValuesOfCurrentVariable.size();
                    bestVariable = i;
                    conflictedValues.clear();
                    conflictedValues.addAll(conflictedValuesOfCurrentVariable);
                }
            }
        }
        return bestVariable;
    }

    /**
     * get possible values and their conflicts for given variable using
     * assignments( some kind of map for those variables that we set a value for them ).
     *
     * @param assignments          hash map of variables( as keys ) and values ( well ... as values )
     *                             that we already fount( it means we set those pairs before ).
     * @param variable             index of the variable that we want find some values for.
     * @param conflictedValues     values that are out of the variable's domain of values.
     * @param checkableValuesCount it says how many values should we check.
     * @param directionOfCheck     if DOF is equal with "1" check direction is forward
     *                             otherwise( DOF equals "-1" ) check direction is backward.
     * @return priority queue of condition values( order is number of conflicts. ).
     */
    public PriorityQueue<CandidateValue> GetCandidateValuesForVariable(
            HashMap<Integer, Integer> assignments,
            int variable,
            HashSet<Integer> conflictedValues,
            int checkableValuesCount,
            int directionOfCheck
    ) {
        PriorityQueue<CandidateValue> candidateValues = new PriorityQueue<>();
        if (checkableValuesCount == 0)
            return candidateValues;
        for (
                int value = directionOfCheck == 1 ? 0 : variablesCount - 1, cvc = 0;
                value < variablesCount && cvc < checkableValuesCount;
                value += directionOfCheck
        ) {
            if (!conflictedValues.contains(value)) {
                cvc++;
                HashMap<Integer, HashSet<Integer>> intersections = new HashMap<>();
                int conflicts = -3;
                /**
                 * find points that are already out of other variables domains because of a pair of variable-value
                 * so those values are out of this pair's new conflicts( it means this pair doesn't remove those values from a variable domain ).
                 */
                for (int anotherVariable : assignments.keySet()) {
                    /**
                     * a: line1, b: line2, c: intersection point.
                     * a   b
                     *   c
                     * b   a
                     */
                    int m1 = 1;
                    int m2 = -1;
                    int b1 = variable - (m1 * value);
                    int b2 = anotherVariable - (m2 * assignments.get(anotherVariable));
                    float intersectionValue = (float) (b1 - b2) / (float) (m2 - m1);
                    if (intersectionValue % 1 == 0 && intersectionValue >= 0 && intersectionValue < variablesCount) {
                        int intersectionVariable = b2 - m2 * (int) intersectionValue;
                        if (!intersections.containsKey(intersectionVariable))
                            intersections.put(intersectionVariable, new HashSet<>());
                        intersections.get(intersectionVariable).add((int) intersectionValue);
                    }
                    /**
                     * a: line1, b: line2, c: intersection point.
                     * b   a
                     *   c
                     * a   b
                     */
                    m1 = -1;
                    m2 = 1;
                    b1 = variable - (m1 * value);
                    b2 = anotherVariable - (m2 * assignments.get(anotherVariable));
                    intersectionValue = (float) (b1 - b2) / (float) (m2 - m1);
                    if (intersectionValue % 1 == 0 && intersectionValue >= 0 && intersectionValue < variablesCount) {
                        int intersectionVariable = b2 - m2 * (int) intersectionValue;
                        if (!intersections.containsKey(intersectionVariable))
                            intersections.put(intersectionVariable, new HashSet<>());
                        intersections.get(intersectionVariable).add((int) intersectionValue);
                    }
                    /**
                     * a: line1, b: line2, c: intersection point.
                     * b
                     * a c a
                     *     b
                     */
                    m1 = 0;
                    m2 = -1;
                    b1 = variable - (m1 * value);
                    b2 = anotherVariable - (m2 * assignments.get(anotherVariable));
                    intersectionValue = (float) (b1 - b2) / (float) (m2 - m1);
                    if (intersectionValue % 1 == 0 && intersectionValue >= 0 && intersectionValue < variablesCount) {
                        int intersectionVariable = b2 - m2 * (int) intersectionValue;
                        if (!intersections.containsKey(intersectionVariable))
                            intersections.put(intersectionVariable, new HashSet<>());
                        intersections.get(intersectionVariable).add((int) intersectionValue);
                    }
                    /**
                     * a: line1, b: line2, c: intersection point.
                     *     b
                     * a c a
                     * b
                     */
                    m1 = 0;
                    m2 = 1;
                    b1 = variable - (m1 * value);
                    b2 = anotherVariable - (m2 * assignments.get(anotherVariable));
                    intersectionValue = (float) (b1 - b2) / (float) (m2 - m1);
                    if (intersectionValue % 1 == 0 && intersectionValue >= 0 && intersectionValue < variablesCount) {
                        int intersectionVariable = b2 - m2 * (int) intersectionValue;
                        if (!intersections.containsKey(intersectionVariable))
                            intersections.put(intersectionVariable, new HashSet<>());
                        intersections.get(intersectionVariable).add((int) intersectionValue);
                    }
                }
                /**
                 * find new conflicts that been created because line1 passing this point( variable, value ):
                 *     1
                 *   1
                 * 1
                 */
                for (int i = variable - Math.min(value, variable), j = value - Math.min(value, variable); i < variablesCount && j < variablesCount; i++, j++) {
                    if (!assignments.containsKey(i) && !assignments.containsValue(j) && !(intersections.containsKey(i) && intersections.get(i).contains(j)))
                        conflicts++;
                }
                /**
                 * find new conflicts that been created because line2 passing this point( variable, value ):
                 * 2
                 *   2
                 *     2
                 */
                for (int i = variable + Math.min(variablesCount - variable, value), j = value - Math.min(variablesCount - variable, value); i >= 0 && j < variablesCount; i--, j++) {
                    if (!assignments.containsKey(i) && !assignments.containsValue(j) && !(intersections.containsKey(i) && intersections.get(i).contains(j)))
                        conflicts++;
                }
                /**
                 * find new conflicts that been created because line3 passing point( variable, value ):
                 * 3 3 3
                 */
                for (int i = 0, j = value; i < variablesCount; i++) {
                    if (!assignments.containsKey(i) && !(intersections.containsKey(i) && intersections.get(i).contains(j)))
                        conflicts++;
                }
                candidateValues.add(new CandidateValue(value, conflicts));
            }
        }
        return candidateValues;
    }
}

/**
 * simple class for candidate values of variables.
 */
class CandidateValue implements Comparable {
    int value;
    int conflicts;

    public CandidateValue(int value, int conflicts) {
        this.value = value;
        this.conflicts = conflicts;
    }

    @Override
    public int compareTo(Object o) {
        try {
            CandidateValue cv = (CandidateValue) o;
            return conflicts - cv.conflicts;
        } catch (Exception e) {
            return 0;
        }
    }
}