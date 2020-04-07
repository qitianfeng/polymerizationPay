package com.mp.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user")
public class User {
    //自增
    @TableId(value = "ID",type = IdType.AUTO)
    private Long id;
    private String username;//驼峰命名,则无需注解
    private String password;
    private String name;
    private Integer age;
    private String email;
    private LocalDateTime birthday;
}