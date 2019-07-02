package top.wmjgm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author: hanne hll941106@163.com
 * @date: 2019-06-29 14:43
 **/
@SpringBootApplication
@EnableCaching
public class SliderVerificationCodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(SliderVerificationCodeApplication.class, args);
    }
}
