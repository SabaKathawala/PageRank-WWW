import java.io.*;
import java.util.*;
import java.util.List;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Input: Documents that are read one by one from the collection
 * Output: List of Tokens to be added to the index
 *
 */

public class TextProcessor {

    private final SplitToken delimiter;
    private List<File> listOfFiles = new ArrayList<>();

    enum SplitToken {
        SPACE(" ");

        private String splitToken;

        SplitToken(String splitToken) {
            this.splitToken = splitToken;
        }

        public String getSplitToken() {
            return splitToken;
        }
    }

    private Set<String> pos;


    //remove everything other than lowercase and uppercase letters
    private static final String DEFAULT_REGEX = "[^a-zA-Z0-9]";

    private String regex;

    //set to store stop words
    private Set<String> stopWords;

    //object to hold a Stemmer
    Porter stemmer = null;

    boolean removePunctuation;
    boolean removeStopWords;

    TextProcessor(boolean removePunctuation, String regex, boolean doesStem,
                  boolean removeStopWords, SplitToken delimiter) {

        this.removePunctuation = removePunctuation;
        if(regex == null || regex.isEmpty()) {
            this.regex = DEFAULT_REGEX;
        } else {
            this.regex = regex;
        }
        if (doesStem) {
            this.stemmer = new Porter();
        }

        this.removeStopWords = removeStopWords;

        this.stopWords = fillSet("src/stopwords.txt");

        this.pos = fillSet("src/pos.txt");

        this.delimiter = delimiter;
    }

    // to read all stopwords in a set
    private Set fillSet(String filePath) {
        Set<String> words = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            while (line != null) {
                words.add(line.trim());
                line = br.readLine();
            }
        } catch (IOException ioe) {

        }
        return words;
    }

    class Token {
        String val;
        boolean isAdjacent;

        public Token(String token, boolean adjacent) {
            this.val = token;
            this.isAdjacent = adjacent;
        }
    }

    /**
     *
     * @param fileType
     * @param file
     * @return list of tokens in the file
     */


    public List<Token> process(String fileType, File file) {
        List<Token> listOfTokens = new ArrayList<>();
        FileIterator iterator = FileFactory.getInstance(fileType, file);
        while (iterator.hasNext()) {
            String line = iterator.next();
            listOfTokens.addAll(process(line));
        }

        return listOfTokens;
    }

    /**
     *
     * @param line
     * @return array of processed tokens in the line
     */
    public List<Token> process(String line) {
        String[] tokens = line.split(delimiter.getSplitToken());
        List<Token> processedTokens = new ArrayList<>();
        boolean adjacent = false;
        for (int i = 0; i < tokens.length; i++) {
            String[] split = tokens[i].split("_");

            if(!pos.contains(split[1])) {
                adjacent = false;
                continue;
            }

            String token = split[0];
            if (removePunctuation) {
                token = token.replaceAll(regex, "");
            }

            //making everything lowercase
            token = token.toLowerCase().trim();

            if (!token.isEmpty()) {

                if (removeStopWords && stopWords.contains(token)) {
                    adjacent = false;
                    continue;
                }
                if (stemmer != null) {
                    token = stemmer.stripAffixes(token);
                }

                if (removeStopWords && stopWords.contains(token)) {
                    adjacent = false;
                    continue;
                }
                if(token.isEmpty()){
                    adjacent=false;
                    continue;
                }
                processedTokens.add(new Token(token, adjacent));
                adjacent = true;
            }


        }
        return processedTokens;
    }

}
