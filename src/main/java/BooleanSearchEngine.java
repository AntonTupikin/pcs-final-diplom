import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> index = new HashMap<>();//результирующая мапа
    private String stopList;

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        File folder = new File(pdfsDir.toURI());
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {

                var doc = new PdfDocument(new PdfReader(file));
                int pageNumbers = doc.getNumberOfPages();
                for (int i = 1; i <= pageNumbers; i++) {
                    var page = doc.getPage(i);
                    var text = PdfTextExtractor.getTextFromPage(page);
                    var words = text.split("\\P{IsAlphabetic}+");
                    Map<String, Integer> freqs = new HashMap<>(); // мапа, где ключом будет слово, а значением - частота
                    for (var word : words) { // перебираем слова
                        if (word.isEmpty()) {
                            continue;
                        }
                        word = word.toLowerCase();
                        freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                    }
                    for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                        String word = entry.getKey();
                        PageEntry pageEntry = new PageEntry(file.getName(), i, entry.getValue());
                        if (index.containsKey(word)) {
                            List<PageEntry> list = index.get(word);
                            list.add(pageEntry);
                            Collections.sort(list);
                            index.put(word, list);
                        } else {
                            List<PageEntry> list = new ArrayList<>();
                            list.add(pageEntry);
                            Collections.sort(list);
                            index.put(word, list);
                        }
                    }
                }
            }
        }
    }

    @Override
    public List<PageEntry> search(String word) throws IOException {
        List<String> words = List.of(word.split("\\P{IsAlphabetic}+"));
        if (words.size() == 1) {
            return wordSearch(word);
        } else {
            return phraseSearch(words);
        }
    }

    public List<PageEntry> wordSearch(String word) {
        return index.get(word);
    }

    public List<PageEntry> phraseSearch(List<String> words) throws IOException {
        Path file = Paths.get("stop-ru.txt");
        List<String> stopWords = Files.readAllLines(file);
        List<PageEntry> list = new ArrayList<>();
        for (String word : words) {
            if (!stopWords.contains(word)) {
                list.addAll(wordSearch(word));
            }
        }
        Collections.sort(list);
        //здесь должен быть перебор списка лист с объединением полей count у объектов, у который
        //совпадает page и pdfName, но я не знаю, как его сделать
        return list;
    }
}
