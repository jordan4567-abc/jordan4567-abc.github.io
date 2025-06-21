import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    int callCount, correctCount, consecutiveCorrect;
    Map<Integer, Boolean> sessionHistory = new HashMap<>();

    public Student(String name) { this.name = name; }

    public String getName() { return name; }
    public int getCallCount() { return callCount; }
    public int getCorrectCount() { return correctCount; }
    public double getCorrectRate() { return callCount > 0 ? (double) correctCount / callCount : 0; }
    public void incrementCallCount() { callCount++; }
    public void incrementCorrectCount() {
        correctCount++;
        consecutiveCorrect++;
        if (consecutiveCorrect >= 3) { correctCount++; consecutiveCorrect = 0; }
    }
    public void resetConsecutive() { consecutiveCorrect = 0; }
    public void recordSession(int sessionId, boolean correct) { sessionHistory.put(sessionId, correct); }
    public int getSessionCount() { return sessionHistory.size(); }

    @Override
    public String toString() {
        return String.format("%s (点名:%d, 正确:%d, 正确率:%.1f%%, 连续正确:%d)",
                name, callCount, correctCount, getCorrectRate() * 100, consecutiveCorrect);
    }
}