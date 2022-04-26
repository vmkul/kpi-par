/* ......................................................................................... */

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/* ......................................................................................... */

class Document {
    private final List<String> lines;
    private final String name;
    
    Document(String name, List<String> lines) {
        this.lines = lines;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    List<String> getLines() {
        return this.lines;
    }
    
    static Document fromFile(File file) throws IOException {
        List<String> lines = new LinkedList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        }
        return new Document(file.getName(), lines);
    }
}

/* ......................................................................................... */

class Folder {
    private final List<Folder> subFolders;
    private final List<Document> documents;
    
    Folder(List<Folder> subFolders, List<Document> documents) {
        this.subFolders = subFolders;
        this.documents = documents;
    }
    
    List<Folder> getSubFolders() {
        return this.subFolders;
    }
    
    List<Document> getDocuments() {
        return this.documents;
    }
    
    static Folder fromDirectory(File dir) throws IOException {
        List<Document> documents = new LinkedList<>();
        List<Folder> subFolders = new LinkedList<>();
        for (File entry : dir.listFiles()) {
            if (entry.isDirectory()) {
                subFolders.add(Folder.fromDirectory(entry));
            } else {
                documents.add(Document.fromFile(entry));
            }
        }
        return new Folder(subFolders, documents);
    }
}

/* ......................................................................................... */

public class WordCounter {
    public HashMap<Document, Set<String>> documentWords = new HashMap<>();
    private Set<String> ITterms = new HashSet<>(Arrays.asList(
   "data",
        "internet",
        "computer",
        "software",
        "database",
        "electronics",
        "information",
        "it",
        "engineering",
        "system",
        "informatics",
        "network",
        "computing",
        "communication",
        "hardware",
        "architecture",
        "intranet",
        "cyber",
        "infostructure",
        "cybernetics",
        "cyberspace",
        "intel",
        "peripheral",
        "source",
        "update",
        "path",
        "automation",
        "robotics",
        "digital",
        "library",
        "web",
        "class",
        "method",
        "constructor",
        "string",
        "integer",
        "object",
        "identifier",
        "prototype",
        "compiler"
   ));

/* ......................................................................................... */

    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }
    
    void traverseDocument(Document document) {
        HashSet<String> words = new HashSet<>();

        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                words.add(word);
            }
        }

        synchronized (documentWords) {
            documentWords.put(document, words);
        }
    }
    
/* ......................................................................................... */
    
    void traverseDocumentOnSingleThread(Folder folder) {
        for (Folder subFolder : folder.getSubFolders()) {
            traverseDocumentOnSingleThread(subFolder);
        }
        for (Document document : folder.getDocuments()) {
            traverseDocument(document);
        }
    }

    Set<String> calcCommonWords() {
        Iterator<Set<String>> vals = documentWords.values().iterator();
        if (!vals.hasNext()) return new HashSet<>();
        Set<String> res = vals.next();

        while (vals.hasNext()) {
            res.retainAll(vals.next());
        }

        return res;
    }

    int calcPercentage(Set<String> words) {
        int commonWords = 0;
        for (String word : words) {
            if (ITterms.contains(word)) {
                commonWords++;
            }
        }

        return (int)(((double) commonWords / ITterms.size()) * 100);
    }

    Map<String, Integer> calcPercentages() {
        HashMap<String, Integer> res = new HashMap<>();

        for (Map.Entry<Document, Set<String>> entry : documentWords.entrySet()) {
            res.put(entry.getKey().getName(), calcPercentage(entry.getValue()));
        }

        return res;
    }

/* ......................................................................................... */

    class DocumentSearchTask extends RecursiveAction {
        private final Document document;
        
        DocumentSearchTask(Document document) {
            super();
            this.document = document;
        }
        
        @Override
        protected void compute() {
            traverseDocument(document);
        }
    }

/* ......................................................................................... */

    class FolderSearchTask extends RecursiveAction {
        private final Folder folder;
        
        FolderSearchTask(Folder folder) {
            super();
            this.folder = folder;
        }
        
        @Override
        protected void compute() {
            List<RecursiveAction> forks = new LinkedList<>();
            for (Folder subFolder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(subFolder);
                forks.add(task);
                task.fork();
            }
            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task = new DocumentSearchTask(document);
                forks.add(task);
                task.fork();
            }
            for (RecursiveAction task : forks) {
                task.join();
            }
        }
    }
        
/* ......................................................................................... */
    
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    
    void countOccurrencesInParallel(Folder folder) {
        forkJoinPool.invoke(new FolderSearchTask(folder));
    }

/* ......................................................................................... */

    public static void main(String[] args) throws IOException {
        Folder folder = Folder.fromDirectory(new File(args[0]));
        
        final int repeatCount = Integer.decode(args[1]);
        long startTime;
        long stopTime;
        
        long[] singleThreadTimes = new long[repeatCount];
        long[] forkedThreadTimes = new long[repeatCount];
        
        for (int i = 0; i < repeatCount; i++) {
            WordCounter wordCounter = new WordCounter();
            startTime = System.currentTimeMillis();
            wordCounter.traverseDocumentOnSingleThread(folder);
            Map<String, Integer> percentages = wordCounter.calcPercentages();
            stopTime = System.currentTimeMillis();
            singleThreadTimes[i] = (stopTime - startTime);
            System.out.println("single thread search took " + singleThreadTimes[i] + "ms");
            System.out.println("IT book percentage");
            for (Map.Entry<String, Integer> entry : percentages.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue() + "%");
            }
        }
        
        for (int i = 0; i < repeatCount; i++) {
            WordCounter wordCounter = new WordCounter();
            startTime = System.currentTimeMillis();
            wordCounter.countOccurrencesInParallel(folder);
            Map<String, Integer> percentages = wordCounter.calcPercentages();
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
            System.out.println("fork / join search took " + forkedThreadTimes[i] + "ms");
            System.out.println("IT book percentage");
            for (Map.Entry<String, Integer> entry : percentages.entrySet()) {
                System.out.println(entry.getKey() + " => " + entry.getValue() + "%");
            }
        }
        
        System.out.println("\nCSV Output:\n");
        System.out.println("Single thread,Fork/Join");        
        for (int i = 0; i < repeatCount; i++) {
            System.out.println(singleThreadTimes[i] + "," + forkedThreadTimes[i]);
        }
        System.out.println();
    }
}

/* ......................................................................................... */