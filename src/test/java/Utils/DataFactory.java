package Utils;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/** Generates unique, collision-free test data so reruns never clash with persisted records. */
public final class DataFactory {
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALNUM = LOWER + LOWER.toUpperCase() + "0123456789";
    private static final Random SECURE = new SecureRandom();

    private DataFactory() {
    }

    private static String random(String alphabet, int length, Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    public static String firstName() {
        return "Qa" + random(LOWER, 6, ThreadLocalRandom.current());
    }

    public static String lastName() {
        return "Auto" + random(LOWER, 6, ThreadLocalRandom.current());
    }

    /** Employee Id (max 10 chars): 'T' + 8 digits. */
    public static String employeeId() {
        return "T" + (10_000_000 + ThreadLocalRandom.current().nextInt(90_000_000));
    }

    public static String username() {
        return "qauser" + random(LOWER, 8, ThreadLocalRandom.current());
    }

    /** Random password meeting typical policy (upper, lower, digit, 12 chars); generated per run. */
    public static String password() {
        return "Aa1" + random(ALNUM, 9, SECURE);
    }

    /** A random Monday at least 90 days ahead, to avoid overlapping earlier leave requests. */
    public static LocalDate futureMonday() {
        LocalDate date = LocalDate.now().plusDays(90 + ThreadLocalRandom.current().nextInt(1500));
        while (date.getDayOfWeek() != DayOfWeek.MONDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /** Formats a date using an OrangeHRM placeholder such as yyyy-mm-dd or dd-mm-yyyy. */
    public static String format(LocalDate date, String placeholder) {
        String pattern = (placeholder == null || placeholder.isBlank()) ? "yyyy-MM-dd"
                : placeholder.replace("mm", "MM");
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }
}
