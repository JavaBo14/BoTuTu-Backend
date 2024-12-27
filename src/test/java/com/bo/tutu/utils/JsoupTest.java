package com.bo.tutu.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
public class JsoupTest {
    @Test
    public void crawlTest(){
        try {
            // 1. 指定关键词和图片数量
            String keyword = "熊猫";
            int maxImages = 10;
            // 2. 构造 Bing 图片搜索 URL
            String searchUrl = buildSearchUrl(keyword);
            System.out.println("搜索 URL: " + searchUrl);
            // 3. 获取 Bing 图片搜索页面的 HTML
            Document document = Jsoup.connect(searchUrl).get();
            // 4. 提取图片的 URL
            Elements imgElements = document.select("a.iusc"); // 获取含图片的节点
            List<String> imageUrls = new ArrayList<>();
            for (Element imgElement : imgElements) {
                String metadata = imgElement.attr("m");
                String imgUrl = extractImageUrl(metadata);
                if (imgUrl != null) {
                    imageUrls.add(imgUrl);
                }
                if (imageUrls.size() >= maxImages) {
                    break; // 最多获取指定数量图片
                }
            }
            // 5. 输出结果
            System.out.println("抓取到的图片 URL：");
            imageUrls.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 构建 Bing 图片搜索 URL
    private static String buildSearchUrl(String keyword) throws UnsupportedEncodingException {
        String baseUrl = "https://cn.bing.com/images/search?q=";
        return baseUrl + URLEncoder.encode(keyword, "UTF-8");
    }
    // 从 metadata 中提取图片 URL
    private static String extractImageUrl(String metadata) {
        try {
            // 示例 metadata: {"murl":"https://image_url.jpg","turl":"thumbnail_url"}
            int start = metadata.indexOf("\"murl\":\"") + 8;
            int end = metadata.indexOf("\"", start);
            return metadata.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
