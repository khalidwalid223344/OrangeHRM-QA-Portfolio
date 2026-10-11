package Utils;

/** An employee created by a test (id as shown in OrangeHRM). */
public record Employee(String firstName, String lastName, String employeeId) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
