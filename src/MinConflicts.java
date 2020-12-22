import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class MinConflicts {
    int variablesCount;
    Random randomGenerator;

    public MinConflicts(int variablesCount, Random randomGenerator) {
        this.variablesCount = variablesCount;
        this.randomGenerator = randomGenerator;
    }

    /**
     * use local search algorithm( minConflicts ) to solve the CSV problem.
     *
     * @return pairs of variable and value.
     */
    public HashMap<Integer, Integer> FindResult() {
        HashMap<Integer, Integer> assignments = FillBoardRandomly();
        HashSet<Integer> conflictedVariables = new HashSet<>();
        int conflicts = HasConflict(assignments, conflictedVariables);
        int randomChance = 0;
        while (conflicts != 0) {
            System.out.println(conflictedVariables.size());
            int randomVariableIndex = randomGenerator.nextInt(conflictedVariables.size());
            int randomVariable = (int) conflictedVariables.toArray()[randomVariableIndex];
            conflictedVariables.clear();
            int bestValue = GetBestValueForVariable(assignments, randomVariable, randomChance);
            assignments.replace(randomVariable, bestValue);
            int newConflicts = HasConflict(assignments, conflictedVariables);
            if (conflicts == newConflicts) {
                randomChance++;
                System.out.println("Random chance increased to: "+randomChance);
            } else
                randomChance = 0;
            conflicts = newConflicts;
        }
        return assignments;
    }

    /**
     * find value with minimum conflicts for given variable and return it.
     *
     * @param assignments  pairs of variables and values.
     * @param variable     randomly chosen variable.
     * @param randomChance chance of choosing a value randomly.
     * @return best value for given variable.
     */
    private int GetBestValueForVariable(HashMap<Integer, Integer> assignments, int variable, int randomChance) {
        if (randomChance > randomGenerator.nextInt(100))
            return randomGenerator.nextInt(variablesCount);
        int[] valuesConflicts = new int[variablesCount];
        for (int i = 0; i < variablesCount; i++) {
            if (i != variable) {
                valuesConflicts[assignments.get(i)] += 1;
                /** check this pair ( variable=i, value=assignments.get(i) ) has conflict with which values of "randomVariable"
                 * using line equation( y=(m*x)+b ): m=(1 or -1), x=randomVariable, b=assignments.get(i)-(m*i).
                 * */
                int y1 = (1 * variable) + (assignments.get(i) - (1 * i));
                int y2 = (-1 * variable) + (assignments.get(i) - (-1 * i));
                if (y1 >= 0 && y1 < variablesCount)
                    valuesConflicts[y1] += 1;
                if (y2 >= 0 && y2 < variablesCount)
                    valuesConflicts[y2] += 1;
            }
        }
        int bestValue = -1;
        int bestValueConflicts = Integer.MAX_VALUE;
        for (int i = 0; i < variablesCount; i++) {
            if (valuesConflicts[i] < bestValueConflicts) {
                bestValue = i;
                bestValueConflicts = valuesConflicts[i];
            }
        }
        return bestValue;
    }

    /**
     * check if there is conflicts in assignments.
     *
     * @param assignments         pairs of variables and values.
     * @param conflictedVariables an empty set to put conflicted variables in.
     * @return "true" if there is any conflict in assignments, otherwise "false".
     */
    private int HasConflict(HashMap<Integer, Integer> assignments, HashSet<Integer> conflictedVariables) {
        conflictedVariables.clear();
        for (int i = 0; i < variablesCount; i++) {
            for (int j = i + 1; j < variablesCount; j++) {
                int value1 = assignments.get(i);
                int value2 = assignments.get(j);
                if (value1 == value2) {
                    conflictedVariables.add(i);
                    conflictedVariables.add(j);
                    continue;
                }
                float m = (float) (j - i) / (float) (assignments.get(j) - assignments.get(i));
                if (m == 1 || m == -1) {
                    conflictedVariables.add(i);
                    conflictedVariables.add(j);
                }
            }
        }
        return conflictedVariables.size();
    }

    /**
     * create all assignments randomly.
     *
     * @return a hashMap of randomly created assignments.
     */
    private HashMap<Integer, Integer> FillBoardRandomly() {
        HashMap<Integer, Integer> assignments = new HashMap<>();
        for (int i = 0; i < variablesCount; i++) {
            int value = randomGenerator.nextInt(variablesCount);
            while (!assignments.isEmpty() && assignments.containsValue(value))
                value = randomGenerator.nextInt(variablesCount);
            assignments.put(i, value);
        }
        return assignments;
    }
}