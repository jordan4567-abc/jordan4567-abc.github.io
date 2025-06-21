
import java.io.*;
import java.util.ArrayList;

public class DataManager {
    private static final String DATA_FILE = "class_data.dat";
    private static final String HISTORY_FILE = "class_history.dat";

    public static ArrayList<Student> students = new ArrayList<>();
    public static ArrayList<ClassSession> classHistory = new ArrayList<>();

    public static void loadInitialData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            students = (ArrayList<Student>) ois.readObject();
        } catch (Exception e) {
            students.add(new Student("张晓健"));
            students.add(new Student("周爱超"));
            students.add(new Student("李咨龙"));
            students.add(new Student("田心智"));
            students.add(new Student("李忱啸"));
            students.add(new Student("孙丰钰"));
            students.add(new Student("崔红昊"));
            students.add(new Student("秦飞宇"));
            students.add(new Student("任耀威"));
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HISTORY_FILE))) {
            classHistory = (ArrayList<ClassSession>) ois.readObject();
        } catch (Exception e) {
            classHistory = new ArrayList<>();
        }
    }

    public static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(students);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HISTORY_FILE))) {
            oos.writeObject(classHistory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isStudentExists(String name) {
        return students.stream().anyMatch(s -> s.getName().equals(name));
    }
}