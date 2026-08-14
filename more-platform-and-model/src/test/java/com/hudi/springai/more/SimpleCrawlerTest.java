package com.hudi.springai.more;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 最小化示例：
 * 1. 请求网页
 * 2. 提取标题
 * 3. 提取前几条链接
 */
public class SimpleCrawlerTest {

    private static final Pattern TITLE_PATTERN =
            Pattern.compile("<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern LINK_PATTERN =
            Pattern.compile("<a[^>]+href=[\"']([^\"'#]+)[\"'][^>]*>(.*?)</a>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Test
    void crawlExampleDotCom() throws IOException, InterruptedException {
        CrawlResult result = crawl("https://www.baidu.com/", 5);

        System.out.println("页面标题: " + result.title());
        for (String link : result.links()) {
            System.out.println("链接: " + link);
        }
    }

    public CrawlResult crawl(String url, int maxLinks) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0 SimpleCrawlerTest")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String html = response.body();

        String title = extractTitle(html);
        List<String> links = extractLinks(url, html, maxLinks);
        return new CrawlResult(title, links);
    }

    private String extractTitle(String html) {
        Matcher matcher = TITLE_PATTERN.matcher(html);
        if (!matcher.find()) {
            return "未找到标题";
        }
        return cleanText(matcher.group(1));
    }

    private List<String> extractLinks(String pageUrl, String html, int maxLinks) {
        Matcher matcher = LINK_PATTERN.matcher(html);
        Set<String> links = new LinkedHashSet<>();
        while (matcher.find() && links.size() < maxLinks) {
            String href = matcher.group(1).trim();
            if (href.isEmpty() || href.startsWith("javascript:") || href.startsWith("mailto:")) {
                continue;
            }
            links.add(resolveUrl(pageUrl, href));
        }
        return new ArrayList<>(links);
    }

    private String resolveUrl(String pageUrl, String href) {
        try {
            return new URI(pageUrl).resolve(href).toString();
        } catch (URISyntaxException e) {
            return href;
        }
    }

    private String cleanText(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    record CrawlResult(String title, List<String> links) {
    }
}
