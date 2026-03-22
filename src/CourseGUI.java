import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CourseGUI {
    private JPanel CoursePanel;
    private JPanel sidePanel;
    private JButton studentButton, enrollmentButton, coursesButton;
    private JTextField txtCourseName, txtCourseCode, txtCredits;
    private JSpinner spinUnits;
    private JTable table1;
    private JButton btnAdd, btnDelete, btnUpdate, btnClear;

    public CourseGUI() {
        JFrame frame = new JFrame("Course Management");
        frame.setContentPane(CoursePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setupListeners(frame);
        loadCourseData();
    }

    private void setupListeners(JFrame frame) {
        // --- Navigation ---
        studentButton.addActionListener(e -> { new StudentGUI(); frame.dispose(); });
        enrollmentButton.addActionListener(e -> { new EnrollmentGUI(); frame.dispose(); });

        // --- Action Buttons ---
        btnAdd.addActionListener(e -> saveCourse());
        btnClear.addActionListener(e -> clearFields());
        btnDelete.addActionListener(e -> deleteCourse());
        btnUpdate.addActionListener(e -> updateCourse());

        // --- Table Selection ---
        table1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table1.getSelectedRow();
                if (row != -1) {
                    txtCourseName.setText(table1.getValueAt(row, 1).toString());
                    txtCourseCode.setText(table1.getValueAt(row, 2).toString());
                    String credits = table1.getValueAt(row, 3).toString();
                    if (txtCredits != null) txtCredits.setText(credits);
                    if (spinUnits != null) spinUnits.setValue(Integer.parseInt(credits));
                }
            }
        });
    }

    private void clearFields() {
        txtCourseName.setText("");
        txtCourseCode.setText("");
        if (txtCredits != null) txtCredits.setText("");
        if (spinUnits != null) spinUnits.setValue(0);
        table1.clearSelection();
    }

    private String getCredits() {
        if (spinUnits != null) return spinUnits.getValue().toString();
        return txtCredits != null ? txtCredits.getText() : "";
    }

    private void saveCourse() {
        String name = txtCourseName.getText();
        String code = txtCourseCode.getText();
        String creditsText = getCredits();

        if (name.isEmpty() || code.isEmpty() || creditsText.isEmpty()) {
            JOptionPane.showMessageDialog(CoursePanel, "Please fill in all fields!");
            return;
        }

        String sql = "INSERT INTO courses (course_name, course_code, credits) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, code);
            pstmt.setInt(3, Integer.parseInt(creditsText));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(CoursePanel, "Course Saved Successfully!");
            clearFields();
            loadCourseData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(CoursePanel, "Error: " + ex.getMessage());
        }
    }

    private void updateCourse() {
        int row = table1.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(CoursePanel, "Select a course to update!");
            return;
        }

        int id = (int) table1.getValueAt(row, 0);
        String sql = "UPDATE courses SET course_name=?, course_code=?, credits=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtCourseName.getText());
            pstmt.setString(2, txtCourseCode.getText());
            pstmt.setInt(3, Integer.parseInt(getCredits()));
            pstmt.setInt(4, id);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(CoursePanel, "Course Updated!");
            clearFields();
            loadCourseData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(CoursePanel, "Error: " + ex.getMessage());
        }
    }

    private void deleteCourse() {
        int row = table1.getSelectedRow();
        if (row == -1) return;

        int id = (int) table1.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(CoursePanel, "Delete this course?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM courses WHERE id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadCourseData();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(CoursePanel, "Error: " + ex.getMessage());
            }
        }
    }

    private void loadCourseData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM courses")) {

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Course Name", "Course Code", "Credits"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("course_name"), rs.getString("course_code"), rs.getInt("credits")});
            }
            table1.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(CoursePanel, "Load Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new CourseGUI();
    }
}