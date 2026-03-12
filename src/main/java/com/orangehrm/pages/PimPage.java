package com.orangehrm.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class PimPage extends BasePage{

    public PimPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = ".oxd-table-filter-title")
    private WebElement lblEmployeeInformation;
    @FindBy(xpath = "//button[contains(.,'Add')]")
    private WebElement btnAdd;
    @FindBy(css = "h6.orangehrm-main-title")
    private WebElement lblAddEmployeeTitle;
    @FindBy(css = "button.employee-image-action")
    private WebElement btnAddPhoto;
    @FindBy(css = "input[name='firstName']")
    private WebElement txtFirstName;
    @FindBy(css = "input[name='lastName']")
    private WebElement txtLastName;
    @FindBy(xpath = "(//input[@class='oxd-input oxd-input--active'])[2]")
    private WebElement txtEmployeeId;
    @FindBy(css = "button[type='submit'].orangehrm-left-space")
    private WebElement btnSave;
    @FindBy(css = "h6.oxd-text--h6.--strong")
    private WebElement lblEmployeeHeaderName;
    @FindBy(css = "a[href*='viewDirectory']")
    private WebElement directory;

    public void validateAddModulePim() throws InterruptedException {
        waitForElementVisible(lblEmployeeInformation, 10000);
        assertEqualsText(lblEmployeeInformation, "Employee Information");
        waitForElementClickable(btnAdd, 5000);
        takeScreenshot("Page_Pim");
        click(btnAdd);
    }


    public void addEmployee(String nameEmployee, String lastNameEmployee) throws InterruptedException {
        waitForElementVisible(lblAddEmployeeTitle, 10000);
        assertEqualsText(lblAddEmployeeTitle, "Add Employee");
        waitForElementClickable(btnAddPhoto, 10000);
        cargarFotoEmpleado("src/test/resources/images/empleado.png");
        sendKeys(txtFirstName, nameEmployee);
        sendKeys(txtLastName, lastNameEmployee);
        String numId = generarNumeroAleatorio4Cifras();
        sendKeys(txtEmployeeId, numId);
        takeScreenshot("Datos_Empleado");
        click(btnSave);
    }

    public void validateEmployeeCreation(String fullName) throws InterruptedException {
        waitForElementVisible(lblEmployeeHeaderName, 10000);
        Thread.sleep(5000);
        assertEqualsText(lblEmployeeHeaderName, fullName);
        takeScreenshot("Empleado_Creado");

    }
    public  void clickDirectory() {
        waitForElementClickable(directory, 10000);
        click(directory);
        takeScreenshot("Page_Directory");
    }
}
