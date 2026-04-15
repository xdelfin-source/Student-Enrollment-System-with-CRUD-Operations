import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private JTextField textField1; // Search Bar

    // 1. Declare the TableRowSorter
    private TableRowSorter<DefaultTableModel> rowSorter;

    public CourseGUI() {
        JFrame frame = new JFrame("Course Management");
        frame.setContentPane(CoursePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 700));

        setupListeners(frame);
        loadCourseData();

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setupListeners(JFrame frame) {
        // Navigation
        if (studentButton != null) {
            studentButton.addActionListener(e -> { new StudentGUI(); frame.dispose(); });
        }
        if (enrollmentButton != null) {
            enrollmentButton.addActionListener(e -> { new EnrollmentGUI(); frame.dispose(); });
        }

        // Action Buttons
        btnAdd.addActionListener(e -> saveCourse());
        btnClear.addActionListener(e -> clearFields());
        btnDelete.addActionListener(e -> deleteCourse());
        btnUpdate.addActionListener(e -> updateCourse());

        // Table Selection - Updated with convertRowIndexToModel
        table1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int viewRow = table1.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = table1.convertRowIndexToModel(viewRow);
                    DefaultTableModel model = (DefaultTableModel) table1.getModel();

                    txtCourseName.setText(model.getValueAt(modelRow, 1).toString());
                    txtCourseCode.setText(model.getValueAt(modelRow, 2).toString());
                    String credits = model.getValueAt(modelRow, 3).toString();

                    if (txtCredits != null) txtCredits.setText(credits);
                    if (spinUnits != null) spinUnits.setValue(Integer.parseInt(credits));
                }
            }
        });

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

        String sql = "INSERT INTO course (course_name, course_description, credits) VALUES (?, ?, ?)";

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
            JOptionPane.showMessageDialog(CoursePanel, "Save Error: " + ex.getMessage());
        }
    }

    private void updateCourse() {
        int viewRow = table1.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(CoursePanel, "Select a course to update!");
            return;
        }

        // Convert view index to model index to grab the correct ID
        int modelRow = table1.convertRowIndexToModel(viewRow);
        int id = (int) table1.getModel().getValueAt(modelRow, 0);

        String sql = "UPDATE course SET course_name=?, course_description=?, credits=? WHERE course_id=?";

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
            JOptionPane.showMessageDialog(CoursePanel, "Update Error: " + ex.getMessage());
        }
    }

    private void deleteCourse() {
        int viewRow = table1.getSelectedRow();
        if (viewRow == -1) return;

        // Convert view index to model index to grab the correct ID
        int modelRow = table1.convertRowIndexToModel(viewRow);
        int id = (int) table1.getModel().getValueAt(modelRow, 0);

        if (JOptionPane.showConfirmDialog(CoursePanel, "Delete this course?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("DELETE FROM course WHERE course_id=?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                loadCourseData();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(CoursePanel, "Delete Error: " + ex.getMessage());
            }
        }
    }

    private void loadCourseData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM course")) {

            // Prevent direct table editing
            DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Course Name", "Course Code/Desc", "Credits"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("course_id"),
                        rs.getString("course_name"),
                        rs.getString("course_description"),
                        rs.getInt("credits")
                });
            }
            table1.setModel(model);

            // 4. Attach the sorter to the new model every time data is reloaded
            rowSorter = new TableRowSorter<>(model);
            table1.setRowSorter(rowSorter);

            // Re-apply filter in case the user typed something before saving/updating
            applyFilter();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(CoursePanel, "Load Error: " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("Could not connect to DB for loading courses.");
        }
    }

    public static void main(String[] args) {
        new CourseGUI();
    }
}