package io.javabrains.moviecatalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication // this to tell spring that 'this is an springboot application'
@EnableEurekaClient
public class MovieCatalogServiceApplication {

	/* 向Spring Container声明一个Spring Bean */
    // 手段：@Bean + 返回实例的方法
    // 特征：一次声明，到处使用
	@Bean
    @LoadBalanced // do service discovery Ⅰ   作用：告诉 Eureka Library ，不要直接去查找我提供的URL。而是做hops去 discovery service
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}

	@Bean
	public WebClient.Builder getWebClientBuilder() {
		return WebClient.builder();
	}

	// since we chose an web dependencies, we run this, an Tomcat instance will be running
	// but we haven't config any URI and related API, so there's nothing to show.
	// Spring then try to find an error page, which is also not exist.
	// so it gives back an error page to show no prepared error page find.
	public static void main(String[] args) {
		SpringApplication.run(MovieCatalogServiceApplication.class, args);
	}

}
