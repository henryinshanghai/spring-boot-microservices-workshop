package io.javabrains.moviecatalogservice.resources;

import io.javabrains.moviecatalogservice.models.CatalogItem;
import io.javabrains.moviecatalogservice.models.Movie;
import io.javabrains.moviecatalogservice.models.Rating;
import io.javabrains.moviecatalogservice.models.UserRating;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// a controller is an REST resources, since the web browser going to search for it
@RestController // tell spring that this is an controller class
@RequestMapping("/catalog") // tell spring to mapping the URI to this class
public class MovieCatalogResource {

    /* this is a wrong place to write stream codes. */

    // reference the singleton in spring container
    @Autowired // ask for a bean from Spring Container
    private RestTemplate restTemplate;

    @Autowired
    private WebClient.Builder webClientBuilder;


    // 根据传入的userId，返回用户所有评分过的电影 & 电影详情
    @RequestMapping("/{userId}") // 以一个变量的方式来接收 请求URL中的字符串
    //     👇👇👇  返回是Mono(所以需要等待 .block()) 这里是刻意的行为
    public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {

        // 发起Rest 请求
//        RestTemplate restTemplate = new RestTemplate();

        /*
            tell him what to do now, and what to do when things get completed.
            since I've arrange everything up, I'm gonna do something else instead of waiting around here.
         */


        /* get all rated movie IDs */
        // hard-coded way
//        List<Rating> ratings = Arrays.asList(
//                new Rating("1234", 4),
//                new Rating("5678", 4)
//        );

        // get movieId info from a REST call
        /*
            here is a little hiccup, what to pass on the second position?
            1 you can not pass List.class since it is a generic class,
            2 nor you can pass Rating since it's a solo object.
            解决手段：ParameterizedType;
            用法：
                1 创建是一个 ParameterizedType的实例；
                2 向该实例中传入 需要返回的对象；
            语法： ParameterizedTypeReference<ResponseWrapper<T>>() {}
            但这并不是一种好的方式，我想要传入一个 single Class（而不是用这么复杂的语法实现封装）。

         */
//        UserRating ratings = restTemplate.getForObject("http://localhost:8083/ratingsdata/user/" + userId,
        // do service discovery Ⅱ
        // 这里的URL中不再硬编码 域名、端口号，而是使用在Eureka Server上注册的服务名称代替 域名 + 端口号
        // Note：restTemplate won't take this string literally. it will call Eureka instead
        // restTemplate会把 “xxx” 解析成为 Eureka client的名字，请求Eureka Server得到真正的 host + port，然后再次向目标地址发起请求
        UserRating ratings = restTemplate.getForObject("http://rating-data-Service/ratingsdata/user/" + userId,
                UserRating.class); // 😳 ain't the service / client name still hard-coding?

        // 必须在列表对象上把它转化成为流对象
        return ratings.getRatings().stream().map(rating -> { // use certain method to iterate each item in list
            // For each movie Id, call movie info service and get details
                    /* send an HTTP request, and seal the return string into an object via RestTemplate */
                    // this will wait for restTemplate give me the result I expected          localhost:8082
                    Movie movie = restTemplate.getForObject("http://movie-info-Service/movies/" + rating.getMovieId() ,Movie.class); // use RestTemplate

            // put them all together
            // use the return result to instantiate Movie
            // By now, I know for sure that I already have this result I need
            return new CatalogItem(movie.getMovieName(), "Desc", rating.getRating());
        })
                .collect(Collectors.toList()); // use certain method to wrap things up



        /* return a hard-coded information(this is the oldest version) */
//        return Collections.singletonList(
//                new CatalogItem("Transformers", "Test", 4)
//        );
    }

}
/*
在代码中发起HTTP请求：
    使用RestTemplate类型；

在代码中使用Java8 Stream：
    1 转化为stream流；
    2 使用特定方法对流中的每一个item进行迭代(以遍历)；
    3 对每个item进行逻辑处理；
    4 使用特定的方法来收集处理结果(wrap it up)

It wouldn't be too weird, as long as it works?!

回顾代码中的异味：
1 硬编码请求的URL；
2 在方法中实例化 RestTemplate；
    这是一种浪费，应该是只有一个实例，然后用于多个请求
    在SpringBoot中如何创建单例对象，并能够到处被其他类型所使用？
    答：创建一个Spring Bean；
    具体方法：
        1 在启动类中创建一个方法； // 这个方法就只会执行一次
        2 在方法头上添加@Bean;
        3 在方法体中返回一个 RestTemplate的实例；

        ---
        使用时，在需要的类型中，用@Autowired来把实例注入到一个成员变量上？
        @Autowire是消费者，而@Bean是一个生产者

    问：Bean是什么时候被创建的？
    答：这个是可以进行配置的。可以懒加载,也可以预先加载

    问：@Bean方法必须要在启动类中吗？
    答：不一定，只要在classpath中就可以了

    the name doesn't matter, but the Type matter.

    如果两个bean有相同类型的话，会有一个error。因为Spring不知道该要实例化/注入哪一类型的实例
 */

/* send an HTTP request via WebClient(this is asynchronous) */
//            Movie movie = webClientBuilder.build() // use the builder pattern to get a builder
//                    .get()
//                    .uri("http://localhost:8082/movies/" + rating.getMovieId())
//                    .retrieve()  // go to fetch
//                    .bodyToMono(Movie.class) // convert your body info into a instance  what is Mono? getting an object back but not right away
//                    .block();

// 如果想要方法是异步的，那么方法的返回值就需要是一个 Mono类型的对象
// 调用block()方法 把 asynchronous的方法又变成了 synchronous


// 如果应用在多线程环境下被访问，我们需要处理并发问题吗？
// 答：在Servlet容器中，有很多的线程在并行地运行
// 一般来说，对于每一个请求，都会使用一个新的线程来处理它。
// 所以要保证代码中创建的对象要是线程安全的。
// 而 restTemplate对象是线程安全的

// 问：啥时候使用 RestTemplate，什么时候使用 WebClient
// 答：现阶段 RestTemplate用得比较多，但是它最终会被废弃

// 问：可以仅仅使用一个外部调用(不是微服务)来进行这些call吗？
// 答：完全可行，而且会演示
// json payload

// 在多个微服务之间进行通信时，如何处理安全问题?
// 答：手段1-使用HTTPS； 手段2-使用认证的方式来调用微服务

/*
------
现在的实现结果还有哪些问题？
1 发生变动时，必须修改代码；
2 在云端时，URL可能会动态变化
3 负载均衡
4 项目会被部署到多个环境，如何能够一次性配置？

实现1：服务发现；
具体实现：在调用者client 与 被调用者services之间，添加一个 discover server层；

1 各个微服务主动把自己注册到 discovery server上面；// like a phonebook
2 client根据 discovery server上查询到的信息，再去对具体的service进行调用；
显然，步骤变多了

两种方式：
    1 在client去实现对service调用； // 我们会使用这种方式
    2 在server中实现对service的调用； // 这个不像是 discover service

mitigate 缓和

// 问：能不能手动从 discovery service中获取到信息？ 能不能在一段时间内缓存这些信息？
答：yes and yes
spring do all the works, I feel like I lost control

This is the Model/Pattern. now let's talk about Tech.
the tech SpringCloud integrate with that implement discovery service : Eureka

Netflix OSS - leader of micro-services;
    Eureka、Ribbon、Hystrix、Zuul these are libraries;
Spring have abstract layer to make things work together without specify the specific tech~
Spring see these nice libraries, and wrap them into handy use

使用服务发现 来代替 URL的硬编码：
1 添加抽象的中间层 Eureka server；
2 make services as Eureka client;
    - I'm a client; // register
    - I need sth. // consume
 */

/*
启示：
1 你不能随便地在配置文件中改变 client的 service name，这会导致caller找不到你的服务

#Q1 这种调用URL的方式本质上仍旧是硬编码，有没有办法能够 “在运行时获取到所有可用的服务”？
应用场景：现在我们并不清楚 所有可用的services 的名字；
    YES

#Q2 在从 Eureka Server中得到 目标service的URL地址后，caller在后续访问目标service时，还回去先请求 Eureka Service吗？
答：No，caller端会做缓存的

@LoadBalanced注解的作用：
    1 发现可用的services列表；
    2 做caller端的负载均衡；
        caller -> eureka server -> service01_instance1、service01_instance2、service01_instance3
        当某一类型的可用services存在多个的时候，就需要在caller端进行“发起请求时的负载均衡”
        Note：这个过程对程序员来说是透明的(不可见的)，因为封装到Eureka的library中了
    ---
    illustrate what this means:
    prerequisite: you can run a jar file with a given port;
        1 run 'mvn install' in terminal for given springBoot project, you can get a jar file;
    task: Run a project;
    method01: run project in IDE;
        aka run the main class;
    method02: run project in terminal/CMD window;
        java -jar <jar-filename>.jar  // this is actually run the main method inside of the jar file

    note that this is just two ways to run the project.
    so if you run them at the same time, it' gonna have port conflict problem

    solution: while you run the project with command, pass in port configuration;
    syntax: java -D<config-name>=config-value -jar <jar-file-name>.jar
    note that this is typically Java property override syntax，but spring configuration go with same mechanism
    java -Dserver.port=8026 -jar movie-info-service-0.0.1-SNAPSHOT.jar
 */