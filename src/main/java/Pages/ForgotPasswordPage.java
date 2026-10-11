package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Pattern;

public class ForgotPasswordPage extends OrangeHrmPage {
    private static final String FORGOT_PASSWORD_URL_FRAGMENT = "/auth/requestPasswordResetCode";

    private final By title = By.cssSelector(".orangehrm-forgot-password-title");
    private final By usernameInput = By.name("username");
    private final By resetButton = By.cssSelector("button[type='submit']");

    public ForgotPasswordPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAt() {
        return urlContains(FORGOT_PASSWORD_URL_FRAGMENT);
    }

    public ForgotPasswordPage enterUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    public void clickReset() {
        click(resetButton);
    }

    /**
     * Waits (up to the default timeout) for the page heading to contain the given text,
     * then returns the current heading text, or an empty string if no heading is present
     * (e.g. a gateway-timeout error page). The caller asserts on the returned value.
     */
    public String waitForConfirmationContaining(String fragment) {
        try {
            wait.until(d -> {
                List<WebElement> headings = d.findElements(title);
                return !headings.isEmpty()
                        && headings.get(0).getText().toLowerCase().contains(fragment.toLowerCase());
            });
        } catch (TimeoutException ignored) {
            // fall through: the test asserts on whatever is actually displayed
        }
        List<WebElement> headings = driver.findElements(title);
        return headings.isEmpty() ? "" : headings.get(0).getText().trim();
    }

    /** Waits for any toast/alert/validation text to appear and returns it ("" if none appears). */
    public String waitForFeedback() {
        try {
            wait.until(d -> !d.findElements(FEEDBACK).isEmpty() && !getFeedbackText().isBlank());
        } catch (TimeoutException ignored) {
            // the caller asserts on whatever is (not) displayed
        }
        return getFeedbackText();
    }

    /**
     * Text of the card note ("If the email does not arrive, please contact your OrangeHRM Administrator.").
     * The page nests a paragraph inside a paragraph, which browsers split into siblings, so the note is
     * located by its text instead of by the outer wrapper class. Returns "" when absent.
     */
    public String getCardNoteText() {
        List<WebElement> notes = driver.findElements(By.xpath(
                "//p[contains(normalize-space(.), 'contact your OrangeHRM Administrator')]"));
        return notes.isEmpty() ? "" : notes.get(0).getText().trim();
    }

    public String getUsernameFieldError() {
        return fieldErrorByInputName("username");
    }

    /** Heading text, or "" when the page shows none (e.g. a gateway error page). */
    public String getHeadingText() {
        List<WebElement> headings = driver.findElements(title);
        return headings.isEmpty() ? "" : headings.get(0).getText().trim();
    }

    /** True if the visible page text looks like a server/gateway failure (5xx, gateway, internal error). */
    public boolean looksLikeServerError() {
        String body = driver.findElement(By.tagName("body")).getText();
        return Pattern.compile("(?i)\\b(50[0-4]|bad gateway|gateway time-?out|internal server error)\\b")
                .matcher(body).find();
    }
}
