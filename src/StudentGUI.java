import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.*;
import java.awt.Dimension;

public class StudentGUI {
    private JPanel mainPanel, sidePanel;
    private JTextField txtFirstName, txtLastName, txtEmail, txtAge;
    private JButton btnSave, btnUpdate, btnDelete, btnClear;
    private JButton studentButton, enrollmentButton, coursesButton;
    private JTable tableStudents;
    private JTextField textField1; // This acts as your Search Bar

    // 1. Declare the TableRowSorter
    private TableRowSorter<DefaultTableModel> rowSorter;

    public StudentGUI() {
        JFrame frame = new JFrame("Student Management System");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));

        setupListeners(frame);
        loadStudentData(); // Initial load

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupListeners(JFrame frame) {
        if (coursesButton != null) {
            coursesButton.addActionListener(e -> {
                try {
                    new CourseGUI();
                    frame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainPanel, "Crash opening Course window:\n" + ex.toString(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        if (enrollmentButton != null) {
            enrollmentButton.addActionListener(e -> {
                try {
                    new EnrollmentGUI();
                    frame.dispose();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(mainPanel, "Crash opening Enrollment window:\n" + ex.toString(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        // Data action buttons
        if (btnSave != null) btnSave.addActionListener(e -> saveStudent());
        if (btnUpdate != null) btnUpdate.addActionListener(e -> updateStudent());
        if (btnDelete != null) btnDelete.addActionListener(e -> deleteStudent());
        if (btnClear != null) btnClear.addActionListener(e -> clearFields());

        // Table Selection Listener
        if (tableStudents != null) {
            tableStudents.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int viewRow = tableStudents.getSelectedRow();
                    if (viewRow != -1) {
                        // IMPORTANT: Convert view index to model index when using a filter!
                        int modelRow = tableStudents.convertRowIndexToModel(viewRow);
                        DefaultTableModel model = (DefaultTableModel) tableStudents.getModel();

                        txtFirstName.setText(model.getValueAt(modelRow, 1).toString());
                        txtLastName.setText(model.getValueAt(modelRow, 2).toString());
                        txtEmail.setText(model.getValueAt(modelRow, 3).toString());
                        txtAge.setText(model.getValueAt(modelRow, 4).toString());
                    }
                }
            });
        }

        // 2. Add Listener to the Search Bar (textField1)
        if (textField1 != null) {
            textField1.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { applyFilter(); }
                @Override
                public void removeUpdate(DocumentEvent e) { applyFilter(); }
                @Override
                public void changedUpdate(DocumentEvent e) { applyFilter(); }
            });
        }
    }

    // 3. Helper method to apply the search filter
    private void applyFilter() {
        if (rowSorter == null) return;

        String text = textField1.getText();
        if (text.trim().length() == 0) {
            rowSorter.setRowFilter(null); // Show all if search is empty
        } else {
            // (?i) makes the search case-insensitive
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    public void loadStudentData() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "First Name", "Last Name", "Email", "Age"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Good practice: prevent users from double-clicking and typing directly in the table
            }
        };

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            String sql = "SELECT * FROM student";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("student_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getInt("age")
                });
            }

            tableStudents.setModel(model);

            // 4. Attach the sorter to the new model every time data is reloaded
            rowSorter = new TableRowSorter<>(model);
            tableStudents.setRowSorter(rowSorter);

            // Re-apply filter in case the user typed something before saving/updating
            applyFilter();

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
            loadStudentData(); // This will auto-update the table and keep the search active
            clearFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainPanel, "Save Error: " + ex.getMessage());
        }
    }

    private void updateStudent() {
        int viewRow = tableStudents.getSelectedRow();
        if (viewRow == -1) return;

        // Convert view index to model index to grab the correct ID
        int modelRow = tableStudents.convertRowIndexToModel(viewRow);
        int id = (int) tableStudents.getModel().getValueAt(modelRow, 0);

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
        int viewRow = tableStudents.getSelectedRow();
        if (viewRow == -1) return;

        // Convert view index to model index to grab the correct ID
        int modelRow = tableStudents.convertRowIndexToModel(viewRow);
        int id = (int) tableStudents.getModel().getValueAt(modelRow, 0);

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