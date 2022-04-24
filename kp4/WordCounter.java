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
    public HashMap<Document, Set<String>> documentWords = new HashMap<>();

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
            Set<String> commonWords = wordCounter.calcCommonWords();
            stopTime = System.currentTimeMillis();
            singleThreadTimes[i] = (stopTime - startTime);
            System.out.println("single thread search took " + singleThreadTimes[i] + "ms");
            System.out.println(commonWords);
            System.out.println("Books have " + commonWords.size() + " words in common");
        }
        
        for (int i = 0; i < repeatCount; i++) {
            WordCounter wordCounter = new WordCounter();
            startTime = System.currentTimeMillis();
            wordCounter.countOccurrencesInParallel(folder);
            Set<String> commonWords = wordCounter.calcCommonWords();
            stopTime = System.currentTimeMillis();
            forkedThreadTimes[i] = (stopTime - startTime);
            System.out.println("fork / join search took " + forkedThreadTimes[i] + "ms");
            System.out.println(commonWords);
            System.out.println("Books have " + commonWords.size() + " words in common");
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