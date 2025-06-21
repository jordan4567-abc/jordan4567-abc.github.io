
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClassSession implements Serializable {
    private static final long serialVersionUID = 1L;
    int sessionId;
    Date date;
    int totalStudents, calledCount, correctCount;
    List<String> calledStudents = new ArrayList<>();

    public ClassSession(int sessionId, int totalStudents) {
        this.sessionId = sessionId;
        this.date = new Date();
        this.totalStudents = totalStudents;
    }

    public void incrementCalled(String studentName) {
        calledCount++;
        calledStudents.add(studentName);
    }

    public void incrementCorrect() { correctCount++; }

    public String getSummary() {
        return String.format("课堂ID: %d | 日期: %s | 学生总数: %d | 已点名: %d | 正确率: %.2f%%",
                sessionId, new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date),
                totalStudents, calledCount,
                calledCount > 0 ? (double) correctCount / calledCount * 100 : 0);
    }

    public String getDetails() {
        StringBuilder details = new StringBuilder();
        details.append("课堂ID: ").append(sessionId).append("\n");
        details.append("日期: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)).append("\n");
        details.append("学生总数: ").append(totalStudents).append("\n");
        details.append("已点名: ").append(calledCount).append(" 人\n");
        details.append("正确回答: ").append(correctCount).append(" 次\n");
        details.append("正确率: ").append(calledCount > 0 ?
                String.format("%.2f%%", correctCount * 100.0 / calledCount) : "0.0%").append("\n\n");
        details.append("点名记录: ").append("\n");

        for (int i = 0; i < calledStudents.size(); i++) {
            details.append(i + 1).append(". ").append(calledStudents.get(i)).append("\n");
        }
        return details.toString();
    }
}