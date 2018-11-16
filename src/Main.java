import java.io.File;
import java.util.*;

public class Main {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String basePath = "src/www/";

        System.out.println("Enter name of abstracts folder: ");
        String abstracts = sc.next() + "/";
        System.out.println(abstracts);
        System.out.println("Enter name of gold folder: ");
        String gold = sc.next() + "/";
        System.out.println(gold);
        String goldPath = basePath + gold;

        File folder = new File(basePath + abstracts);

        //get all files inside the abstract folder
        List<File> files = new ArrayList<>();
        Utilities.readFiles(folder, files);

        /**
         *
         * You can provide the following inputs to TextProcessor constructor:
         * removePunctuation: true - remove
         *                    false - do not remove
         * regex: if you want to provide your own regex for remoing function
         *        else pass "" or null and a default regex will be used
         * doesStem: true - do stemming
         *           false - do not do stemming
         * removeStopwords: true - remove
         *                  false: do not remove
         * delimiter: pass TextProcessor.SplitToken.SPACE
         * TextProcessor(boolean removePunctuation, String regex, boolean doesStem,
         *         boolean removeStopWords, SplitToken delimiter) {
         */

        //Initialise TextProcessor with the required parameters
        TextProcessor textProcessor = new TextProcessor(true, "", true,
                true, TextProcessor.SplitToken.SPACE);

        //to keep count of files
        int fileCount = 0;

        //arrays to hold MRR for k=1 to 10
        double MRRPageRank[] = new double[10];
        double MRRTF_IDF[] = new double[10];

        /*** PageRank ***/
        //calculate page rank score for each file in abstracts folder
        for (File file : files) {

            String name = file.getName();
            File goldFile = new File(goldPath + name);

            //do further processing if gold file exists
            if (goldFile.exists()) {

                //get gold standard key words
                FileIterator fileIterator = FileFactory.getInstance("REGULAR", goldFile);
                Set<String> goldStandard = new HashSet<>();

                Porter stemmer = new Porter();
                while(fileIterator.hasNext()) {
                    String[] splits = fileIterator.next().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for(String split: splits) {
                        sb.append(stemmer.stripAffixes(split)+ " ");
                    }
                    goldStandard.add(sb.toString().trim());
                }

                //get list of tokens for that file
                List<TextProcessor.Token> processedTokens = textProcessor.process("REGULAR", file);

                //create word graph from the tokens
                WordGraph wordGraph = new WordGraph(processedTokens);

                //calculate PageRank score for each word in word graph
                Map<String, Double> score = new PageRank(wordGraph.wordGraph, 0.85).pageRank;

                //create N-Grams from 1 to 3 from the word graph
                Set<String> nGrams = new HashSet<>();
                Iterator<WordGraph.Token> iterator = wordGraph.wordGraph.keySet().iterator();
                while (iterator.hasNext()) {
                    WordGraph.Token token = iterator.next();
                    NGram.getNgrams(3, 0, token, wordGraph.wordGraph, nGrams,
                            token.val, wordGraph.hashes, wordGraph.adjacentHashes);
                }

                //get combined PageRank score for all N-Grams
                Map<String, Double> nGramPageRank = Scorer.pageRank(score, nGrams);

                //rank N-Grams based on their scores
                Map<Double, List<String>> rankedNGrams = rankNGrams(nGramPageRank);

                //get top 10 N-grams
                List<String > topKNGrams = getTopKNGrams(rankedNGrams, 10);

                //calculate MRR for k=1 to 10
                int count = 1;
                int rank = 0;
                for(String word: topKNGrams) {
                    if(goldStandard.contains(word)) {
                        rank = count;
                        break;
                    }
                    count++;
                }
                int i=0;
                while(i<rank-1) {
                    MRRPageRank[i++] += 0;
                }
                double MRR = 0;
                if(rank != 0) {
                    MRR = 1.0/rank;
                }
                while(i<10) {
                    MRRPageRank[i++] += MRR;
                }
                fileCount++;
            }
        }

        /*** TF-IDF ***/

        // get a map for each word and its tf and df
        Indexer indexer = new Indexer(textProcessor, folder, "REGULAR", basePath);
        Map<String, Indexer.TokenInfo> invertedIndex = indexer.getInvertedIndex();

        //calculate tf-idf score for N-Grams in each file in abstracts folder
        for (File file : files) {

            String name = file.getName();
            File goldFile = new File(goldPath + name);

            //do further processing if gold file exists
            if (goldFile.exists()) {

                //get gold standard key words
                FileIterator fileIterator = FileFactory.getInstance("REGULAR", goldFile);
                Set<String> goldStandard = new HashSet<>();

                Porter stemmer = new Porter();
                while(fileIterator.hasNext()) {
                    String[] splits = fileIterator.next().split(" ");
                    StringBuilder sb = new StringBuilder();
                    for(String split: splits) {
                        sb.append(stemmer.stripAffixes(split)+ " ");
                    }
                    goldStandard.add(sb.toString().trim());
                }

                //get list of tokens for that file
                List<TextProcessor.Token> processedTokens = textProcessor.process("REGULAR", file);

                //create word graph from the tokens
                WordGraph wordGraph = new WordGraph(processedTokens);

                //create N-Grams from 1 to 3 from the word graph
                Set<String> nGrams = new HashSet<>();
                Iterator<WordGraph.Token> iterator = wordGraph.wordGraph.keySet().iterator();
                while (iterator.hasNext()) {
                    WordGraph.Token token = iterator.next();
                    NGram.getNgrams(3, 0, token, wordGraph.wordGraph, nGrams,
                            token.val, wordGraph.hashes, wordGraph.adjacentHashes);
                }

                //get combined TF-IDF score for all N-Grams
                Map<String, Double> nGramTFIDF = Scorer.TFIDF(invertedIndex, nGrams, Integer.parseInt(file.getName()), fileCount);

                //rank NGrams
                Map<Double, List<String>>rankedNGrams = rankNGrams(nGramTFIDF);

                //get top 10 N-grams
                List<String> topKNGrams = getTopKNGrams(rankedNGrams, 10);

                int count = 1;
                int rank = 0;
                for(String word: topKNGrams) {
                    if(goldStandard.contains(word)) {
                        rank = count;
                        break;
                    }
                    count++;
                }
                int i=0;
                while(i<rank-1) {
                    MRRTF_IDF[i++] += 0;
                }
                double MRR = 0;
                if(rank !=0) {
                    MRR = 1.0/rank;
                }
                while(i<10) {
                    MRRTF_IDF[i++] += MRR;
                }
            }
        }

        for(int i=0; i<10; i++) {
            System.out.println("MRR for top "+ (i+1) + " words: " +   MRRPageRank[i]/fileCount + "(PageRank), "
                                + MRRTF_IDF[i]/fileCount + "(TF-IDF)");
        }
    }

    private static List<String> getTopKNGrams(Map<Double, List<String>> rankedNGrams, int K) {
        List<String> topKNGrams = new ArrayList<>();
        Collection<List<String>> entries = rankedNGrams.values();
        for(List<String> entry: entries) {

            for(String phrase: entry) {
                topKNGrams.add(phrase);
                if (topKNGrams.size() == K) {
                    return topKNGrams;
                }
            }
        }
        return topKNGrams;
    }

    // LinkedHashSet ensures order of insertion
    private static Map<Double, List<String>> rankNGrams(Map<String, Double> nGrams) {

        //TreeMap to sort PageRank score in decreasing order
        Map<Double, List<String>> rankedNGrams = new TreeMap<>(Collections.reverseOrder());

        Set<Map.Entry<String, Double>> words = nGrams.entrySet();
        //for each relevant document
        for (Map.Entry<String, Double> word: words) {
            if (rankedNGrams.containsKey(word.getValue())) {
                rankedNGrams.get(word.getValue()).add(word.getKey());
            } else {
                List<String> list = new LinkedList<>();
                list.add(word.getKey());
                rankedNGrams.put(word.getValue(), list);
            }
        }
        return rankedNGrams;
    }
}