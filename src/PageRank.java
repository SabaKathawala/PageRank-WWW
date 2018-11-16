import java.util.HashMap;
import java.util.Map;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Input: WordGraph, damping factor
 * Output: PageRank score of unigrams
 *
 */

public class PageRank {

    Map<String, Double> pageRank;

    PageRank(Map<WordGraph.Token, Map<WordGraph.Token, Integer>> wordGraph, double dampingFactor) {

        pageRank = new HashMap<>();

        double alphaInverse = 1-dampingFactor;
        Map<String, Double> temporaryPageRankScore = new HashMap<>();
        fill(wordGraph, pageRank, 1.0/wordGraph.size());

        double teleportConstant = alphaInverse/wordGraph.size();
        int i = 1;
        while(i<=10) {

            for(WordGraph.Token vi: wordGraph.keySet()) {
                double currentScore = 0.0;
                //calculates inner sum
                for(Map.Entry<WordGraph.Token, Integer> vj: wordGraph.get(vi).entrySet()) {
                    double svj = pageRank.get(vj.getKey().val);
                    double adjacentScore = calculateScore(vj.getKey(), wordGraph);
                    if(adjacentScore > 0) {
                        int wij = vj.getValue();
                        currentScore += wij*svj/adjacentScore;
                    }
                }
                currentScore *= dampingFactor;
                currentScore += teleportConstant;

                temporaryPageRankScore.put(vi.val, currentScore);
            }
            pageRank = temporaryPageRankScore;
            temporaryPageRankScore = new HashMap<>();
            i++;
        }

    }

    private void fill(Map<WordGraph.Token, Map<WordGraph.Token, Integer>> wordGraph, Map<String, Double> pageRankScore,
                      double initialValue) {
        for(WordGraph.Token token: wordGraph.keySet()) {
            pageRankScore.put(token.val, initialValue);
        }
    }

    private int calculateScore(WordGraph.Token vij, Map<WordGraph.Token, Map<WordGraph.Token, Integer>> wordGraph) {
        int score = 0;
        for(Integer wjk: wordGraph.get(vij).values()) {
            score += wjk;
        }
        return score;
    }
}
