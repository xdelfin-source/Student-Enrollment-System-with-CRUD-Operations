import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.sql.*;

public class StudentGUI {
    private JPanel mainPanel, sidePanel;
    private JTextField txtFirstName, txtLastName, txtEmail, txtAge;
    private JButton btnSave, btnUpdate, btnDelete, btnClear;
    private JButton studentButton, enrollmentButton, coursesButton;
    private JTable tableStudents;

    public StudentGUI() {
        JFrame frame = new JFrame("Student Management System");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));

        setupListeners(frame);
        loadStudentData(); // Initial load
        if (btnDelete != null) btnDelete.addActionListener(e -> deleteStudent());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupListeners(JFrame frame) {
        // ==========================================
        // FIXED: Navigation Button Listeners Added
        // ==========================================
        if (coursesButton != null) {
            coursesButton.addActionListener(e -> {
                try {
                    new CourseGUI(); // Open Course window
                    frame.dispose(); // Close Student window
                } catch (Exception ex) {
                    ex.printStackTrace(); // Prints exact line of error in console
                    JOptionPane.showMessageDialog(mainPanel, "Crash opening Course window:\n" + ex.toString(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        if (enrollmentButton != null) {
            enrollmentButton.addActionListener(e -> {
                try {
                    new EnrollmentGUI(); // Open Enrollment window
                    frame.dispose(); // Close Student window
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainPanel, "Crash opening Enrollment window:\n" + ex.toString(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        // ==========================================

        // Data action buttons
        if (btnSave != null) btnSave.addActionListener(e -> saveStudent());
        if (btnUpdate != null) btnUpdate.addActionListener(e -> updateStudent());
        if (btnDelete != null) btnDelete.addActionListener(e -> deleteStudent());
        if (btnClear != null) btnClear.addActionListener(e -> clearFields());

        if (tableStudents != null) {
            tableStudents.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int row = tableStudents.getSelectedRow();
                    if (row != -1) {
                        txtFirstName.setText(tableStudents.getValueAt(row, 1).toString());
                        txtLastName.setText(tableStudents.getValueAt(row, 2).toString());
                        txtEmail.setText(tableStudents.getValueAt(row, 3).toString());
                        txtAge.setText(tableStudents.getValueAt(row, 4).toString());
                    }
                }
            });
        }
    }

    public void loadStudentData() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Email", "Age"}, 0);

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            String sql = "SELECT * FROM student"; // Singular table name
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("student_id"), // Column name from your DB
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getInt("age")
                });
            }
            tableStudents.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void saveStudent() {
        if (txtFirstName.getText().isEmpty() || txtAge.getText().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Please fill required fields!");
            return;
        }

        String sql = "INSERT INTO student (first_name, last_name, email, age) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtFirstName.getText());
            pstmt.setString(2, txtLastName.getText());
            pstmt.setString(3, txtEmail.getText());
            pstmt.setInt(4, Integer.parseInt(txtAge.getText()));

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(mainPanel, "Student Saved!");
            loadStudentData();
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "Save Error: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        int row = tableStudents.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableStudents.getValueAt(row, 0);
        String sql = "UPDATE student SET first_name=?, last_name=?, email=?, age=? WHERE student_id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtFirstName.getText());
            pstmt.setString(2, txtLastName.getText());
            pstmt.setString(3, txtEmail.getText());
            pstmt.setInt(4, Integer.parseInt(txtAge.getText()));
            pstmt.setInt(5, id);

            pstmt.executeUpdate();
            loadStudentData();
            JOptionPane.showMessageDialog(mainPanel, "Student Updated!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "Update Error: " + ex.getMessage());
        }
    }

    private void deleteStudent() {
        int row = tableStudents.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableStudents.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(mainPanel, "Delete this student?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM student WHERE student_id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadStudentData();
                clearFields();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void clearFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtAge.setText("");
        if (tableStudents != null) tableStudents.clearSelection();
    }

    public static void main(String[] args) {
        new StudentGUI();
    }
}