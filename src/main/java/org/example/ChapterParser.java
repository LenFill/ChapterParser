package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ChapterParser {
    public static String buildChapterUrl(
            String url, int volume, int chapter) {
        return url.replaceFirst(
                "/read/v\\d+/c\\d+",
                "/read/v" + volume + "/c" + chapter
        );
    }
    public static String parseChapters(
            String url, int volume, int firstChapter, int lastChapter) {
        if (volume < 1 || firstChapter < 1 || lastChapter < firstChapter) {
            throw new IllegalArgumentException("Проверьте том и диапазон глав.");
        }
        StringBuilder result = new StringBuilder();
        WebDriver driver = new ChromeDriver();
        try {
            for (int chapter = firstChapter; chapter <= lastChapter; chapter++) {
                String chapterUrl = buildChapterUrl(url, volume, chapter);
                String text = parseChapter(driver, chapterUrl);

                result.append("Том ").append(volume)
                        .append(". Глава ").append(chapter)
                        .append(System.lineSeparator())
                        .append(System.lineSeparator())
                        .append(text)
                        .append(System.lineSeparator());
                if (chapter < lastChapter) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Загрузка прервана.", e);
                    }
                }
            }
            return result.toString();
        } finally {
            driver.quit();
        }
    }
    private static String parseChapter(WebDriver driver, String url) {
        By paragraphs = By.cssSelector(
                "main[data-reader-content] p.node-paragraph"
        );
        driver.get(url);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(d -> !d.findElements(paragraphs).isEmpty());
        List<WebElement> found = driver.findElements(paragraphs);
        StringBuilder chapterText = new StringBuilder();

        for (WebElement paragraph : found) {
            String text = paragraph.getText();
            if (!text.isBlank()) {
                chapterText.append(text).append(System.lineSeparator());
            }
        }
        return chapterText.toString();
    }
}
