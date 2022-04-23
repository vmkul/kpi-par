/* ......................................................................................... */

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/* ......................................................................................... */

class Document {
    private final List<String> lines;
    
    Document(List<String> lines) {
        this.lines = lines;
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
        return new Document(lines);
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
    public HashMap<Integer, Integer> wordCount = new HashMap<>();

    private void saveWordLength(String word) {
        int len = word.length();
        if (len == 0) return;
        synchronized (wordCount) { // HashMap is not thread-safe
            wordCount.put(Integer.valueOf(len), wordCount.getOrDefault(Integer.valueOf(len), 0) + 1);
        }
    }

/* ......................................................................................... */

    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }
    
    void traverseDocument(Document document) {
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                saveWordLength(word);

            }
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

    private double calcAverage() {
        int sum = 0;
        int count = 0;

        for (Map.Entry<Integer, Integer> entry : wordCount.entrySet()) {
            sum += entry.getKey() * entry.getValue();
            count += entry.getValue();
        }

        return (double) sum / count;
    }

    private double calcStd() {
        double sum = 0;
        double count = 0;
        double mean = calcAverage();

        for (Map.Entry<Integer, Integer> entry : wordCount.entrySet()) {
            double diff = Math.pow(entry.getKey() - mean, 2);
            count += entry.getValue();
            sum += diff * entry.getValue();
        }

        return sum / count;
    }

    private void printWordCounts() {
        for (Map.Entry<Integer, Integer> entry : wordCount.entrySet()) {
            System.out.println(String.format("Word length %d => %d", entry.getKey(), entry.getValue()));
        }
    }
    
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
            stopTime = System.currentTimeMillis();
            singleThreadTimes[i] = (stopTime - startTime);
            System.out.println("single thread search took " + singleThreadTimes[i] + "ms");
            wordCounter.printWordCounts();
            System.out.println("Average value: " + wordCounter.calcAverage());
            System.out.println("Standard deviation: " + wordCounter.calcStd());
        }
        
        for (int i = 0; i < repeatCount; i++) {
            WordCounter wordCounter = new WordCounter();
            startTime = System.currentTimeMillis();
            wordCounter.countOccurrencesInParallel(folder);
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
            System.out.println("fork / join search took " + forkedThreadTimes[i] + "ms");
            wordCounter.printWordCounts();
            System.out.println("Average value: " + wordCounter.calcAverage());
            System.out.println("Standard deviation: " + wordCounter.calcStd());
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