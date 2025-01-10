package com.solvd.pageobjectpattern;

import com.solvd.testng.pages.*;
import com.solvd.testng.utils.DriverPool;
import com.solvd.testng.utils.Person;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProductStoreTest {

    private static ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();

    @Parameters({"browser"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver();
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        threadLocalDriver.set(driver);
    }


    @AfterMethod
    public void tearDown() {
        WebDriver driver = threadLocalDriver.get();
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Error while quitting driver: " + e.getMessage());
            } finally {
                threadLocalDriver.remove();
            }
        }
    }

    public WebDriver getDriver() {
        return threadLocalDriver.get();
    }


    @Test
    public void testLogin() {
        WebDriver driver = getDriver();
        login(driver, "jakubszczypek", "1234");
        assertLogin(driver, "jakubszczypek");
    }

    @Test
    public void testAddProductToCart() throws InterruptedException {
        WebDriver driver = getDriver();
        // Log in - the helpful method to log in - the reuse of code
        login(driver, "jakubszczypek2", "1234");

        // Adds product to the cart - helpful method which reduce amount of code
        List<String> informations = addProductToCartByIndex(driver, 0);
        Assert.assertNotNull(informations, "Product information should not be null!");
        Assert.assertFalse(informations.isEmpty(), "Product information should not be empty!");

        String productName  = informations.get(0);
        String price = informations.get(1);

        ProductPage productPage = new ProductPage(driver);
        productPage.goToCart();

        CartPage cartPage = new CartPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.withMessage("The cart is not loaded properly!")
                .until(ExpectedConditions.visibilityOfAllElements(cartPage.getProductNamesInCartElement()));

        assertsInCart(driver, productName, price);

        deleteProductsInTheCartAndCheckIt(driver);
    }

    @Test
    public void testAddSingleProductPurchase() throws InterruptedException {
        WebDriver driver = getDriver();

        login(driver, "jakubszczypek3", "1234");

        AllProductsPage allProductsPage = new AllProductsPage(driver);
        int size = allProductsPage.getProductList().size();
        int index = 3;
        if (index < size) {
            List<String> informations = addProductToCartByIndex(driver, 3);

            String price = informations.get(1);
            ProductPage productPage = new ProductPage(driver);
            productPage.goToCart();

            PurchaseProduct(driver, price);
        } else {
            throw new IndexOutOfBoundsException("Index is out of bounds: " + index + " for list of size: " + size + "!");
        }
    }


    @Test
    public void testPurchaseProductWithList() throws InterruptedException {
        WebDriver driver = getDriver();

        login(driver, "jakubszczypek4", "1234");
        AllProductsPage allProductsPage = new AllProductsPage(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Thread.sleep(2000);
        wait.withMessage("No products found on the page!")
                .until(ExpectedConditions.visibilityOfAllElements(allProductsPage.getProductList()));

        List<String> allNamesOfProducts  = allProductsPage.getProductNames();
        int size = allNamesOfProducts .size();

        List<String> selectedProductNames = new ArrayList<>();
        List<String> pricesOfProducts = new ArrayList<>();
        List<Integer> indexes = List.of(0, 1, 5);

        for (int index : indexes) {
            if (index < size) {
                List<String> informations = addProductToCartByIndex(driver, index);

                selectedProductNames.add(allNamesOfProducts.get(index));
                pricesOfProducts.add(informations.get(1));
            } else {
                System.out.println("Index " + index + " is out of bounds for list of size: " + size);
            }
        }

        if (pricesOfProducts.size() < indexes.size()) {
            throw new IllegalStateException("Not all products were added to the cart!");
        }

        allProductsPage.goToCart();

        CartPage cartPage = new CartPage(driver);
        wait.withMessage("The elements Product names in the cart were not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOfAllElements(cartPage.getProductNamesInCartElement()));

        wait.withMessage("The elements Product prices in the cart were not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOfAllElements(cartPage.getProductPricesInCartElement()));

        List<String> cartProductNames = cartPage.getProductNamesInCart();
        List<String> cartProductPrices = cartPage.getProductPricesInCart();

        // Sort lists using stream
        selectedProductNames = selectedProductNames.stream().sorted().toList();
        cartProductNames = cartProductNames.stream().sorted().toList();
        pricesOfProducts = pricesOfProducts.stream().sorted().toList();
        cartProductPrices = cartProductPrices.stream().sorted().toList();

        Assert.assertEquals(cartProductNames, selectedProductNames,
                "Product names in the cart do not match the selected products!");
        Assert.assertEquals(cartProductPrices, pricesOfProducts,
                "Product prices in the cart do not match the selected products!");

        String totalPrice = cartPage.getTotalPrice();
        int actualTotalPrice = Integer.parseInt(totalPrice);

        int expectedTotalPrice = cartProductPrices.stream()
                .mapToInt(Integer::parseInt)
                .sum();

        Assert.assertEquals(actualTotalPrice, expectedTotalPrice, "Total price mismatch!");

        PurchaseProduct(driver, totalPrice);
    }


    public void PurchaseProduct(WebDriver driver, String totalPrice) {
        CheckoutPage checkoutPage = new CheckoutPage(driver);

        CartPage cartPage = new CartPage(driver);
        cartPage.placeOrder();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.withMessage("The element Name field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getNameField()));

        wait.withMessage("The element Country field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getCountryField()));

        wait.withMessage("The element City field was not found in 10 seconds!").until(ExpectedConditions.visibilityOf(checkoutPage.getCityField()));

        wait.withMessage("The element Card field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getCardField()));

        wait.withMessage("The element Month field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getMonthField()));

        wait.withMessage("The element Year field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getYearField()));

        // Filling the purchase details
        Person person = new Person("Jakub", "Poland", "Cracow", "411111111111", "December", "2025");

//        checkoutPage.fillCheckoutDetails(person.getName(),
//                person.getCountry(),
//                person.getCity(),
//                person.getCreditCard(),
//                person.getMonth(),
//                person.getYear());

        checkoutPage.fillCheckoutDetails(person);

        wait.withMessage("The element Purchase button was not found in 10 seconds!")
                .until(ExpectedConditions.elementToBeClickable(checkoutPage.getPurchaseButton()));

        checkoutPage.completePurchase();
        wait.withMessage("The element Purchase details was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(checkoutPage.getPurchaseDetailsElement()));

        // Check if the details of purchase are okay
        assertPurchaseDetails(driver, totalPrice, person.getCreditCard(), person.getName());

        // Confirm the purchase
        checkoutPage.confirmPurchase();
    }

    public void assertPurchaseDetails(WebDriver driver, String totalPrice, String creditCard, String name) {
        CheckoutPage checkoutPage = new CheckoutPage(driver);

        // Validate the purchase summary details
        String purchaseDetails = checkoutPage.getPurchaseDetails();
        Assert.assertTrue(purchaseDetails.contains("Id:"), "Purchase details missing ID!");
        Assert.assertTrue(purchaseDetails.contains("Amount: " + totalPrice), "Purchase amount mismatch!");
        Assert.assertTrue(purchaseDetails.contains("Card Number: " + creditCard), "Credit card mismatch!");
        Assert.assertTrue(purchaseDetails.contains("Name: " + name), "Name mismatch in purchase details!");

        System.out.println("Test passed: Purchase details are correct!");
    }

    public static void login(WebDriver driver, String username, String password) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.openLoginModal();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.withMessage("The element Username field was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(loginPage.getUsernameField()));

        loginPage.login(username, password);
    }

    public static void assertLogin(WebDriver driver, String username) {
        LoginPage loginPage = new LoginPage(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.withMessage("The element Welcome text was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(loginPage.getWelcomeTextElement()));

        Assert.assertEquals(loginPage.getWelcomeText(), "Welcome " + username,
                "Login failed or welcome message is incorrect!");
    }

    public static List<String> addProductToCartByIndex(WebDriver driver, int index) throws InterruptedException {
        AllProductsPage allProductsPage = new AllProductsPage(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.withMessage("The element Product name was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(allProductsPage.getProductList().get(index)));

        ProductPage productPage = new ProductPage(driver);
        String productName = allProductsPage.getProductList().get(index).getText();
        allProductsPage.selectProductByIndex(index);

        wait.withMessage("The element Product price was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(productPage.getProductPriceElement()));

        String price = productPage.getProductPrice();
        productPage.addToCart();

        wait.withMessage("The alert was not found in 10 seconds!").until(ExpectedConditions.alertIsPresent());

        driver.switchTo().alert().accept();

        allProductsPage.goToHome();

        List<String> informations = new ArrayList<>();
        informations.add(productName);
        informations.add(price);

        return informations;
    }

    public static void assertsInCart(WebDriver driver, String productName, String price) {
        CartPage cartPage = new CartPage(driver);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.withMessage("The element Product name in the cart was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(cartPage.getProductNameInTheCartElement()));

        wait.withMessage("The element Product price in the cart was not found in 10 seconds!")
                .until(ExpectedConditions.visibilityOf(cartPage.getProductPriceInTheCartElement()));

        Assert.assertEquals(cartPage.getProductNamesInCart().get(0), productName,
                "Product name in the cart does not match the selected product!");
        Assert.assertTrue(price.contains(cartPage.getProductPricesInCart().get(0)),
                "Product price in the cart does not match the selected product!");
    }

    public static void deleteProductsInTheCartAndCheckIt(WebDriver driver) {
        CartPage cartPage = new CartPage(driver);
        cartPage.deleteAllItems();
        Assert.assertTrue(cartPage.getDeleteButtons().isEmpty(),
                "Cart is not empty after removing all items");
    }
}
