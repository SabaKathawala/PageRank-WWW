import java.io.File;

/**
 * author: Saba
 * date: September 19, 2018
 *
 * comment: Factory to return type of FileIterator
 */

public class FileFactory {

    public static FileIterator getInstance(String fileType, File file) {
        switch (fileType) {
            case "REGULAR" :
                return new FileIterator(file);

//            case "SGML":
//                return new SGMLFileIterator(file);

            default:
                return new FileIterator(file);
        }
    }
}
