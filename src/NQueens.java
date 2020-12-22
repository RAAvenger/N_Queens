import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class NQueens {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("how many queens?");
        int n = scanner.nextInt();
//        Backtracking resultFinder = new Backtracking(n, new Random(new Date().getTime()));
//        HashMap<Integer, Integer> result = new HashMap<>();
//        resultFinder.FindResult(result);
        MinConflicts resultFinder = new MinConflicts(n, new Random(new Date().getTime()));
        HashMap<Integer, Integer> result = resultFinder.FindResult();
        for (int key : result.keySet())
            System.out.println(key + ": " + result.get(key));
        scanner.nextLine();
        scanner.nextLine();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (result.get(i) == j)
                    System.out.print("|Q");
                else
                    System.out.print("|_");
            }
            System.out.println("|");
        }
    }
}
