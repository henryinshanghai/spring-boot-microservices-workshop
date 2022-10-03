package io.javabrains.ratingsdataservice.resources;

import io.javabrains.ratingsdataservice.models.Rating;
import io.javabrains.ratingsdataservice.models.UserRating;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/ratingsdata")
public class RatingsResource {

    // 根据传入的movieId来返回此电影的用户评分
    @RequestMapping("/movies/{movieId}")
    public Rating getMovieRating(@PathVariable("movieId") String movieId) {
         return new Rating(movieId, 4);
    }


    // 根据传入的userId 来 返回用户对所有电影的评分情况
    @RequestMapping("/user/{userId}")
    public UserRating getUserRatings(@PathVariable("userId") String userId) { // UserRating - List<Rating>
//        UserRating userRating = new UserRating();
//        userRating.initData(userId);
//        return userRating;
        List<Rating> ratings = Arrays.asList(
                new Rating("1234", 4),
                new Rating("5678", 3)
        );
//        return ratings; // 不要返回一个array/list，而应该返回一个Object(更加灵活)

        // 不再返回一个ratings列表，而是返回一个 userRating对象（对象中封装了ratings的信息）
        UserRating userRating = new UserRating();
        userRating.setRatings(ratings);
        return userRating;
    } // 返回Object可以很好地应对将来可能的API升级

    @RequestMapping("/{movieId}")
    public Rating getRating(@PathVariable("movieId") String movieId) {
        return new Rating(movieId, 4);
    }

}
