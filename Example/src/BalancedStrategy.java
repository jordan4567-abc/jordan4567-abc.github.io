import java.util.*;

public class BalancedStrategy implements Strategy {
    @Override
    public Student selectStudent(List<Student> students, List<Student> calledStudents, int unansweredCount) {
        return students.stream()
                .sorted(Comparator.comparingDouble(s ->
                        s.getCallCount() * 0.7 + (1 - s.getCorrectRate()) * 0.3))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("无可用学生"));
    }
}