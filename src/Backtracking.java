import java.util.HashMap;
import java.util.HashSet;

public class Backtracking {
    int variablesCount;

    public Backtracking(int variablesCount) {
        this.variablesCount = variablesCount;
    }

    public boolean FindResult(HashMap<Integer, Integer> assignments) {
        if (assignments.size() == variablesCount)
            return true;
        HashSet<Integer> conflictedValues = new HashSet<>();
        int variable = FindVariableWithMRV(assignments, conflictedValues);
        for (int value = 0; conflictedValues.size() < variablesCount && value < variablesCount; value++)
            if (!conflictedValues.contains(value)) {
                assignments.put(variable, value);
                if (FindResult(assignments))
                    return true;
                else {
                    assignments.remove(variable);
                }
            }
        if (assignments.size() > 220)
            System.out.println(assignments.size());
        return false;
    }

    /**
     * find a variable with smallest domain of values( Minimum Remaining Values ).
     *
     * @param assignments      pairs of variables and their values.
     * @param conflictedValues set of conflicted values for best variable.
     * @return the variable with Minimum Remaining Values( the one with smallest domain ).
     */
    private int FindVariableWithMRV(HashMap<Integer, Integer> assignments, HashSet<Integer> conflictedValues) {
        if (assignments.isEmpty()) {
            return variablesCount / 2;
        }
        int maxConflictedValuesCount = 0;
        int bestVariable = -1;
        for (int i = 0; i < variablesCount; i++)
            if (!assignments.containsKey(i)) {
                HashSet<Integer> conflictedValuesOfCurrentVariable = new HashSet<>();
                for (int variable : assignments.keySet()) {
                    conflictedValuesOfCurrentVariable.add(assignments.get(variable));
                    /**first line equation( y2-y1=m*(x2-x1) ): "x1=variable", "x2=i", "m=1", "y1=assignment.get(variable)" and "y2=value=?".*/
                    int value = variable - i + assignments.get(variable);
                    if (value < variablesCount && value >= 0)
                        conflictedValuesOfCurrentVariable.add(value);
                    /**second line equation: "x1=variable", "x2=i", "m=-1", "y1=assignment.get(variable)" and "y2=value=?".*/
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
        return bestVariable;
    }

}