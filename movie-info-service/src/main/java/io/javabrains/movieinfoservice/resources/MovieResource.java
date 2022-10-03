package io.javabrains.movieinfoservice.resources;

import io.javabrains.movieinfoservice.models.Movie;
import io.javabrains.movieinfoservice.models.MovieSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/movies")
public class MovieResource {

    // get the api-key from properties file
    // 手段：@Value(${<config-key>}) // get config-value
    @Value("${api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/{movieId}")
    public Movie getMovieInfo(@PathVariable("movieId") String movieId) {

        /* 演示视频中的TMBD网站访问404😳 只好找豆瓣电影的API了 */
//        MovieSummary movieSummary = restTemplate.getForObject(
//                "https://www.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey,
//                MovieSummary.class
//        );

//        return new Movie(movieId, movieSummary.getTitle(), movieSummary.getOverview());

        return new Movie("001", "蝙蝠侠大战超人");
    }
}

/*
启示：
#Q Eureka Client要如何找到该把自己注册到什么地方？
答：直接在配置文件中声明即可，如果配置文件中没有声明的话。就会尝试使用默认的 host + port
 */

/*
启示：
1 如果不主动执行 mvn install 命令的话，jar文件不会自己生成（即便这时候已经有了编译后的 target/文件夹）
2 jar文件生成在什么地方？
    一边是target/目录的最外层子文件夹

3 如何运行这个jar文件？
    Ⅰ 进入到 target/目录下；
    Ⅱ java -Dserver.port=8206 -jar movie-info-service-0.0.1-SNAPSHOT.jar
 */

/*
启示：
@LoadBalanced注解的作用：；
    1 执行服务发现的工作；
    2 实现caller端(restTemplate)的负载均衡
#Q 这种caller端实现的负载均衡是有效的吗？
答：未必，因为如果存在多个caller客户端使用同样的 负载均衡策略，可能会导致 请求最终都跑到了一台service instance上面

Next:可以把 可用的service的nameList放到属性文件中，以避免代码中的硬编码；但这并不是一个优雅的解决方案————我们想要更多可编程的控制方式
什么叫做“可编程的控制方式”？
aka 在类型中，有一个具体的实例，程序员能够在此实例上调用这种方法完成特定的操作；
答：
    1 Eureka library提供了一个类型(这个类型会在项目启动时，被自动注入容器中)
    2 在需要的类型中，使用 @Autowired注入 DiscoveryClient的实例；
    3 尽情使用APIs来做各种操作
Note 不推荐这样做，除非 you know what you are doing~
 */

/*
Next: How fault tolerance works? 容错机制

现有的工作流程/机制：
1 存在多个 Eureka Client，它们在启动后会向 Eureka Server来注册自己；
2 Eureka Server上存储有所有注册的“声明自己可用的” services；
3 caller向 Eureka Server发起请求，问“有没有买煎饼的店铺”？
4 caller得到了“煎饼店铺的具体地址”，然后去买煎饼

但是，如果“服务在注册后死掉了呢？”
    煎饼店入驻美团后，由于资金链断裂，商铺倒闭...
这时候如果 Eureka Server照旧向 caller返回 煎饼店的地址信息，caller照着这个信息是买不到煎饼的...

问题转化：Eureka Server如何能够感知到 Eureka Client在注册后是否仍旧在正常工作？
手段：
    Eureka client间歇而持续地向 服务注册中心(Eureka Server)发送“心跳”
Note：这个特性也已经有Eureka Library内置实现了

追问：如果Eureka Server 直接挂掉了怎么办？
答：这时候caller从 Server就会获取到error，然后从缓存中来读取信息；
Note 从缓存中获取得到的信息可能是过时的，但这是当下最好的结果(好歹有些东西能给caller)
Note 这个特性也是由Eureka 内置实现的，不需要程序员做任何工作

 */

/*
Recap总结回顾：
1 创建一堆的services；由于它们都提供单一的功能，所以叫做 microservices;
2 想办法让这些个服务能够相互通信/调用；
3 想办法使用 service discovery的概念来避免 URL的硬编码；
    在 restTemplate上添加 @LoadBalanced

Level1完结撒花~~~
 */
