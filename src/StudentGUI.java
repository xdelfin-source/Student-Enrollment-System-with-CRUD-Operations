import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.sql.*;

public class StudentGUI {
    private JPanel mainPanel;
    private JPanel sidePanel;
    private JButton studentButton, enrollmentButton, coursesButton;
    private JTextField txtFirstName, txtLastName, txtEmail, txtAge;
    private JButton btnSave;
    private JButton btnUpdate, btnDelete, btnClear;
    private JTable tableStudents; // Ensure this is named tableStudents in your .form

    public StudentGUI() {
        JFrame frame = new JFrame("Student Management System");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        setupListeners(frame);
        loadStudentData();
    }

    private void setupListeners(JFrame frame) {
        // --- Navigation ---
        coursesButton.addActionListener(e -> { new CourseGUI(); frame.dispose(); });
        enrollmentButton.addActionListener(e -> { new EnrollmentGUI(); frame.dispose(); });

        // --- Action Buttons ---
        btnSave.addActionListener(e -> saveStudent());
        if (btnUpdate != null) btnUpdate.addActionListener(e -> updateStudent());
        if (btnDelete != null) btnDelete.addActionListener(e -> deleteStudent());
        if (btnClear != null) btnClear.addActionListener(e -> clearFields());

        // --- Table Clicker ---
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

    private void loadStudentData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM students")) {

            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Email", "Age"}, 0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
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
        if (txtFirstName.getText().isEmpty() || txtLastName.getText().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Name fields cannot be empty!");
            return;
        }

        String sql = "INSERT INTO students (first_name, last_name, email, age) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtFirstName.getText());
            pstmt.setString(2, txtLastName.getText());
            pstmt.setString(3, txtEmail.getText());
            pstmt.setInt(4, Integer.parseInt(txtAge.getText()));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(mainPanel, "Student Saved!");
            clearFields();
            loadStudentData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "Error: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        int row = tableStudents.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableStudents.getValueAt(row, 0);
        String sql = "UPDATE students SET first_name=?, last_name=?, email=?, age=? WHERE id=?";

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
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {
        int row = tableStudents.getSelectedRow();
        if (row == -1) return;

        int id = (int) tableStudents.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(mainPanel, "Delete student?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM students WHERE id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadStudentData();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainPanel, "Database Error: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        txtFirstName.setText("");
        txtLastName.setText("");
        txtEmail.setText("");
        txtAge.setText("");
        tableStudents.clearSelection();
    }

    public static void main(String[] args) {
        new StudentGUI();
    }
}