package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared building blocks for OrangeHRM 5 screens: label-based form controls, dropdowns,
 * result tables, feedback messages and the delete-confirmation dialog.
 * Controls are located through their visible label, which is more stable than DOM position.
 */
public abstract class OrangeHrmPage extends BasePage {
    protected static final By SUBMIT = By.cssSelector("button[type='submit']");
    protected static final By FEEDBACK = By.cssSelector(
            ".oxd-toast-content-text, .oxd-alert-content-text, .oxd-input-field-error-message");

    private static final By SPINNER = By.cssSelector(".oxd-loading-spinner");
    private static final By RECORDS_SUMMARY =
            By.cssSelector(".orangehrm-horizontal-padding.orangehrm-vertical-padding > span");
    private static final By TABLE_ROWS = By.cssSelector(".oxd-table-body .oxd-table-card");
    private static final By TABLE_HEADERS = By.cssSelector(".oxd-table-header .oxd-table-header-cell");
    private static final By DROPDOWN_OPTIONS = By.xpath("//div[@role='listbox']//div[@role='option']");
    private static final By SUCCESS_TOAST = By.cssSelector(".oxd-toast--success");
    private static final By ERROR_TOAST = By.cssSelector(".oxd-toast--error");
    private static final By DELETE_DIALOG = By.cssSelector(".orangehrm-dialog-popup");
    /** Overlay shown while a form is busy (loading balances, saving, ...); it intercepts clicks. */
    private static final By FORM_LOADER = By.cssSelector(".oxd-form-loader");
    private static final By VALIDATION_MESSAGES = By.cssSelector(".oxd-input-field-error-message");

    /**
     * OrangeHRM toasts fade after a few seconds, often at the very moment the app navigates, so reading
     * the DOM "afterwards" races them. This observer records every toast (kind + text) as it renders.
     * The app is a single-page application, so the log survives in-app navigation.
     */
    private static final String INSTALL_TOAST_LOG = """
            if (!window.__hrmToastLog) {
              window.__hrmToastLog = { entries: [], seen: new WeakMap() };
              var scan = function () {
                document.querySelectorAll('.oxd-toast').forEach(function (el) {
                  var kind = 'other';
                  ['success', 'error', 'warn', 'info'].forEach(function (k) {
                    if (el.classList.contains('oxd-toast--' + k)) { kind = k; }
                  });
                  var content = el.querySelector('.oxd-toast-content') || el;
                  var text = (content.innerText || '').replace(/\\s+/g, ' ').trim();
                  var entry = window.__hrmToastLog.seen.get(el);
                  if (!entry) {
                    entry = { kind: kind, text: text };
                    window.__hrmToastLog.seen.set(el, entry);
                    window.__hrmToastLog.entries.push(entry);
                  } else {
                    entry.kind = kind;
                    if (text) { entry.text = text; }
                  }
                });
              };
              new MutationObserver(scan).observe(document.body,
                  { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: ['class'] });
              scan();
            }
            """;
    private static final String RESET_TOAST_LOG =
            "if (window.__hrmToastLog) { window.__hrmToastLog.entries.length = 0; }";
    private static final String READ_TOAST_LOG = """
            return window.__hrmToastLog
              ? window.__hrmToastLog.entries.map(function (e) { return e.kind + '\\t' + e.text; }).join('\\n')
              : '';
            """;

    protected OrangeHrmPage(WebDriver driver) {
        super(driver);
    }

    // ---------- clicking and feedback capture ----------

    /**
     * Waits for the busy overlay to disappear, then clicks, retrying while another element still
     * intercepts the click. Toast capture is armed on every click and restarted on form submits.
     */
    @Override
    protected void click(By locator) {
        installFeedbackCapture();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(FORM_LOADER));
        if (SUBMIT.equals(locator)) {
            resetFeedbackCapture();
        }
        new WebDriverWait(driver, TIMEOUT)
                .ignoring(ElementClickInterceptedException.class, StaleElementReferenceException.class)
                .until(d -> {
                    WebElement element = ExpectedConditions.elementToBeClickable(locator).apply(d);
                    if (element == null) {
                        return false;
                    }
                    element.click();
                    return true;
                });
    }

    /** Starts recording toasts on the current document (idempotent). */
    protected void installFeedbackCapture() {
        runScript(INSTALL_TOAST_LOG);
    }

    /** Forgets toasts recorded so far, so only feedback from the next action is considered. */
    protected void resetFeedbackCapture() {
        runScript(RESET_TOAST_LOG);
    }

    private void runScript(String script) {
        try {
            ((JavascriptExecutor) driver).executeScript(script);
        } catch (RuntimeException ignored) {
            // page is mid-navigation; capture is best-effort and the live DOM is still checked
        }
    }

    /** Recorded toasts as "kind\ttext" lines ("" if none or no observer installed). */
    public static String readToastLog(WebDriver driver) {
        try {
            Object result = ((JavascriptExecutor) driver).executeScript(READ_TOAST_LOG);
            return result == null ? "" : result.toString();
        } catch (RuntimeException e) {
            return "";
        }
    }

    private List<String> recordedToasts(String kind) {
        List<String> texts = new ArrayList<>();
        for (String line : readToastLog(driver).split("\n")) {
            String[] pair = line.split("\t", 2);
            if (pair.length == 2 && (kind == null || pair[0].equals(kind))) {
                texts.add(pair[1]);
            }
        }
        return texts;
    }

    /** One-line snapshot for assertion messages: URL, busy overlay, toasts seen, validation messages. */
    public String describeState() {
        StringBuilder sb = new StringBuilder("url=");
        try {
            sb.append(driver.getCurrentUrl());
            sb.append("; busyOverlay=").append(!driver.findElements(FORM_LOADER).isEmpty());
        } catch (RuntimeException e) {
            sb.append("<unavailable>");
        }
        sb.append("; toasts=").append(readToastLog(driver).replace('\t', ':').replace('\n', '|'));
        List<String> validation = new ArrayList<>();
        try {
            for (WebElement e : driver.findElements(VALIDATION_MESSAGES)) {
                validation.add(e.getText().trim());
            }
        } catch (RuntimeException ignored) {
            // best-effort
        }
        return sb.append("; validation=").append(validation).toString();
    }

    // ---------- label-based locators ----------

    /**
     * XPath of the form-field container holding the label. Matches the exact class token
     * 'oxd-input-group' (a plain contains() would also match the label wrapper
     * 'oxd-input-group__label-wrapper' that sits between the label and the container).
     */
    private static final String GROUP_CLASS = "contains(concat(' ', normalize-space(@class), ' '), ' oxd-input-group ')";

    private String groupOf(String label) {
        return "//label[normalize-space()='" + label + "']/ancestor::div[" + GROUP_CLASS + "][1]";
    }

    protected final By inputByLabel(String label) {
        return By.xpath(groupOf(label) + "//input");
    }

    protected final By selectByLabel(String label) {
        return By.xpath(groupOf(label) + "//div[contains(@class,'oxd-select-text')]");
    }

    protected final By errorByLabel(String label) {
        return By.xpath(groupOf(label) + "//span[contains(@class,'oxd-input-field-error-message')]");
    }

    /** Inline validation message of the control with this label, or "" if none appears. */
    protected String fieldError(String label) {
        By locator = errorByLabel(label);
        return isVisible(locator) ? getText(locator) : "";
    }

    /** Inline validation message of the input with this name attribute, or "" if none appears. */
    protected String fieldErrorByInputName(String inputName) {
        By locator = By.xpath("//input[@name='" + inputName
                + "']/ancestor::div[" + GROUP_CLASS + "][1]"
                + "//span[contains(@class,'oxd-input-field-error-message')]");
        return isVisible(locator) ? getText(locator) : "";
    }

    // ---------- dropdowns and dates ----------

    protected void chooseOption(String label, String optionText) {
        click(selectByLabel(label));
        click(By.xpath("//div[@role='listbox']//span[normalize-space()='" + optionText + "']"));
    }

    /** Opens the dropdown and picks the first real option (not the placeholder); returns its text. */
    protected String chooseFirstOption(String label) {
        click(selectByLabel(label));
        wait.until(d -> d.findElements(DROPDOWN_OPTIONS).size() > 1);
        for (WebElement option : driver.findElements(DROPDOWN_OPTIONS)) {
            String text = option.getText().trim();
            if (!text.isEmpty() && !text.startsWith("-- Select")) {
                option.click();
                return text;
            }
        }
        throw new IllegalStateException("No selectable option available for '" + label + "'");
    }

    protected String selectedText(String label) {
        return getText(selectByLabel(label));
    }

    protected void closeDropdown() {
        driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE);
    }

    /** Replaces the value of a date input (an empty value just clears it). */
    protected void typeDate(By input, String value) {
        WebElement element = waitVisible(input);
        element.clear();
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        if (!value.isEmpty()) {
            element.sendKeys(value);
        }
        element.sendKeys(Keys.TAB);
    }

    // ---------- loading, results, tables ----------

    /** Waits for the loading spinner to appear (briefly) and then disappear. */
    protected void waitForLoading() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.visibilityOfElementLocated(SPINNER));
        } catch (TimeoutException ignored) {
            // the request may already have finished
        }
        wait.until(ExpectedConditions.invisibilityOfElementLocated(SPINNER));
    }

    public String getRecordsSummary() {
        return getText(RECORDS_SUMMARY);
    }

    /** Number from "(n) Records Found"; 0 for "No Records Found". */
    public int getRecordCount() {
        Matcher matcher = Pattern.compile("\\((\\d+)\\)").matcher(getRecordsSummary());
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    /** Values of one column (matched by exact header text) for every row on the current page. */
    public List<String> getColumnValues(String header) {
        List<String> headers = new ArrayList<>();
        for (WebElement cell : driver.findElements(TABLE_HEADERS)) {
            headers.add(cell.getText().trim());
        }
        int index = -1;
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equalsIgnoreCase(header)) {
                index = i;
            }
        }
        if (index < 0) {
            throw new IllegalStateException("Column '" + header + "' not found; columns are " + headers);
        }
        List<String> values = new ArrayList<>();
        for (WebElement row : driver.findElements(TABLE_ROWS)) {
            values.add(row.findElements(By.cssSelector(".oxd-table-cell")).get(index).getText().trim());
        }
        return values;
    }

    protected By rowByCell(String cellText) {
        return By.xpath("//div[contains(@class,'oxd-table-card')][.//div[@role='cell' and normalize-space(.)='"
                + cellText + "']]");
    }

    // ---------- delete flow ----------

    /** Clicks the trash icon of the row that contains a cell with exactly this text. */
    public void clickDeleteForRow(String cellText) {
        By trash = By.xpath("(//div[contains(@class,'oxd-table-card')][.//div[@role='cell' and normalize-space(.)='"
                + cellText + "']]//button[.//i[contains(@class,'bi-trash')]])[1]");
        click(trash);
    }

    public void deleteRow(String cellText) {
        clickDeleteForRow(cellText);
        confirmDelete();
        waitForLoading();
    }

    public boolean isDeleteDialogDisplayed() {
        return isVisible(DELETE_DIALOG);
    }

    public boolean isDeleteDialogClosed() {
        return waitInvisible(DELETE_DIALOG);
    }

    public void confirmDelete() {
        resetFeedbackCapture();
        click(By.xpath("//div[contains(@class,'orangehrm-dialog-popup')]//button[normalize-space()='Yes, Delete']"));
    }

    public void cancelDelete() {
        click(By.xpath("//div[contains(@class,'orangehrm-dialog-popup')]//button[normalize-space()='No, Cancel']"));
    }

    public void closeDeleteDialog() {
        click(By.cssSelector(".orangehrm-dialog-popup .oxd-dialog-close-button"));
    }

    /** Two synchronous clicks on the confirm button, i.e. "click confirm repeatedly/quickly". */
    public void confirmDeleteWithRapidClicks() {
        WebElement confirm = waitClickable(By.xpath(
                "//div[contains(@class,'orangehrm-dialog-popup')]//button[normalize-space()='Yes, Delete']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click(); arguments[0].click();", confirm);
        waitForLoading();
    }

    // ---------- feedback ----------

    /**
     * True if a success toast was shown since the last submit: checked against the recorded toast log
     * (a toast that has already faded still counts) and the live DOM, waiting up to the default timeout.
     */
    public boolean hasSuccessToast() {
        try {
            return wait.until(d -> !recordedToasts("success").isEmpty()
                    || !d.findElements(SUCCESS_TOAST).isEmpty());
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isErrorToastShown() {
        for (WebElement toast : driver.findElements(ERROR_TOAST)) {
            if (toast.isDisplayed()) {
                return true;
            }
        }
        return false;
    }

    /** Every toast text recorded since the last submit plus what is visible right now, space separated. */
    public String getFeedbackText() {
        Set<String> parts = new LinkedHashSet<>(recordedToasts(null));
        String live = liveFeedbackText();
        if (!live.isBlank()) {
            parts.add(live);
        }
        return String.join(" ", parts).trim();
    }

    /** All currently visible toast / alert / inline-validation texts, space separated. */
    private String liveFeedbackText() {
        StringBuilder text = new StringBuilder();
        for (WebElement element : driver.findElements(FEEDBACK)) {
            try {
                if (element.isDisplayed()) {
                    text.append(element.getText().trim()).append(' ');
                }
            } catch (StaleElementReferenceException ignored) {
                // toast vanished while reading
            }
        }
        return text.toString().trim();
    }
}
