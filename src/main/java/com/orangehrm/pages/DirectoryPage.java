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
        waitForElementVisible(txtEmployeeHints, 10000);
        sendKeys(txtEmployeeHints, nameEmployee);
        waitForElementClickable(firstAutocompleteOption, 10000);
        Thread.sleep(4000);
        click(firstAutocompleteOption);
        takeScreenshot("Buscar_Empleado");
        waitForElementClickable(btnSearchEmployee, 5000);
        click(btnSearchEmployee);
    }

    public void validateSearchEmployee(String fullName) throws InterruptedException {
        waitForElementVisible(lblCardName, 10000);
        assertEqualsText(lblCardName, fullName);
        takeScreenshot("Empleado_Buscado");
    }
}
