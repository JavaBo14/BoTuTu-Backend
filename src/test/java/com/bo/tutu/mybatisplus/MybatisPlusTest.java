package com.bo.tutu.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bo.tutu.model.entity.User;
import com.bo.tutu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@SpringBootTest
public class MybatisPlusTest {
    @Resource
    private  UserService userService;
    @Test
    public void testPageQuery() {
        IPage<User> page = new Page<>(1, 10);
        IPage<User> userPage = userService.page(page);
        System.out.println("userpage"+userPage);
    }
    //Page IPage
    @Test
    public void testPageQuerytwo(){
        // 假设有一个 QueryWrapper 对象，设置查询条件为 age > 25，进行有条件的分页查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount","bobo");
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        System.out.println("userPage " + userPage);
    }
     @Test
    public void userList(){
         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         List<User> list = userService.list(queryWrapper);
         System.out.println(list);
     }


}
