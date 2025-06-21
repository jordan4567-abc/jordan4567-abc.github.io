
import java.util.*;
import java.util.stream.Collectors;

public class CorrectRateStrategy implements Strategy {
    // 连续回答错误的阈值
    private static final int MAX_UNANSWERED = 3;

    @Override
    public Student selectStudent(List<Student> students, List<Student> calledStudents, int unansweredCount) {
        // 如果连续未回答次数超过阈值，尝试选择正确率高的学生
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

        // 正常情况：选择正确率较低的学生
        List<Student> filtered = students.stream()
                .filter(s -> s.getCallCount() > 0)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new RandomStrategy().selectStudent(students, calledStudents, unansweredCount);
        }

        return filtered.stream()
                .sorted(Comparator.comparingDouble(Student::getCorrectRate))
                .limit(5)
                .collect(Collectors.toList())
                .get(new Random().nextInt(Math.min(5, filtered.size())));
    }
}