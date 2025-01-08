package com.solvd.testng.pages;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Getter
public class CheckoutPage {

    private WebDriver driver;

    @FindBy(xpath = "//input[@id='name']")
    WebElement nameField;

    @FindBy(xpath = "//input[@id='country']")
    private WebElement countryField;

    @FindBy(xpath = "//input[@id='city']")
    private WebElement cityField;

    @FindBy(xpath = "//input[@id='card']")
    private WebElement cardField;

    @FindBy(xpath = "//input[@id='month']")
    private WebElement monthField;

    @FindBy(xpath = "//input[@id='year']")
    private WebElement yearField;

    @FindBy(xpath = "//button[@onclick='purchaseOrder()']")
    private WebElement purchaseButton;

    @FindBy(css = "p.lead.text-muted")
    private WebElement purchaseDetails;

    @FindBy(xpath = "//button[@class='confirm btn btn-lg btn-primary']")
    private WebElement confirmButton;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void fillCheckoutDetails(String name, String country, String city, String card, String month, String year) {
        nameField.sendKeys(name);
        countryField.sendKeys(country);
        cityField.sendKeys(city);
        cardField.sendKeys(card);
        monthField.sendKeys(month);
        yearField.sendKeys(year);
    }

    public void completePurchase() {
        purchaseButton.click();
    }

    public String getPurchaseDetails() {
        return purchaseDetails.getText();
    }

    public WebElement getPurchaseDetailsElement() {
        return purchaseDetails;
    }

    public void confirmPurchase() {
    	confirmButton.click();
    }
}
