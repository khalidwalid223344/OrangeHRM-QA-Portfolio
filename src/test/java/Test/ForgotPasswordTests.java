package Test;

import Pages.ForgotPasswordPage;
import Pages.LoginPage;
import Utils.Config;
import Utils.JsonReader;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * REQ-002 Forgot Password Flow.
 * TC-FP-004 (empty username) is a NEW case added by automation; it is not in the manual test case sheet.
 */
@Epic("OrangeHRM")
@Feature("Authentication")
public class ForgotPasswordTests extends HrmBaseTest {
    private static final JsonNode DATA = JsonReader.getTestData("forgotPassword");

    // TC-FP-001 | REQ-002
    // Related Bug: BUG-002 (504 Gateway Timeout instead of the confirmation message)
    @Test(description = "TC-FP-001: Submit Forgot Password with valid username", groups = "known-defect")
    @Story("REQ-002") @TmsLink("TC-FP-001") @Issue("BUG-002")
    public void TC_FP_001_verifyForgotPasswordWithValidUsername() {
        ForgotPasswordPage forgotPasswordPage =
                new LoginPage(driver).open(Config.baseUrl()).clickForgotPassword();
        Assert.assertTrue(forgotPasswordPage.isAt(), "Forgot Password page should be displayed");

        forgotPasswordPage.enterUsername(Config.adminUsername()).clickReset();

        String message = forgotPasswordPage.waitForConfirmationContaining(
                DATA.get("confirmationFragment").asText());
        Assert.assertTrue(message.toLowerCase().contains(DATA.get("confirmationFragment").asText()),
                "A confirmation message should be displayed but the page showed: '" + message
                        + "' (browser tab title: '" + driver.getTitle() + "')");
    }

    // TC-FP-002 | REQ-002
    @Test(description = "TC-FP-002: Submit Forgot Password with an unknown username")
    @Story("REQ-002") @TmsLink("TC-FP-002")
    public void TC_FP_002_verifyForgotPasswordWithUnknownUsernameShowsErrorAndStaysOnPage() {
        ForgotPasswordPage page = submitUnknownUsername();

        String feedback = page.waitForFeedback();
        Assert.assertFalse(feedback.isBlank(), "An appropriate error message should be displayed");
        Assert.assertTrue(page.isAt(), "User should remain on the Forgot Password page");
        Assert.assertFalse(page.looksLikeServerError(), "The system should not crash (server error page)");
    }

    // TC-FP-003 | REQ-002
    // Heuristic: the manual case defines "no sensitive information" in prose only, so it is checked
    // against existence-revealing phrases listed in TestData.json (forgotPassword.leakIndicators) and e-mail addresses.
    @Test(description = "TC-FP-003: Unknown username response does not leak account information")
    @Story("REQ-002") @TmsLink("TC-FP-003")
    public void TC_FP_003_verifyUnknownUsernameResponseDoesNotLeakAccountInformation() {
        ForgotPasswordPage page = submitUnknownUsername();

        String response = (page.waitForFeedback() + " " + page.getHeadingText()).trim().toLowerCase();
        Assert.assertFalse(response.isBlank(), "A response should be observable after submitting");
        for (JsonNode indicator : DATA.get("leakIndicators")) {
            Assert.assertFalse(response.contains(indicator.asText()),
                    "Response reveals account information ('" + indicator.asText() + "'): " + response);
        }
        Assert.assertFalse(response.matches(".*[\\w.+-]+@[\\w-]+\\.[\\w.]+.*"),
                "Response must not reveal an e-mail address: " + response);
    }

    // TC-FP-004 | REQ-002 | NEW (not in the manual sheet): required-field validation on the username
    @Test(description = "TC-FP-004 (new): Submit Forgot Password with an empty username")
    @Story("REQ-002") @TmsLink("TC-FP-004")
    public void TC_FP_004_verifyForgotPasswordWithEmptyUsernameShowsRequiredValidation() {
        ForgotPasswordPage page = new LoginPage(driver).open(Config.baseUrl()).clickForgotPassword();

        page.clickReset();

        Assert.assertTrue(page.getUsernameFieldError().toLowerCase().contains(DATA.get("requiredMessage").asText()),
                "A required-field validation message should be shown for the username");
        Assert.assertTrue(page.isAt(), "User should remain on the Forgot Password page");
    }

    private ForgotPasswordPage submitUnknownUsername() {
        ForgotPasswordPage page = new LoginPage(driver).open(Config.baseUrl()).clickForgotPassword();
        page.enterUsername(DATA.get("unknownUsername").asText()).clickReset();
        return page;
    }
}
