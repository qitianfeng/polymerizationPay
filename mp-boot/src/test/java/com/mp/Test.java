package com.mp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.mp.bean.User;
import com.mp.mapper.UserMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jws.soap.SOAPBinding;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {
    @Autowired
    UserMapper userMapper;

    //查询所有
    @org.junit.Test
    public void test() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            System.out.println(user);
        }
    }

    // 通过写xml SQL语句查询
    @org.junit.Test
    public void testById() {
        User byId = userMapper.findById(1L);
        System.out.println(byId);
    }
    /***
     * 通用CRUD
     */
    //插入数据
    @org.junit.Test
    public void testInsert() {
        User user = new User();
        user.setAge(20);
        user.setEmail("test@itcast.cn");
        user.setName("曹操");
        user.setUsername("caocao");
        user.setPassword("123456");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy‐MM‐dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse("1990‐01‐01 00:00:00", df);
        user.setBirthday(localDateTime);
        int insert = userMapper.insert(user);
        System.out.println(insert);
        System.out.println(user.getId());
    }

    //根据id修改，有设置的修改，没有的不动
    @org.junit.Test
    public void testUpdate() {
        User user = new User();
        user.setAge(21);
        user.setId(Long.parseLong("1247466256110575618"));
        int i = userMapper.updateById(user);
    }

    @org.junit.Test
    public void testUpdate1() {
        User user = new User();
        user.setAge(18);
        //更新的条件
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("name", "张三");
        //可以设置多个条件

        int update = userMapper.update(user, wrapper);

    }

    @org.junit.Test
    public void testUpdate2() {
        //更新的条件
        UpdateWrapper<User> wrapper = new UpdateWrapper<>();
        wrapper.eq("name", "张三").set("age", 19);
        //可以设置多个条件

        int update = userMapper.update(null, wrapper);

    }

    @org.junit.Test
    public void testDelete() {
        int i = userMapper.deleteById(4L);

    }

    @org.junit.Test
    public void testDelete1() {
        User user = new User();
        user.setName("曹操");

        //将实体类进行包装
        QueryWrapper<User> wrapper = new QueryWrapper<>(user);
        int delete = userMapper.delete(wrapper);
    }

    //批量删除
    @org.junit.Test
    public void testDelete2() {
        User user = new User();
        user.setName("曹操");

        //将实体类进行包装
        int delete = userMapper.deleteBatchIds(Arrays.asList(1L, 2L, 3L, 4L));

    }

    //查询
    @org.junit.Test
    public void testSelect() {
        User user = new User();
        user.setName("曹操");

        //将实体类进行包装

        User user1 = userMapper.selectById(1L);
        //根据条件查询
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>(user);
        userMapper.selectOne(userQueryWrapper);

        //根据id批量查询
        List<User> users = userMapper.selectBatchIds(Arrays.asList(2L, 3L));

        //参数1 当前页码，小于1按一计算，参数二：每页的记录数
        Page<User> userPage = new Page<>(1, 1);

        IPage<User> userIPage = userMapper.selectPage(userPage, userQueryWrapper);
        System.out.println("数据总条数：" + userIPage.getTotal());
        System.out.println("总页数：" + userIPage.getPages());

        //取出分页记录
        List<User> records = userIPage.getRecords();


        //模糊查询

        QueryWrapper<User> name = userQueryWrapper.like("name", "1");
        // name like 1%
        userQueryWrapper.likeRight("name",1);

        userQueryWrapper.likeLeft("name",1);


    }


    @org.junit.Test
    public void testWrpper() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        //SELECT id,user_name,password,name,age,email FROM tb_user WHERE password = ? AND age >=? AND name IN (?,?,?)
        wrapper.eq("password", "123456")
                .ge("age", 20)
                .in("name", "李四", "王五", "赵六");
        List<User> users = userMapper.selectList(wrapper);

        LambdaQueryWrapper<User> wrapper1 = new LambdaQueryWrapper<>();
        ///lambada表达式
        wrapper1.eq(User::getPassword, "123456")
                .ge(User::getAge, 20)
                .in(User::getName, "李四", "王五", "赵六");

    }


}
