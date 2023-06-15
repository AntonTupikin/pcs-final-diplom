import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
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
        System.out.println("запустил поиск по фразе");
        Path file = Paths.get("stop-ru.txt");
        List<String> stopWords = Files.readAllLines(file);
        List<PageEntry> list = new ArrayList<>();
        for (String word : words) {
            if (!stopWords.contains(word)) {
                list.addAll(wordSearch(word));
            }
        }

        List<PageEntry> result = listComby(list);
        System.out.println("запустил перебор результатов");
        Collections.sort(result);
        return result;
    }

    public List<PageEntry> listComby(List<PageEntry> list) {
        List<PageEntry> result = list;
        for (PageEntry entry1 : result) {
            for (PageEntry entry2 : result) {
                if (entry1.somePage(entry2)) {
                    entry1.setCount(entry2.getCount() + entry1.getCount());
                    result.remove(entry2);
                }

            }
        }
        return result;
    }
}
