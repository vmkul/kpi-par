import java.util.Random;

class ProfThread extends Thread {
    private final Journal journal;
    private final int[][] gradeSum;
    
    public ProfThread(Journal journal) {
	this.journal = journal;
	gradeSum = new int[journal.groupCount()][journal.studentCount()];
    }
    
    @Override
    public void run(){
	Random r = new Random();
	for (int i = 0; i < 1000; i++) {
	    int group = r.nextInt(journal.groupCount());
	    int student = r.nextInt(journal.studentCount());
	    int grade = r.nextInt(101);

	    // System.out.printf("Prof %d puts grade %d to student %d of group %d%n",
	    // 		      getId(), grade, student, group);
	    gradeSum[group][student] += grade;
	    journal.putGrade(group, student, grade);
	}
    }

    public int getGradeSum(int group, int student) {
	return gradeSum[group][student];
    }
}

public class JournalTest {
    private static final int NUM_GROUPS = 3;
    private static final int NUM_STUDENTS = 30;
    private static final int NUM_PROF = 100;

    public static void main(String[] args) {
	Journal journal = new Journal(NUM_GROUPS, NUM_STUDENTS);
	ProfThread[] profThreads = new ProfThread[NUM_PROF];
	for (int i = 0; i < NUM_PROF; i++) {
	    profThreads[i] = new ProfThread(journal);
	}
	for (Thread t : profThreads) {
	    t.start();
	}
	for (Thread t : profThreads) {
	    try {
		t.join();
	    } catch (InterruptedException ex) {}
	}

	for (int group = 0; group < NUM_GROUPS; group++) {
	    for (int student = 0; student < NUM_STUDENTS; student++) {
		int profGradeSum = 0;
		int journalGradeSum = journal.getGradeSum(group, student);
		
		for (int prof = 0; prof < NUM_PROF; prof++) {
		    profGradeSum += profThreads[prof].getGradeSum(group, student);
		}

		if (profGradeSum != journalGradeSum) {
		    System.out.printf("Grade mismatch! %d != %d Student %d of group %d%n",
				      profGradeSum, journalGradeSum, student, group);
		}
	    }
	}
    }
}
