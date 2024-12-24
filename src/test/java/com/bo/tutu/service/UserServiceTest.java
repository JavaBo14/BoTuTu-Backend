package com.bo.tutu.service;

import javax.annotation.Resource;

import cn.hutool.json.JSONUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * 用户服务测试
 *
 * @author <a href="https://github.com/JavaBo14">Bo</a>
 * @from <a href="https://github.com/JavaBo14/BoTuTu-Backend">
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "123456";
        try {
            long result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
            userAccount = "yu";
            result = userService.userRegister(userAccount, userPassword, checkPassword);
            Assertions.assertEquals(-1, result);
        } catch (Exception e) {

        }
    }
    @Test
    void jsonTest() {
        // 模拟pictureUpdateRequest.getTags()返回的列表
        List<String> tagsList = new ArrayList<>();
        tagsList.add("tag1");
        tagsList.add("tag2");

        System.out.println("tagsList: " + tagsList);

        String jsonStr = JSONUtil.toJsonStr(tagsList);
        System.out.println("jsonStr: " + jsonStr);
    }

    @Test
    void setTest(){
        Set<String> stringSet = new HashSet<>();
        // 向Set集合中添加元素
        stringSet.add("apple");
        stringSet.add("banana");
        stringSet.add("apple"); // 尝试添加重复元素
        // 输出Set集合中的元素
        System.out.println(stringSet);
    }

    @Test
    void mapTest(){
        Map<String, Integer> stringIntegerMap = new HashMap<>();
        // 向Map集合中添加键值对
        stringIntegerMap.put("apple", 5);
        stringIntegerMap.put("banana", 10);
        // 输出Map集合中的键值对
        System.out.println(stringIntegerMap);
    }
}

