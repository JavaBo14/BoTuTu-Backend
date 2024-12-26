package com.bo.tutu.mybatisplus;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bo.tutu.mapper.UserMapper;
import com.bo.tutu.model.entity.User;
import com.bo.tutu.model.vo.UserVO;
import com.bo.tutu.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
public class MybatisPlusTest {
    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @Test
    public void testPageQuery() {
        IPage<User> page = new Page<>(1, 10);
        IPage<User> userPage = userService.page(page);
        System.out.println("userpage" + userPage);
    }

    //Page IPage
    @Test
    public void testPageQuerytwo() {
        // 假设有一个 QueryWrapper 对象，设置查询条件为 age > 25，进行有条件的分页查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", "bobo");
        Page<User> userPage = userService.page(new Page<>(1, 10), queryWrapper);
        System.out.println("userPage " + userPage);
    }

    @Test
    public void userList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> list = userService.list(queryWrapper);
        System.out.println(list);
    }

    @Test
    public void userNameList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("age", 25);
        List<Object> userIds = userMapper.selectObjs(queryWrapper); // 调用 selectObjs 方法
        for (Object userId : userIds) {
            System.out.println("User ID: " + userId);
        }
    }

    @Test
    public void selectUsernames() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("userAccount");
        // 通过调用父类的selectMaps方法执行查询，返回结果封装为Map集合列表
        List<Map<String, Object>> maps = userMapper.selectMaps(queryWrapper);
        System.out.println(maps);
    }
    @Test
    // 查询用户名字列并返回List<String>
    public void selectUsernamesAsList() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("userAccount");
        // 先获取Object类型的结果列表
        List<Object> objects = userMapper.selectObjs(queryWrapper);
        // 转换为List<String>
        List<String> userAccount = objects.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        System.out.println(userAccount);
    }

    @Test
    public void selectUsernameAndAge() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "userAccount");
        // 调用 selectMaps 方法，返回指定列的结果
        List<Map<String, Object>> result = userMapper.selectMaps(queryWrapper);
        // 将 Map 转换为 UserVo 对象列表
        List<UserVO> userVoList = result.stream()
                .map(map -> {
                    UserVO userVo = new UserVO();
                    userVo.setId(((Number) map.get("id")).longValue()); // 确保类型安全
                    userVo.setUserName((String) map.get("userAccount"));
                    return userVo;
                })
                .collect(Collectors.toList());
        // 输出结果，验证是否正确
        userVoList.forEach(System.out::println);
    }
}
