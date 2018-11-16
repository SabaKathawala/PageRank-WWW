import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Contains methods to calculate PageRank and TFIDF scores
 *
 */
public class Scorer {

    public static Map<String, Double> pageRank(Map<String, Double> textRank,  Set<String> NGrams) {
        Map<String, Double> nGramPageRank = new HashMap<>();
        for(String token: NGrams) {
            double score = 0;
            for(String unigram: token.split("\\s+")) {
                score += textRank.get(unigram);
            }
            nGramPageRank.put(token, score);
        }
        return nGramPageRank;
    }

    public static Map<String, Double> TFIDF(Map<String, Indexer.TokenInfo> invertedIndex, Set<String> NGrams, int docId, double fileCount) {
        Map<String, Double> nGramTFIDF = new HashMap<>();
        for(String token: NGrams) {
            double score = 0;
            for(String unigram: token.split("\\s+")) {
                Indexer.TokenInfo map = invertedIndex.get(unigram);
                Map<Integer, Indexer.DocumentInfo> doc = map.docToTermFrequencyMap;
                score += (Math.log(fileCount/doc.size())/Math.log(2)) * doc.get(docId).tf;
            }
            nGramTFIDF.put(token, score);
        }
        return nGramTFIDF;
    }
}
