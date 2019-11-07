package com.stackOverFlow;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class StackOverFlowTest {

    private WebDriver driver;
    private WebElement tagSearch;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Step("Проверка текущего URL")
    private static void checkCurrentURL(String currentURL, String neededURL) {
        Assert.assertEquals("Текущий URL не соответствует необходимому", currentURL, neededURL);
    }

    @Step("Проверка эквивалентности введенного слова с \"{searchWord}\"")
    private static void checkContainSearchWord(String searchInput, String searchWord) {
        Assert.assertEquals("Поисковое слово не соответствует введенным данным", searchInput, searchWord);
    }

    @Step("Проверка на наличие слова \"WebDriver\" в превью вопроса")
    private static void checkWordInPreviewTextQuestion(String previewTextQuestion) {
        Assert.assertTrue("В превью вопроса не найдено слово \"WebDriver\"", previewTextQuestion.contains("WebDriver"));
    }

    @Step("Проверка на эквивалентность заголовка внутри вопроса и заголовка в превью вопроса")
    private static void isTitleEquals(String questionTitle, String currentTitleInside) {
        Assert.assertEquals("Заголовки в превью и самом вопросе различаются", questionTitle, currentTitleInside);
    }

    @Step("Проверка эквивалентности тэга \"{tagSearch}\" с тэгом\"{tagNameSearch}\"")
    private static void isTagEquals(String tagSearch, String tagNameSearch) {
        Assert.assertEquals("Поисковой тэг не соответствует введенным данным", tagSearch, tagNameSearch);
    }

    @Step("Поиск по точному совпадению тэга \"{tagNameSearch}\"")
    private static void isNeededTag(List<String> tagList, String tagNameSearch) {
        Assert.assertTrue("Не удалось найти тэг", tagList.contains(tagNameSearch));
    }

    @Step("Проверка содержания слова \"{tagNameSearch}\" в {tag}")
    private static void isContainTag(String tag, String tagNameSearch) {
        Assert.assertTrue("Искомое слово не содержится в тэге", tag.contains(tagNameSearch));
    }

    @Before
    public void setUp() {
        System.setProperty("", "");
        driver = new ChromeDriver();
    }

    @Test
    @DisplayName("Main test")
    @Description("script for stackoverflow.com")
    public void mainTest() {
        driver.get("https://stackoverflow.com");
        // проверка 1 шага
        try {
            checkCurrentURL(driver.getCurrentUrl(), "https://stackoverflow.com/");
        }catch (AssertionError e) {
            collector.addError(e);
        }

        // ввод в поисковую строку
        WebElement searchInput = driver.findElement(By.name("q"));
        searchInput.sendKeys("webdriver");
        // проверка 2 шага
        try {
            checkContainSearchWord(searchInput.getAttribute("value"), "webdriver");
        }catch (AssertionError e) {
            collector.addError(e);
        }
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
            // проверка шага 3
            try {
                checkWordInPreviewTextQuestion(element.getText());
            }catch (AssertionError e) {
                collector.addError(e);
            }

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
            String currentTitleInside = driver.findElement(By.cssSelector("[id=\"question-header\"] h1")).getText();
            // проверка шага 4
            try {
                isTitleEquals(questionTitle, currentTitleInside);
            }catch (AssertionError e) {
                collector.addError(e);
            }
            driver.navigate().back();
            dataPosition++;
        }

        // переход в раздел Tags
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[id=\"nav-tags\"]")));
        driver.findElement(By.id("nav-tags")).click();
        // проверка шага 5
        try {
            checkCurrentURL(driver.getCurrentUrl(), "https://stackoverflow.com/tags");
        }catch (AssertionError e) {
            collector.addError(e);
        }

        // обновляем список тэгов по запросу
        WebElement tagFilter = driver.findElement(By.id("tagfilter"));
        String tagNameSearch = "webdriver";
        tagFilter.sendKeys(tagNameSearch);
        wait.until(ExpectedConditions.textToBe(By.className("post-tag"), tagNameSearch));

        // проходим по каждому тэгу
        List<WebElement> tags = driver.findElements(By.cssSelector("[class=\"post-tag\"]"));

        for(WebElement tag : tags) {
            // проверка шага 6
            try {
                isContainTag(tag.getText(), tagNameSearch);
            }catch (AssertionError e) {
                collector.addError(e);
            }
            if(tag.getText().equals(tagNameSearch)) {
                tagSearch = tag;
            }
        }
        try {
            isTagEquals(tagSearch.getText(), tagNameSearch);
        }catch (AssertionError e) {
            collector.addError(e);
        }

        // переходим по тэгу и просматриваем вопросы с пометкой webdriver
        tagSearch.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("questions")));
        List<WebElement> questions = driver.findElement(By.id("questions")).findElements(By.cssSelector("[class=\"summary\"]"));

        for(WebElement question : questions) {
            List<WebElement> tagList = question.findElements(By.className("post-tag"));
            // проверка шага 7
            List<String> tagStringList = new ArrayList<>(tagList.size());
            for(WebElement tag : tagList) {
                tagStringList.add(String.valueOf(tag.getText()));
            }
            try {
                isNeededTag(tagStringList, tagNameSearch);
            }catch (AssertionError e) {
                collector.addError(e);
            }
        }
    }

    @After
    public void close() {
        driver.quit();
    }

}
