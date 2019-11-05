package com.stackOverFlow;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.List;

public class StackOverFlowTest {

    private WebDriver driver;
    private WebElement tagSearch;

    @Step("Check for a text contains {subString}")
    public static void isContainString(String elementText, String subString) {
        Assert.assertTrue(elementText.contains(subString));
    }

    @Step("Check for a question title inside a discussion")
    public static void isTitleEquals(String questionTitle, String currentTitle) {
        Assert.assertEquals(questionTitle, currentTitle);
    }

    @Step("Check for a \"{tagNameSearch}\" tag")
    public static void isTagEquals(String tagSearch, String tagNameSearch) {
        Assert.assertEquals(tagSearch, tagNameSearch);
    }

    @Step("Search for special tag")
    public static void isNeededTag(boolean isContainTag) {
        Assert.assertTrue(isContainTag);
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/Dmitriy/Documents/drivers/chromedriver78.exe");
        driver = new ChromeDriver();
    }

    @Test
    @DisplayName("Main test")
    @Description("script for stackoverflow.com")
    public void mainTest() {
        driver.get("https://stackoverflow.com");

        // ввод в поисковую строку
        WebElement searchInput = driver.findElement(By.name("q"));
        searchInput.sendKeys("webdriver");
        searchInput.submit();

        // ожидание загрузки списка результатов
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[class=\"flush-left js-search-results\"]")));

        // прохождение по каждому вопросу с проверкой на наличие "WebDriver"
        int size = driver.findElements(By.cssSelector("[class=\"question-summary search-result\"]")).size();

        int dataPosition = 1;
        WebElement element;
        for(int i = 0; i < size; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[class=\"question-summary search-result\"][data-position=\""+ dataPosition +"\"]")));
            element = driver.findElement(By.cssSelector("[class=\"question-summary search-result\"][data-position=\"" + dataPosition + "\"]"));


            if(element.getText().contains("WebDriver")) {
                isContainString(element.getText(), "WebDriver");
                // запоминаем заголовок в разделе вопросов и убираем "Q: " из заголовка вопроса
                String questionTitle = element.findElement(By.cssSelector("a[class=\"question-hyperlink\"]")).getText();
                questionTitle = questionTitle.substring(3);

                wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[class=\"question-summary search-result\"][data-position=\""+ dataPosition +"\"]")));
                // опускаемся по странице
                JavascriptExecutor jse = (JavascriptExecutor) driver;
                int height = element.getSize().height;
                jse.executeScript("window.scrollBy(0,"+ height +")");

                element.findElement(By.className("question-hyperlink")).click();

                // получаем заголовок вопроса внутри обсуждения
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[id=\"question-header\"]")));
                String currentTitle = driver.findElement(By.cssSelector("[id=\"question-header\"] h1")).getText();

                isTitleEquals(questionTitle, currentTitle);
                driver.navigate().back();
            }
            dataPosition++;
        }

        // переход в раздел Tags
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[id=\"nav-tags\"]")));
        driver.findElement(By.id("nav-tags")).click();

        // обновляем список тэгов по запросу
        WebElement tagFilter = driver.findElement(By.id("tagfilter"));
        String tagNameSearch = "webdriver";
        tagFilter.sendKeys(tagNameSearch);
        wait.until(ExpectedConditions.textToBe(By.className("post-tag"), tagNameSearch));

        // проходим по каждому тэгу
        List<WebElement> tags = driver.findElements(By.cssSelector("[class=\"post-tag\"]"));

        for(WebElement tag : tags) {
            if(tag.getText().contains(tagNameSearch)) {
                isContainString(tag.getText(), tagNameSearch);
                if(tag.getText().equals(tagNameSearch))
                    tagSearch = tag;
            }
        }

        isTagEquals(tagSearch.getText(), tagNameSearch);

        // переходим по тэгу и просматриваем вопросы с пометкой webdriver
        tagSearch.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("questions")));
        List<WebElement> questions = driver.findElement(By.id("questions")).findElements(By.cssSelector("[class=\"summary\"]"));

        for(WebElement question : questions) {
            List<WebElement> tagList = question.findElements(By.className("post-tag"));
            boolean isContainTag = false;
            for (WebElement tag : tagList) {
                if (tag.getText().equals(tagNameSearch)) {
                    isContainTag = true;
                }
            }
            isNeededTag(isContainTag);
            isContainTag = false;
        }
    }

    @After
    public void close() {
        driver.quit();
    }

}
