package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;



public class FormPage extends BasePage {
    private By inputname = By.id("input_25");
    private By inputbalance = By.id("input_6");
    private By enterduration = By.id("cid_35");
    private By balance = By.xpath("//*[@id='label_6']");
    //private By employeedropdown = By.xpath("//*[@id=\"label_4\"]");
    private By employeedropdown = By.id("input_4");
    private By departmentdropdown = By.name("q34_employmentType34");
    private By vacationtypedropdown = By.name("q8_vacationType");
    private By departmentoption1 = By.xpath("//*[@id=\"input_34\"]/option[2]");
    private By VacationEndDate = By.xpath("//*[@id=\"lite_mode_10\"]");
    private By inputnote = By.id("43_shorttext-1");
    private By submit = By.id("input_19");
    private By enterstartdate =By.xpath("//*[@id=\"lite_mode_9\"]");
    private By enterenddate =By.xpath("//*[@id=\"lite_mode_10\"]");
    //WebElement drop = driver.findElement(employeedropdown);
    //public Select select=new Select(drop);
    private WebDriverWait wait;
    public FormPage(WebDriver driver) {

        super(driver);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
         wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    public String getname() {
        return driver.findElement(inputname).getAttribute("value");
    }
    public void entername(String name) {
        driver.findElement(inputname).sendKeys(name);
    }

   /* public void enterbalance(int x) {
        driver.findElement(inputbalance).sendKeys(String.valueOf("X"));
    } // balance is not editable */


    public void selectEmployee(int y) {
        Select select = new Select(driver.findElement(employeedropdown));
        select.selectByIndex(y);
    }
    public void selectdepartment(int y) {
        Select select = new Select(driver.findElement(departmentdropdown));
        select.selectByIndex(y);
    }

    public void selectvacationtype(int y) {
        Select select = new Select(driver.findElement(vacationtypedropdown));
        select.selectByIndex(y);
    }

    public void enterstartDate(String day, String month, String year) {
        driver.findElement(enterstartdate).sendKeys(day + "-" + month + "-" + year);
    }

    public void enterendDate(String day, String month, String year) {
        driver.findElement(enterenddate).sendKeys(day + "-" + month + "-" + year);
    }
    public void Enterduration(String name) {
        driver.findElement(enterduration).sendKeys(name);
    }

    public void enternote() {
        driver.findElement(inputnote).sendKeys("this is great form");
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

    }
    public String getStartDate() {
        return driver.findElement(enterstartdate).getAttribute("value");
    }
    public String getEndDate() {
        return driver.findElement(enterenddate).getAttribute("value");
    }

    public String getDuration() {
        return driver.findElement(enterduration).getAttribute("value");
    }
}
