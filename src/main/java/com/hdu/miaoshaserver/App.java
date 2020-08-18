package com.hdu.miaoshaserver;

import com.hdu.miaoshaserver.dao.UserDOMapper;
import com.hdu.miaoshaserver.dataobject.UserDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
//@Controller
//@ResponseBody
@RestController
@SpringBootApplication(scanBasePackages = {"com.hdu.miaoshaserver"})
@MapperScan("com/hdu/miaoshaserver/dao")
public class App {
    @Autowired
    private UserDOMapper userDOMapper;

    public static void main(String[] args) {
        System.out.println("Hello World!");
        SpringApplication.run(App.class, args);
    }

    @RequestMapping("/")
    public String home() {

        UserDO userDo = userDOMapper.selectByPrimaryKey(1);
        if (userDo == null) {
            return "用户对象不存在";
        } else return userDo.getName();

    }
}
