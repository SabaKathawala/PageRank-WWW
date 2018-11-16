import java.util.List;
import java.util.Map;
import java.util.Set;


public class NGram {

    public static void getNgrams(int length, int currLength, WordGraph.Token key, Map<WordGraph.Token,
            Map<WordGraph.Token, Integer>> wordGraph, Set<String> NGram, String string, Map<String, Long> hashes,
                                 Map<String, Set<Long>> adjacentHashes) {
        if(currLength == length) {
            return;
        }

        NGram.add(string);
        for(WordGraph.Token token: wordGraph.get(key).keySet()) {
            for(long combinedHash: adjacentHashes.get(token.val)) {
                long currHash = combinedHash/hashes.get(token.val);
                if(key.adjacentHashes.contains(currHash) || key.hash == currHash) {
                    getNgrams(length, currLength + 1, token, wordGraph,
                             NGram, string + " " + token.val,hashes, adjacentHashes);
                }
            }
        }

    }
}
