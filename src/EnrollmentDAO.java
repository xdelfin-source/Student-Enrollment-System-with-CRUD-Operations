import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {

    // 1. ENROLL STUDENT (Create)
    public void enrollStudent(int studentId, int courseId) {
        String sql = "INSERT INTO Enrolled_Subject (student_id, course_id, enrollment_date) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);

            // Gets the current date automatically
            Date currentDate = new Date(System.currentTimeMillis());
            pstmt.setDate(3, currentDate);

            pstmt.executeUpdate();
            System.out.println("Student enrolled successfully!");

        } catch (SQLException e) {
            System.out.println("Error enrolling student.");
            e.printStackTrace();
        }
    }

    // 2. VIEW ALL ENROLLMENTS (Read with JOIN)
    // Returning a List of Object arrays makes it incredibly easy to add directly to a Swing JTable!
    public List<Object[]> getAllEnrollmentDetails() {
        List<Object[]> enrollments = new ArrayList<>();

        // This query joins the three tables together to get the actual names
        String sql = "SELECT e.enrollment_id, CONCAT(s.first_name, ' ', s.last_name) AS student_name, " +
                "c.course_name, e.enrollment_date " +
                "FROM Enrolled_Subject e " +
                "JOIN Student s ON e.student_id = s.student_id " +
                "JOIN Course c ON e.course_id = c.course_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Create an array holding exactly what your Swing table needs
                Object[] row = {
                        rs.getInt("enrollment_id"),
                        rs.getString("student_name"),
                        rs.getString("course_name"),
                        rs.getDate("enrollment_date")
                };
                enrollments.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching enrollment details.");
            e.printStackTrace();
        }
        return enrollments;
    }

    // 3. DELETE ENROLLMENT (Delete)
    public void deleteEnrollment(int enrollmentId) {
        String sql = "DELETE FROM Enrolled_Subject WHERE enrollment_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            pstmt.executeUpdate();
            System.out.println("Enrollment deleted successfully!");

        } catch (SQLException e) {
            System.out.println("Error deleting enrollment.");
            e.printStackTrace();
        }
    }
}