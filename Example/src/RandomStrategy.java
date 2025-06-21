import java.util.*;
import java.util.stream.Collectors;

public class RandomStrategy implements Strategy {
    private static final int MAX_UNANSWERED = 3;

    @Override
    public Student selectStudent(List<Student> students, List<Student> calledStudents, int unansweredCount) {
        if (unansweredCount >= MAX_UNANSWERED) {
            List<Student> highCorrect = students.stream()
                    .filter(s -> s.getCallCount() > 0)
                    .sorted(Comparator.comparingDouble(Student::getCorrectRate).reversed())
                    .limit(3)
                    .collect(Collectors.toList());
            if (!highCorrect.isEmpty()) {
                return highCorrect.get(new Random().nextInt(highCorrect.size()));
            }
        }

        List<Student> remaining = students.stream()
                .filter(s -> !calledStudents.contains(s))
                .collect(Collectors.toList());
        if (remaining.isEmpty()) {
            remaining = new ArrayList<>(students);
        }

        remaining.sort(Comparator.comparingInt(Student::getCallCount));
        int minCall = remaining.get(0).getCallCount();
        List<Student> minCalled = remaining.stream()
                .filter(s -> s.getCallCount() == minCall)
                .collect(Collectors.toList());
        return minCalled.get(new Random().nextInt(minCalled.size()));
    }
}