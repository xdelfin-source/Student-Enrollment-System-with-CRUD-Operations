import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // 1. ADD COURSE (Create)
    public void addCourse(Course course) {
        String sql = "INSERT INTO Course (course_name, course_description, credits) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getCourseDescription());
            pstmt.setInt(3, course.getCredits());

            pstmt.executeUpdate();
            System.out.println("Course added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding course.");
            e.printStackTrace();
        }
    }

    // 2. VIEW ALL COURSES (Read)
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM enrollmentsystem.course";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Course course = new Course(
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_description"),
                        rs.getInt("credits")
                );
                courses.add(course);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching courses.");
            e.printStackTrace();
        }
        return courses;
    }

    // 3. UPDATE COURSE (Update)
    public void updateCourse(Course course) {
        String sql = "UPDATE Course SET course_name=?, course_description=?, credits=? WHERE course_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getCourseDescription());
            pstmt.setInt(3, course.getCredits());
            pstmt.setInt(4, course.getCourseId());

            pstmt.executeUpdate();
            System.out.println("Course updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating course.");
            e.printStackTrace();
        }
    }

    // 4. DELETE COURSE (Delete)
    public void deleteCourse(int courseId) {
        String sql = "DELETE FROM Course WHERE course_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);
            pstmt.executeUpdate();
            System.out.println("Course deleted successfully!");

        } catch (SQLException e) {
            System.out.println("Error deleting course.");
            e.printStackTrace();
        }
    }
}