package com.solvd.testng.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ProductPage {

    private WebDriver driver;

    @FindBy(xpath = "//h3[@class='price-container']")
    private WebElement priceContainer;

    @FindBy(xpath = "//a[@class='btn btn-success btn-lg']")
    private WebElement addToCartButton;

    @FindBy(xpath = "//a[@id='cartur']")
    private WebElement cartButton;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }


    public String getProductPrice() {
        String rawPrice = priceContainer.getText();
        return rawPrice.replaceAll("[^0-9]", "");
    }

    public void addToCart() {
        addToCartButton.click();
    }

    public void goToCart() {
        cartButton.click();
    }

    public WebElement getProductPriceElement() {
        return priceContainer;
    }
}