package io.javabrains.movieinfoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableEurekaClient
public class MovieInfoServiceApplication {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


	public static void main(String[] args) {
		SpringApplication.run(MovieInfoServiceApplication.class, args);
	}

}
/*
启示：
1 把一个SpringBoot项目生命成为 Eureka Client不需要编写代码，只需要在 pom.xml中引入依赖，并添加 dependenciesManagement即可
2 在 application.properties文件中可以指定 client的名称————方便在 Eureka Server的dashboard上找出谁是谁
3 为了让程序员能够更加清楚这是一个Eureka Client，可以在启动类上添加注解 @EnableEurekaClient
    Note 但这不是强制要求的

client 要怎么知道到哪儿(server)去注册自己？
机制/原理：
    1 尝试访问(look up)默认的server端口，如果找到了server，就注册自己；
    2 可以在 application.properties属性文件中 配置server的具体位置

 */