import java.io.File;
import java.util.*;

/**
 * author: Saba Kathawala (650408125)
 * date: September 18, 2018
 *
 * Input: documents that need to be indexed for tokens (TextProcessor)
 * Output: An inverted index for fast access
 */


public class Indexer {

    private final String path;
    private String fileType;
    TextProcessor textProcessor;

    class TokenInfo {
        int df; //document frequency
        //double idf; // inverse document frequency
        Map<Integer, DocumentInfo> docToTermFrequencyMap; //maps document id to DocumentInfo

        public TokenInfo(int df, Map<Integer, DocumentInfo> docToTermFrequencyMap) {
            this.df = df;
            //this.idf = Math.log(1330.0/df);
            this.docToTermFrequencyMap = docToTermFrequencyMap;
        }
    }

    class DocumentInfo {
        int tf; //term frequency
        String name;    //document name
        int maxTermFreq;    //max term frequency in the document for normalization

        public DocumentInfo(int tf, String name) {
            this.tf = tf;
            this.name = name;
            this.maxTermFreq = 1;
        }
    }

    //maps tokens to TokenInfo
    private Map<String, TokenInfo> invertedIndex;

    public Map<String, TokenInfo> getInvertedIndex() {
        return invertedIndex;
    }

    public Map<Integer, Double> getDocumentLength() {
        return documentLength;
    }

    //maps document id to its length
    private Map<Integer, Double> documentLength;

    private List<File> listOfFiles;

    Indexer(TextProcessor textProcessor, File file, String fileType, String path) {
        this.textProcessor = textProcessor;
        this.listOfFiles = new ArrayList<>();
        Utilities.readFiles(file, listOfFiles);
        invertedIndex = new HashMap<>();
        this.fileType = fileType;
        this.path = path;
        fillTokens();
        this.documentLength = new HashMap<>();


        //findDocumentLengths();
    }

    public void add(String token, int docId, String docName) {

        // if token already present
        if (invertedIndex.containsKey(token)) {
            TokenInfo tInfo = invertedIndex.get(token);

            // if document already present
            if (tInfo.docToTermFrequencyMap.containsKey(docId)) {
                DocumentInfo dInfo = tInfo.docToTermFrequencyMap.get(docId);

                // increase term frequency of token
                dInfo.tf++;

                // update term with max frequency
                if(dInfo.tf > dInfo.maxTermFreq) {
                    dInfo.maxTermFreq = dInfo.tf;
                }
            }
            // else create DocumentInfo object and add to map
            else {
                DocumentInfo dInfo = new DocumentInfo(1, docName);
                tInfo.docToTermFrequencyMap.put(docId, dInfo);
                tInfo.df++;
                //tInfo.idf = Math.log(1330.0/tInfo.df)/Math.log(2);
            }
        }
        // create create document map and add document info
        // create token Info and add map to it
        // add token to inverted index
        else {
            Map<Integer, DocumentInfo> docToTermFrequencyMap = new HashMap<>();
            DocumentInfo dInfo = new DocumentInfo(1, docName);
            docToTermFrequencyMap.put(docId, dInfo);
            TokenInfo tInfo = new TokenInfo(1, docToTermFrequencyMap);
            //tInfo.idf = Math.log(1330.0)/Math.log(2);
            invertedIndex.put(token, tInfo);
        }
    }

    public void fillTokens() {
        for(File file: listOfFiles) {
            String name = file.getName();
            File goldFile = new File(path + "/gold/" + name);
            if (goldFile.exists()) {
                List<TextProcessor.Token> tokens = textProcessor.process(fileType, file);

                int docId = Integer.parseInt(file.getName());
                for (TextProcessor.Token token : tokens) {
                    add(token.val, docId, name);
                }
            }
        }
    }
}
