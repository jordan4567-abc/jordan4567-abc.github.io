import java.util.*;
import java.util.stream.Collectors;

public class NewcomerStrategy implements Strategy {
    @Override
    public Student selectStudent(List<Student> students, List<Student> calledStudents, int unansweredCount) {
        List<Student> newcomers = students.stream()
                .filter(s -> s.getCallCount() == 0)
                .collect(Collectors.toList());
        if (!newcomers.isEmpty()) {
            return newcomers.get(new Random().nextInt(newcomers.size()));
        }
        return students.stream()
                .sorted(Comparator.comparingInt(Student::getCallCount))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("无可用学生"));
    }
}