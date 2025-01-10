package com.solvd.testng.pages;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@Getter
public class AllProductsPage {

    private WebDriver driver;

    @FindBy(xpath = "//a[@class='hrefch']")
    private List<WebElement> productList;

    @FindBy(css = "#navbarExample > ul > li.nav-item.active > a")
    private WebElement Home;

    @FindBy(xpath = "//a[@id='cartur']")
    private WebElement cartButton;

    @FindBy(xpath = "//a[@id='logout2']")
    private WebElement logoutButton;

    public AllProductsPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public void selectProductByIndex(int index) throws InterruptedException {
        Thread.sleep(2000);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Re-fetch the product list to avoid stale references
        List<WebElement> updatedProductList = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//a[@class='hrefch']")));

        if (index >= updatedProductList.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        WebElement product = wait.withMessage("Product is not visible in the list!")
                .until(ExpectedConditions.elementToBeClickable(updatedProductList.get(index)));

        product.click();
    }

    public List<String> getProductNames() throws InterruptedException {
        Thread.sleep(2000);
        return productList.stream()
                .map(WebElement::getText)
                .toList();
    }

    public void goToHome() {
        Home.click();
    }


    public void goToCart() {
        cartButton.click();
    }
}
