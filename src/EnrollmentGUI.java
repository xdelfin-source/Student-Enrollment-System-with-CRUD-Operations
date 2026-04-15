import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JTextField textField1; // Search Bar

    private HashMap<String, Integer> studentMap = new HashMap<>();
    private HashMap<String, Integer> courseMap = new HashMap<>();

    // 1. Declare the TableRowSorter
    private TableRowSorter<DefaultTableModel> rowSorter;

    public EnrollmentGUI() {
        JFrame frame = new JFrame("Enrollment Management");
        frame.setContentPane(EnrollmentPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));

        setupListeners(frame);
        refreshData();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupListeners(JFrame frame) {
        // --- Navigation ---
        if (studentButton != null) {
            studentButton.addActionListener(e -> { new StudentGUI(); frame.dispose(); });
        }
        if (coursesButton != null) {
            coursesButton.addActionListener(e -> { new CourseGUI(); frame.dispose(); });
        }

        // --- Action Buttons ---
        if (btnEnroll != null) btnEnroll.addActionListener(e -> enrollStudent());
        if (btnUpdateEnrollment != null) btnUpdateEnrollment.addActionListener(e -> updateEnrollment());
        if (btnDeleteEnrollment != null) btnDeleteEnrollment.addActionListener(e -> deleteEnrollment());
        if (btnClearEnrollment != null) btnClearEnrollment.addActionListener(e -> clearFields());

        // --- Table Selection - Updated with convertRowIndexToModel ---
        if (tableEnrollment != null) {
            tableEnrollment.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int viewRow = tableEnrollment.getSelectedRow();
                    if (viewRow != -1) {
                        int modelRow = tableEnrollment.convertRowIndexToModel(viewRow);
                        DefaultTableModel model = (DefaultTableModel) tableEnrollment.getModel();

                        comboStudent.setSelectedItem(model.getValueAt(modelRow, 1).toString());
                        comboCourse.setSelectedItem(model.getValueAt(modelRow, 2).toString());
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

    private void refreshData() {
        loadDropdowns();
        loadEnrollmentData();
    }

    private void loadDropdowns() {
        studentMap.clear();
        courseMap.clear();
        if (comboStudent != null) comboStudent.removeAllItems();
        if (comboCourse != null) comboCourse.removeAllItems();

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return;

            // Load Students
            ResultSet rsS = conn.createStatement().executeQuery("SELECT student_id, CONCAT(first_name, ' ', last_name) AS full_name FROM student");
            while (rsS.next()) {
                String name = rsS.getString("full_name");
                comboStudent.addItem(name);
                studentMap.put(name, rsS.getInt("student_id"));
            }

            // Load Courses
            ResultSet rsC = conn.createStatement().executeQuery("SELECT course_id, course_name FROM course");
            while (rsC.next()) {
                String title = rsC.getString("course_name");
                comboCourse.addItem(title);
                courseMap.put(title, rsC.getInt("course_id"));
            }
        } catch (SQLException ex) {
            System.out.println("Dropdown Load Error: " + ex.getMessage());
        }
    }

    private void enrollStudent() {
        String sName = (String) comboStudent.getSelectedItem();
        String cName = (String) comboCourse.getSelectedItem();

        if (sName == null || cName == null) return;

        String sql = "INSERT INTO enrolled_subject (student_id, course_id, enrollment_date) VALUES (?, ?, CURDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentMap.get(sName));
            pstmt.setInt(2, courseMap.get(cName));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(EnrollmentPanel, "Student Enrolled!");
            loadEnrollmentData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Enroll Error: " + ex.getMessage());
        }
    }

    private void updateEnrollment() {
        if (tableEnrollment == null) return;

        int viewRow = tableEnrollment.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Select an enrollment to update.");
            return;
        }

        // Convert view index to model index
        int modelRow = tableEnrollment.convertRowIndexToModel(viewRow);
        int id = (int) tableEnrollment.getModel().getValueAt(modelRow, 0);

        String sName = (String) comboStudent.getSelectedItem();
        String cName = (String) comboCourse.getSelectedItem();

        String sql = "UPDATE enrolled_subject SET student_id = ?, course_id = ? WHERE enrollment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        if (tableEnrollment == null) return;

        int viewRow = tableEnrollment.getSelectedRow();
        if (viewRow == -1) return;

        // Convert view index to model index
        int modelRow = tableEnrollment.convertRowIndexToModel(viewRow);
        int id = (int) tableEnrollment.getModel().getValueAt(modelRow, 0);

        if (JOptionPane.showConfirmDialog(EnrollmentPanel, "Remove this enrollment?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM enrolled_subject WHERE enrollment_id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadEnrollmentData();
                clearFields();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(EnrollmentPanel, "Delete Error: " + ex.getMessage());
            }
        }
    }

    private void clearFields() {
        if (comboStudent != null && comboStudent.getItemCount() > 0) comboStudent.setSelectedIndex(0);
        if (comboCourse != null && comboCourse.getItemCount() > 0) comboCourse.setSelectedIndex(0);
        if (tableEnrollment != null) tableEnrollment.clearSelection();
    }

    private void loadEnrollmentData() {
        if (tableEnrollment == null) {
            System.out.println("Warning: tableEnrollment is null. Check UI Designer bindings.");
            return;
        }

        String sql = "SELECT e.enrollment_id, CONCAT(s.first_name, ' ', s.last_name) AS student_name, " +
                "c.course_name, e.enrollment_date " +
                "FROM enrolled_subject e " +
                "JOIN student s ON e.student_id = s.student_id " +
                "JOIN course c ON e.course_id = c.course_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Make cells non-editable
            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Student Name", "Course", "Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("enrollment_id"),
                        rs.getString("student_name"),
                        rs.getString("course_name"),
                        rs.getDate("enrollment_date")
                });
            }
            tableEnrollment.setModel(model);

            // 4. Attach the sorter to the new model every time data is reloaded
            rowSorter = new TableRowSorter<>(model);
            tableEnrollment.setRowSorter(rowSorter);

            // Re-apply filter in case the user typed something before saving/updating
            applyFilter();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "Database SQL Error:\n" + ex.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(EnrollmentPanel, "General Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new EnrollmentGUI();
    }
}