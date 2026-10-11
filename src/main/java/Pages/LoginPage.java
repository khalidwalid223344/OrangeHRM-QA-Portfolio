package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    private static final String LOGIN_PATH = "/web/index.php/auth/login";
    private static final String LOGIN_URL_FRAGMENT = "/auth/login";

    private final By usernameInput = By.name("username");
    private final By passwordInput = By.name("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By errorAlert = By.cssSelector(".oxd-alert-content-text");
    private final By fieldErrors = By.cssSelector(".oxd-input-field-error-message");
    private final By forgotPasswordLink = By.cssSelector(".orangehrm-login-forgot-header");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open(String baseUrl) {
        driver.get(baseUrl + LOGIN_PATH);
        waitVisible(usernameInput);
        return this;
    }

    public LoginPage enterUsername(String username) {
        type(usernameInput, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public void clickLogin() {
        click(loginButton);
    }

    public void loginAs(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLogin();
    }

    public ForgotPasswordPage clickForgotPassword() {
        click(forgotPasswordLink);
        return new ForgotPasswordPage(driver);
    }

    /** Text of the login error alert, or an empty string if none appears. */
    public String getAlertMessage() {
        return isVisible(errorAlert) ? getText(errorAlert) : "";
    }

    public String getUsernameFieldError() {
        return fieldErrorText("username");
    }

    public String getPasswordFieldError() {
        return fieldErrorText("password");
    }

    /** Number of inline field validation messages currently shown (0 if none appear). */
    public int getFieldErrorCount() {
        try {
            wait.until(d -> !d.findElements(fieldErrors).isEmpty());
        } catch (TimeoutException e) {
            return 0;
        }
        return driver.findElements(fieldErrors).size();
    }

    /** DOM "type" attribute of the password input ("password" means masked). */
    public String getPasswordFieldType() {
        return waitVisible(passwordInput).getDomAttribute("type");
    }

    public boolean isAt() {
        return urlContains(LOGIN_URL_FRAGMENT);
    }

    private String fieldErrorText(String inputName) {
        By locator = By.xpath("//input[@name='" + inputName
                + "']/ancestor::div[contains(@class,'oxd-input-group')][1]"
                + "//span[contains(@class,'oxd-input-field-error-message')]");
        return isVisible(locator) ? getText(locator) : "";
    }
}
