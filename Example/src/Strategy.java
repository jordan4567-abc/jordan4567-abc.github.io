import java.util.List;

public interface Strategy {
    Student selectStudent(List<Student> students, List<Student> calledStudents, int unansweredCount);
}