package com.orangehrm.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class DirectoryPage extends BasePage{

    public DirectoryPage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "a[href*='viewDirectory']")
    private WebElement directory;
    @FindBy(css = ".oxd-autocomplete-text-input input")
    private WebElement txtEmployeeHints;
    @FindBy(css = ".oxd-autocomplete-option")
    private WebElement firstAutocompleteOption;
    @FindBy(css = "button[type='submit'].orangehrm-left-space")
    private WebElement btnSearchEmployee;
    @FindBy(css = ".orangehrm-directory-card-header")
    private WebElement lblCardName;


    public void searchEmployee(String nameEmployee) throws InterruptedException {
        waitForElementVisible(txtEmployeeHints, 5000);
        sendKeys(txtEmployeeHints, nameEmployee);
        waitForElementClickable(firstAutocompleteOption, 10000);
        Thread.sleep(2000);
        click(firstAutocompleteOption);
        takeScreenshot("Buscar_Empleado");
        Thread.sleep(10000);
        waitForElementClickable(btnSearchEmployee, 5000);
        click(btnSearchEmployee);
        Thread.sleep(10000);
    }

    public void validateSearchEmployee(String fullName) throws InterruptedException {
        waitForElementVisible(lblCardName, 5000);
        assertEqualsText(lblCardName, fullName);
        takeScreenshot("Empleado_Buscado");
        Thread.sleep(10000);
    }

}
