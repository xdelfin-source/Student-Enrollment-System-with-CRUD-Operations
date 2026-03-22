import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.sql.*;
import java.util.HashMap;

public class EnrollmentGUI {
    private JPanel EnrollmentPanel;
    private JComboBox<String> comboStudent, comboCourse;
    private JTable tableEnrollment;
    private JButton studentButton, enrollmentButton, coursesButton;
    private JButton btnEnroll, btnUpdateEnrollment, btnDeleteEnrollment, btnClearEnrollment;
    private JPanel sidePanel;

    private HashMap<String, Integer> studentMap = new HashMap<>();
    private HashMap<String, Integer> courseMap = new HashMap<>();

    public EnrollmentGUI() {
        JFrame frame = new JFrame("Enrollment Management");
        frame.setContentPane(EnrollmentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setupListeners(frame);
        refreshData();
    }

    private void setupListeners(JFrame frame) {
        // --- Navigation ---
        studentButton.addActionListener(e -> { new StudentGUI(); frame.dispose(); });
        coursesButton.addActionListener(e -> { new CourseGUI(); frame.dispose(); });

        // --- Action Buttons ---
        btnEnroll.addActionListener(e -> enrollStudent());
        if (btnUpdateEnrollment != null) btnUpdateEnrollment.addActionListener(e -> updateEnrollment());
        if (btnDeleteEnrollment != null) btnDeleteEnrollment.addActionListener(e -> deleteEnrollment());
        if (btnClearEnrollment != null) btnClearEnrollment.addActionListener(e -> clearFields());

        // --- Table Selection ---
        tableEnrollment.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tableEnrollment.getSelectedRow();
                if (row != -1) {
                    comboStudent.setSelectedItem(tableEnrollment.getValueAt(row, 1).toString());
                    comboCourse.setSelectedItem(tableEnrollment.getValueAt(row, 2).toString());
                }
            }
        });
    }

    private void refreshData() {
        loadDropdowns();
        loadEnrollmentData();
    }

    private void loadDropdowns() {
        studentMap.clear();
        courseMap.clear();
        comboStudent.removeAllItems();
        comboCourse.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load Students
            ResultSet rsS = conn.createStatement().executeQuery("SELECT id, CONCAT(first_name, ' ', last_name) AS full_name FROM students");
            while (rsS.next()) {
                String name = rsS.getString("full_name");
                comboStudent.addItem(name);
                studentMap.put(name, rsS.getInt("id"));
            }

            // Load Courses
            ResultSet rsC = conn.createStatement().executeQuery("SELECT id, course_name FROM courses");
            while (rsC.next()) {
                String title = rsC.getString("course_name");
                comboCourse.addItem(title);
                courseMap.put(title, rsC.getInt("id"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void enrollStudent() {
        String sName = (String) comboStudent.getSelectedItem();
        String cName = (String) comboCourse.getSelectedItem();

        if (sName == null || cName == null) return;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO enrollments (student_id, course_id, enrollment_date) VALUES (?, ?, CURDATE())")) {

            pstmt.setInt(1, studentMap.get(sName));
            pstmt.setInt(2, courseMap.get(cName));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(EnrollmentPanel, "Student Enrolled!");
            loadEnrollmentData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Error: " + ex.getMessage());
        }
    }

    private void updateEnrollment() {
        int row = tableEnrollment.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Select an enrollment to update.");
            return;
        }

        int id = (int) tableEnrollment.getValueAt(row, 0);
        String sName = (String) comboStudent.getSelectedItem();
        String cName = (String) comboCourse.getSelectedItem();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("UPDATE enrollments SET student_id = ?, course_id = ? WHERE id = ?")) {

            pstmt.setInt(1, studentMap.get(sName));
            pstmt.setInt(2, courseMap.get(cName));
            pstmt.setInt(3, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(EnrollmentPanel, "Enrollment Updated!");
            loadEnrollmentData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Update Error: " + ex.getMessage());
        }
    }

    private void deleteEnrollment() {
        int row = tableEnrollment.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableEnrollment.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(EnrollmentPanel, "Remove this enrollment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM enrollments WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadEnrollmentData();
                clearFields();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearFields() {
        if (comboStudent.getItemCount() > 0) comboStudent.setSelectedIndex(0);
        if (comboCourse.getItemCount() > 0) comboCourse.setSelectedIndex(0);
        tableEnrollment.clearSelection();
    }

    private void loadEnrollmentData() {
        String sql = "SELECT e.id, CONCAT(s.first_name, ' ', s.last_name) AS student_name, " +
                "c.course_name, e.enrollment_date " +
                "FROM enrollments e " +
                "JOIN students s ON e.student_id = s.id " +
                "JOIN courses c ON e.course_id = c.id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Student Name", "Course", "Date"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("student_name"), rs.getString("course_name"), rs.getDate("enrollment_date")});
            }
            tableEnrollment.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Load Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new EnrollmentGUI();
    }
}