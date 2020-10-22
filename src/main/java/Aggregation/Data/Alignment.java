package Aggregation.Data;

public class Alignment {
    String topSequence;
    String leftSequence;

    private static int MATCHSCORE = 1;
    private static int MISMATCHSCORE = -1;
    private static int GAPSCORE = -1;

    public Alignment(String s1, String s2){
        computeAlignment(s1, s2);
    }

    public void computeAlignment(String s1, String s2){
        System.out.println("s1: " + s1);
        System.out.println("s2: " + s2);

        StringBuffer topSeq = new StringBuffer();
        StringBuffer leftSeq = new StringBuffer();
        int[][] editMatrix = computeEditMatrix(s1, s2);

        //Utils.Utils.printMatrix(editMatrix);
        printMatrix(editMatrix, s1, s2);

        int i = editMatrix.length - 1;
        int j = editMatrix[0].length - 1;
        while(i != 0 && j != 0){
            if(editMatrix[i][j] == editMatrix[i][j-1] + GAPSCORE){
                topSeq.insert(0, s1.charAt(j - 1));
                leftSeq.insert(0, '-');
                j--;
            }
            else if(editMatrix[i][j] == editMatrix[i - 1][j] + GAPSCORE){
                topSeq.insert(0, '-');
                leftSeq.insert(0, s2.charAt(i - 1));
                i--;
            }
            else {
                topSeq.insert(0, s1.charAt(j - 1));
                leftSeq.insert(0, s2.charAt(i - 1));
                i--;
                j--;
            }
        }

        this.topSequence = topSeq.toString();
        this.leftSequence = leftSeq.toString();
    }

    public int[][] computeEditMatrix(String s1, String s2){
        int[][] matrix = new int[s2.length() + 1][s1.length() + 1];
        int score;
        for (int i = 0; i <= s2.length(); i++)
            matrix[i][0] = 0 - i;
        for (int j = 0; j <= s1.length(); j++)
            matrix[0][j] = 0 - j;
        for (int i = 1; i <= s2.length(); i++)
            for (int j = 1; j <= s1.length(); j++) {
                if (s1.charAt(j - 1) == s2.charAt(i - 1))
                    score = MATCHSCORE;
                else
                    score = MISMATCHSCORE;
                matrix[i][j] = max(matrix[i - 1][j - 1] + score, matrix[i - 1][j] + GAPSCORE, matrix[i][j - 1] + GAPSCORE);
            }
        return matrix;
    }

    public int getCost(){
        int cost = 0;
        for(int i = 0; i < this.topSequence.length(); i++)
            if(this.topSequence.charAt(i) != this.leftSequence.charAt(i))
                cost++;
            return cost;
    }

    private int max(int diag, int top, int left) {
        return Math.max(Math.max(diag, top), left);
    }

    public static void printMatrix(int[][] matrix, String s1, String s2){
        System.out.print("- | ");
        for (int j = 0; j < s1.length(); j++) {
            System.out.printf("%2s | ", s1.charAt(j));
            //System.out.print(s1.charAt(j) + "  | ");
        }
        System.out.println();
        for (int i = 1; i < s2.length() + 1; i++) {
            System.out.print(s2.charAt(i - 1) + " | ");
            for (int j = 1; j < s1.length() + 1; j++)
                if(j != 1)
                    System.out.printf("%3s |",matrix[i][j]);
                else
                    System.out.printf("%2s |",matrix[i][j]);
            System.out.println();
        }
    }

    @Override
    public String toString() {
        return topSequence + "\n" + leftSequence + " (cost = " + getCost() + ")";
    }
}