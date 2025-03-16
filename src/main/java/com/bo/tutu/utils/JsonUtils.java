package com.bo.tutu.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * JSON工具类，基于Jackson实现
 * 提供JSON字符串与Java对象之间的转换功能
 *
 * @author JavaBo14
 * @since 2025-02-28
 */
public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    /**
     * ObjectMapper实例
     * Jackson的主要入口点，用于进行所有JSON操作
     */
    private static final ObjectMapper objectMapper;

    /**
     * 静态初始化块
     * 配置ObjectMapper的各种特性
     */
    static {
        objectMapper = new ObjectMapper();
        // 1. 通用配置
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);  // 不序列化null值
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);  // 允许序列化空对象
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);  // 忽略未知属性
        // 2. 日期时间处理
        objectMapper.registerModule(new JavaTimeModule());  // 注册Java 8日期时间模块
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 日期序列化为ISO格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));  // 设置日期格式
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));  // 设置时区
        // 3. 格式化配置
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // 启用格式化输出
        objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);  // 不转义非ASCII字符
    }

    /**
     * 私有构造函数，防止实例化
     */
    private JsonUtils() {
        throw new AssertionError("No JsonUtils instances for you!");
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     * @throws RuntimeException 如果序列化过程中发生错误
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("序列化对象为JSON失败: {}", e.getMessage());
            throw new RuntimeException("序列化对象为JSON失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 如果反序列化过程中发生错误
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON失败: {}", e.getMessage());
            throw new RuntimeException("反序列化JSON失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为复杂泛型对象
     *
     * @param json JSON字符串
     * @param typeReference 类型引用，例如：new TypeReference<List<User>>() {}
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 如果反序列化过程中发生错误
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON失败: {}", e.getMessage());
            throw new RuntimeException("反序列化JSON失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为List集合
     *
     * @param json JSON字符串
     * @param elemType 集合元素类型
     * @param <T> 泛型类型
     * @return 反序列化后的List集合
     * @throws RuntimeException 如果反序列化过程中发生错误
     */
    public static <T> List<T> fromJsonList(String json, Class<T> elemType) {
        CollectionType listType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elemType);
        try {
            return objectMapper.readValue(json, listType);
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON到List失败: {}", e.getMessage());
            throw new RuntimeException("反序列化JSON到List失败", e);
        }
    }

    /**
     * 将JSON字符串反序列化为自定义泛型类型
     *
     * @param json JSON字符串
     * @param rawClass 原始类型
     * @param paramClasses 泛型参数类型
     * @param <T> 泛型类型
     * @return 反序列化后的对象
     * @throws RuntimeException 如果反序列化过程中发生错误
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromJson(String json, Class<?> rawClass, Class<?>... paramClasses) {
        JavaType type = objectMapper.getTypeFactory()
                .constructParametricType(rawClass, paramClasses);
        try {
            return (T) objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON失败: {}", e.getMessage());
            throw new RuntimeException("反序列化JSON失败", e);
        }
    }

    /**
     * 将对象转换为格式化的JSON字符串（美化输出）
     *
     * @param object 要序列化的对象
     * @return 格式化的JSON字符串
     * @throws RuntimeException 如果序列化过程中发生错误
     */
    public static String toPrettyJson(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("序列化对象为格式化JSON失败: {}", e.getMessage());
            throw new RuntimeException("序列化对象为格式化JSON失败", e);
        }
    }

    /**
     * 将对象转换为字节数组
     *
     * @param object 要序列化的对象
     * @return 字节数组
     * @throws RuntimeException 如果序列化过程中发生错误
     */
    public static byte[] toJsonBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("序列化对象为字节数组失败: {}", e.getMessage());
            throw new RuntimeException("序列化对象为字节数组失败", e);
        }
    }

    /**
     * 将对象转换为另一个类型的对象
     *
     * @param fromValue 源对象
     * @param toValueType 目标类型
     * @param <T> 目标类型泛型
     * @return 转换后的对象
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    /**
     * 获取配置好的ObjectMapper实例
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}