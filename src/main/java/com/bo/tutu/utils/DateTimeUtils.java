package com.bo.tutu.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 时间工具类
 */
public class DateTimeUtils {
    // 默认格式化器（线程安全）
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 默认时区（可配置）
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    // 获取当前时间（默认格式和时区）
    public static String getCurrentTime() {
        return LocalDateTime.now(DEFAULT_ZONE).format(DEFAULT_FORMATTER);
    }
    // 自定义格式
    public static String getCurrentTime(String pattern) {
        return LocalDateTime.now(DEFAULT_ZONE)
            .format(DateTimeFormatter.ofPattern(pattern));
    }
    // 自定义时区
    public static String getCurrentTime(ZoneId zone) {
        return LocalDateTime.now(zone).format(DEFAULT_FORMATTER);
    }
    // 完全自定义
    public static String getCurrentTime(String pattern, ZoneId zone) {
        return LocalDateTime.now(zone)
            .format(DateTimeFormatter.ofPattern(pattern));
    }
}