package com.orangehrm.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @FindBy(css = "a[href*='viewPimModule']")
    private WebElement pim;

    public void homeClickPim() {
        waitForElementVisible(pim, 5000);
        takeScreenshot("Home_Page");
        click(pim);
    }
}