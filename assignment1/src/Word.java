import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLOutput;
import java.util.*;
public class Word {

    //my anagram algorithm works using recursion, the base case being when the startIndex=word length, once this base is
    //reached, the permutation is added, the default case starts a for loop from startIndex to word length, then will
    //swap i with startIndex and call the procedure again with the same word, but startIndex incremented by 1, then the
    //swap is undone in order to still work later in the for loop. Once this process has finished the fullAnagrams list
    //will be full. It has a time complexity of O(n!), because on the first recursion level there will be n
    //branches made swaping the first letter for each of the others including itself, then on the next level there will
    //be n-1 branches, all the way until the base case with 0 therefore the time complexity=nx(n-1)x...x1=n!.
    //the Partial anagram function calculates all subsets of the word, then calculates the full anagrams for each of them
    //and since there are n choose k subsets of size k which can be make from a word of size n, the complexity will be
    //the sum from k=1 to n of: (n choose k)*k!.

    private char[] original;
    private int n;
    private ArrayList<String> fullAnagrams = new ArrayList<>();
    private ArrayList<String> partialAnagrams = new ArrayList<>();
    public int testsTimeTaken;

    /**
     * Constructor to set the original word, n (length), sort the original and call makeFullAnagrams.
     * @param word the word used to make the anagrams.
     */
    public Word(String word) {
        original = word.toUpperCase().toCharArray();
        n = original.length;
        original=selectionSort(original);
        makeFullAnagrams(original,0);
    }

    private char[] selectionSort(char[] arr) {
        for(int j=0;j<arr.length;j++){
            int smallest = j;
            int k=0;
            for(k=j+1;k<arr.length;k++){
                if(arr[k] < arr[smallest]){
                    smallest = k;
                }
            }
            swap(j,smallest,arr);
        }
        return arr;
    }

    private void swap(int i, int j, char[] arr){
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j]=temp;
    }

    /**
     * Main method, used for testing.
     * @param args
     */
    public static void main(String[] args) {
        Word word = new Word("");
        ArrayList<String> tests = new ArrayList<>();
        //edge cases
        tests.add("aaaaaa");tests.add("zzzzzz");tests.add("h");tests.add("lkjhgfds");
        try(Scanner file = new Scanner(new File("src/Finnish.tsv"))) {
            file.nextLine();
            for(int i=0;i<96;i++){
                String[] line = file.nextLine().split("\t");
                while(line[0].length()>8){
                    line = file.nextLine().split("\t");
                }
                tests.add(line[0]);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        for(int i=0;i<tests.size();i++){
            System.out.println("\nTest "+(i+1)+": "+tests.get(i)+" length: "+tests.get(i).length());
            word.searchAnagrams(tests.get(i));
        }
        System.out.println("\nAverage time taken for all tests: "+word.testsTimeTaken/100+" milliseconds");
        System.out.println("For a set of words ranging from 1 letter to 8");
    }

    /**
     * Accessor to return full anagrams.
     * @return array list of full anagrams.
     */
    public ArrayList<String> getFullAnagrams() {
        return fullAnagrams;
    }

    /**
     * Accessor to either make or return partial anagrams.
     * @return array list of partial anagrams.
     */
    public ArrayList<String> getPartialAnagrams() {
        if(partialAnagrams.isEmpty()){
            makePartialAnagrams();
        }
        return partialAnagrams;
    }

    /**
     * Procedure which will recursively create all the different full length anagrams of the original word.
     * @param perm The current permutation of the characters in the string.
     * @param startI The index that we are currently looking at swapping.
     */
    public void makeFullAnagrams(char[] perm, int startI) {
        if (startI == perm.length){
            fullAnagrams.add(new String(perm));
        }
        ArrayList<Character> used =new ArrayList<>();
        for (int i = startI; i < perm.length; i++){
            if(used.contains(perm[i])){
               continue;
            }
            used.add(perm[i]);
            swap(i, startI, perm);
            makeFullAnagrams(perm, startI + 1);
            swap(i, startI, perm);
        }
    }

    /**
     * Function which uses all the different binary strings in order to return an array list of all possible sub sets of
     * the word passed in.
     * @param word The word we want to make sub sets of.
     * @return Array list of all sub words.
     */
    public ArrayList<String> subWordsOf(char[] word) {
        ArrayList<String> allSubWords = new ArrayList<>();
        int binaryVals = (int) Math.pow(2, n);
        for (int i=1;i<binaryVals;i++){
            String subWord="";
            String binary = Integer.toBinaryString(i);
            for(int j=0;j<binary.length();j++){
                if(binary.charAt(binary.length()-j-1)=='1'){
                    subWord = subWord + word[j];
                }
            }
            if(!allSubWords.contains(subWord)) {
                allSubWords.add(subWord);
            }
        }

        return allSubWords;
    }

    /**
     * Uses the subWordsOf and getFullAnagrams functions in order to create an array list of all anagrams of any length.
     */
    public void makePartialAnagrams() {
        ArrayList<String> subWords = subWordsOf(original);
        for(int i=0;i<subWords.size();i++){
            Word word = new Word(subWords.get(i));
            partialAnagrams.addAll(word.getFullAnagrams());
        }
    }

    /**
     * Reads in the Italian tsv file, splits it up and inserts an array of two strings into the binary search tree.
     */
    public void createDictionary(){
        //I chose to use a binary search tree, this is because to search in one it is only O(h) which is the same as
        //0(log(n)) for a balanced tree or at worst O(n) for a fully skewed tree. Therefore, meaning that on average it
        //will be better than linear time. This makes it good for repeated searches because despite taking longer to
        //insert, that time will be made up due to all the searching.
        try(Scanner file = new Scanner(new File("src/Italian.tsv"))) {
            file.nextLine();
            while (file.hasNextLine()) {
                String[] line = file.nextLine().split("\t");
                insert(line);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private TreeNode root;
    private class TreeNode {
        String[] key;
        TreeNode left;
        TreeNode right;
        TreeNode parent;

        /**
         * Constructor to initialise the key, left node, right node and parent node.
         * @param key The array of two strings.
         */
        public TreeNode(String[] key) {
            this.key = key;
            this.left = null;
            this.right = null;
            this.parent = null;
        }
    }

    /**
     * Procedure to insert a node into the current binary search tree, then set the correct left or right pointer to the
     * parent.
     * @param key The array of two strings.
     */
    public void insert (String[] key){
        TreeNode x = root;
        TreeNode y=null;
        while(x != null) {
            y = x;
            if (key[0].compareTo(x.key[0]) < 0) {
                x = x.left;
            } else {
                x = x.right;
            }
        }
        TreeNode z = new TreeNode(key);
        if(y == null){
            root = z;
        }
        else{
            if(key[0].compareTo(y.key[0]) < 0){
                y.left = z;
            }
            else{
                y.right = z;
            }
        }
    }

    /**
     * Procedure, which takes in any string, compute all the anagrams of any length, then search through the binary tree
     * for each anagram, and outputs any anagrams that are in the italian tsv file.
     * @param s the word to make anagrams and search for.
     */
    public void searchAnagrams(String s){
        //The average time taken per search is between 3 and 5 milliseconds. In theory, if the binary search tree is
        //perfectly balanced, the time complexity for the search will be O(log(n)). This is because, each time you move
        //left or right, you will exactly half the tree. Therefore, in the worst case you will need to half n until it
        //gets to one. Meaning that in the worst case you will have to move left or right log(n) times. However, if the
        //tree is completely imbalanced, then the worst case would be O(n). This is because, in the worst case it will
        //keep going one way, comparing to every single value in the tree. Making it O(n).
        if(root==null) {
            createDictionary();
        }
        Word word = new Word(s);
        ArrayList<String> anagrams = word.getPartialAnagrams();
        long startTime = System.currentTimeMillis();
        for(String anagram:anagrams){
            TreeNode x = root;
            while(x != null && !anagram.toLowerCase().equals(x.key[0])){
                if(anagram.toLowerCase().compareTo(x.key[0]) < 1){
                    x = x.left;
                }
                else{
                    x = x.right;
                }
            }
            if(x!=null){
                System.out.println("Italian: "+x.key[0] + " English: " + x.key[1]);
            }
        }
        long elapsedTime = System.currentTimeMillis() -startTime;
        testsTimeTaken += elapsedTime;
        System.out.println("Time: "+elapsedTime+ " milliseconds");
    }
}
