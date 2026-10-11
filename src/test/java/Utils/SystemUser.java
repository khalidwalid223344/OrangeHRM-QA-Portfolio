package Utils;

/** A system user created by a test. The password is generated at runtime, never stored. */
public record SystemUser(String username, String password, String role, Employee employee) {
}
