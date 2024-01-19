package com.dajungdagam.dg.Controller;

import com.dajungdagam.dg.domain.dto.RecommendOutputDto;
import com.dajungdagam.dg.domain.dto.UserResponseDto;
import com.dajungdagam.dg.domain.dto.WishlistDto;
import com.dajungdagam.dg.domain.entity.Wishlist;
import com.dajungdagam.dg.service.UserService;
import com.dajungdagam.dg.service.WishlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private UserService userService;

    // 찜하기
    @PostMapping("/wishlist")
    public ResponseEntity<RecommendOutputDto> likes(Authentication authentication, @RequestBody WishlistDto wishlistDto) {

        try{
            if(authentication == null)
                throw new Exception("authentication is null. non user Info");

            String kakaoName = authentication.getName();
            UserResponseDto userResponseDto = userService.findByUserKakaoNickName(kakaoName);

            //kakaoName으로 찾은 유저의 id와 pathVariable로 받은 id가 같은지 검증
            if(!userService.isSameUser(wishlistDto.getUserId(), userResponseDto)){
                throw new Exception("user is not same");
            }

//            Wishlist wishlist = wishlistService.addPostToWishlist(wishlistDto);
//            if(wishlist == null)    throw new Exception("찜하기 실패");
            RecommendOutputDto recommendOutputDto = wishlistService.addPostToWishlist(wishlistDto);
            if(recommendOutputDto == null)  throw new Exception("FastAPI 서버로 데이터 요청 실패");

            System.out.println(recommendOutputDto.getPostTitles());

            return ResponseEntity.ok().body(recommendOutputDto);
        } catch(Exception e){

            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // 찜 취소하기
    @DeleteMapping("/wishlist")
    public ResponseEntity<Wishlist> deletePost(Authentication authentication,
                                               @RequestParam int postCategory, @RequestParam Long postId){
        Wishlist wishlist = null;
        try{
            if(authentication == null)  throw new Exception("authentication is null");

            String kakaoName = authentication.getName();

            wishlist = wishlistService.deletePostAtWishlist(kakaoName, postCategory, postId);

            log.info("찜목록 게시글 삭제됨.");
            log.info(wishlist.toString());


        } catch (Exception e){
            log.error(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

        log.info("wishlist: " + wishlist.toString());

        return new ResponseEntity<>(wishlist, headers, HttpStatus.OK);

    }

    @PostMapping("/wishlist/{userId}")
    public ResponseEntity<Wishlist> getWishlistByUserId(Authentication authentication, @PathVariable("userId") int userId){
        try{
            if(authentication == null)
                throw new Exception("authentication is null. non user Info");

            String kakaoName = authentication.getName();
            UserResponseDto userResponseDto = userService.findByUserKakaoNickName(kakaoName);

            if(!UserService.isSameUser(userId, userResponseDto))
                throw new Exception("권한이 없습니다.");

            Wishlist wishlist = wishlistService.getWishlistByUserId(userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(new MediaType("application", "json", StandardCharsets.UTF_8));

            log.info("wishlist: " + wishlist.toString());

            return new ResponseEntity<>(wishlist, headers, HttpStatus.OK);
        } catch(Exception e){

            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }





    }
}
