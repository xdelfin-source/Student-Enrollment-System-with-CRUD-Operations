Student Enrollment Management System README

Project Structure

The project is built using Java and IntelliJ IDEA UI Designer. It consists of the following main components:

StudentGUI: The window for adding, updating, and deleting student records.
CourseGUI: The window for managing course information like Name, Description, and Credits.
EnrollmentGUI: The window for enrolling students into courses and viewing all active enrollments.
DatabaseConnection: A utility class that handles the JDBC connection to the MySQL database.

Dependencies Required

To run this project, you need the following installed on your system:

Java Development Kit: Version 8 or higher.

MySQL Server and MySQL Workbench: To host and view the database.

MySQL JDBC Driver: Required for Java to talk to MySQL.
Download the MySQL Connector J. Setup in IntelliJ by going to File, then Project Structure, then Modules, then the Dependencies tab. Click the plus icon, choose JARs or Directories, and select the downloaded jar file.

How to Set Up the MySQL Database

Before running the application, you must set up the database.

Open MySQL Workbench.

Open a new SQL tab.

Type the following SQL script to create the database and tables:

CREATE DATABASE student_management;
USE student_management;

CREATE TABLE student (
student_id INT AUTO_INCREMENT PRIMARY KEY,
first_name VARCHAR(50),
last_name VARCHAR(50),
email VARCHAR(100),
age INT
);

CREATE TABLE course (
course_id INT AUTO_INCREMENT PRIMARY KEY,
course_name VARCHAR(100),
course_description VARCHAR(100),
credits INT
);

CREATE TABLE enrolled_subject (
enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
student_id INT,
course_id INT,
enrollment_date DATE,
FOREIGN KEY (student_id) REFERENCES student(student_id),
FOREIGN KEY (course_id) REFERENCES course(course_id)
);

Execute the script to create the tables.

Make sure the database credentials like username, password, and database URL in your DatabaseConnection file match your local MySQL setup.

How to Run the Application

Open the project folder in IntelliJ IDEA.

Ensure the MySQL JDBC driver is added to your project dependencies.

Ensure your local MySQL Server is currently running.

Open any of the GUI files like StudentGUI.

Run the file in IntelliJ.

Use the navigation buttons on the side panel to switch between the Student, Course, and Enrollment windows.
