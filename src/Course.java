public class Course {
    private int courseId;
    private String courseName;
    private String courseDescription;
    private int credits;

    // Constructor with ID
    public Course(int courseId, String courseName, String courseDescription, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.credits = credits;
    }

    // Constructor without ID
    public Course(String courseName, String courseDescription, int credits) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.credits = credits;
    }

    // Getters
    public int getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getCourseDescription() { return courseDescription; }
    public int getCredits() { return credits; }
}