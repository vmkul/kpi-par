import java.util.ArrayList;

class Group {
    public final int numStudents;
    public ArrayList<ArrayList<Integer>> grades = new ArrayList<ArrayList<Integer>>();

    public Group(int numStudents) {
	this.numStudents = numStudents;
	
	for (int i = 0; i < numStudents; i++) {
	    grades.add(new ArrayList<Integer>());
	}
    }

    public void putGrade(int student, int grade) {
	ArrayList<Integer> studentGrades = grades.get(student);

	synchronized(studentGrades) {
	    studentGrades.add(grade);	    
	}
    }

    public int getGradeSum(int student) {
	int res = 0;
	ArrayList<Integer> studentGrades = grades.get(student);
	
	for (int i = 0; i < studentGrades.size(); i++) {
	    res += studentGrades.get(i);
	}

	return res;
    }
}

public class Journal {
    public final int numGroups;
    public final int numStudents;
    private Group[] groups;

    public Journal(int numGroups, int numStudents) {
	this.numGroups = numGroups;
	this.numStudents = numStudents;
	groups = new Group[numGroups];
	for (int i = 0; i < numGroups; i++) {
	    groups[i] = new Group(numStudents);
	}
    }

    public void putGrade(int group, int student, int grade) {
	groups[group].putGrade(student, grade);
    }

    public int getGradeSum(int group, int student) {
	return groups[group].getGradeSum(student);
    }

    public int studentCount() {
	return numStudents;
    }

    public int groupCount() {
	return numGroups;
    }
}
