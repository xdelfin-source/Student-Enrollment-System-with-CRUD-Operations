public class Student {
    private int studentId;
    private String firstName;
    private String lastName;
    private int age;
    private String email;

    // Constructor with ID (for fetching from DB)
    public Student(int studentId, String firstName, String lastName, int age, String email) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
    }

    // Constructor without ID (for adding a new student)
    public Student(String firstName, String lastName, int age, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.email = email;
    }

    // Getters
    public int getStudentId() { return studentId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getAge() { return age; }
    public String getEmail() { return email; }
}