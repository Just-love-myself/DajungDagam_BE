package com.dajungdagam.dg.service;

import com.dajungdagam.dg.domain.dto.RecommendInputDto;
import com.dajungdagam.dg.domain.dto.RecommendOutputDto;
import com.dajungdagam.dg.domain.dto.WishlistDto;
import com.dajungdagam.dg.domain.entity.Post;
import com.dajungdagam.dg.domain.entity.User;
import com.dajungdagam.dg.domain.entity.Wishlist;
import com.dajungdagam.dg.repository.PostRepository;
import com.dajungdagam.dg.repository.UserJpaRepository;
import com.dajungdagam.dg.repository.WishListJpaRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WishlistService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserJpaRepository userRepository;
    @Autowired
    private WishListJpaRepository wishlistRepository;

    @Autowired
    private WebClientService webClientService;

    // 빈 순환 참조 뜨니까 사용 안함
//    @Autowired
//    private TradePostService tradePostService;

    public Wishlist addWishlist(String kakaoName){
        User user = userRepository.findByKakaoName(kakaoName);

        Wishlist wishlist = new Wishlist(user);
        wishlistRepository.save(wishlist);

        log.info("찜목록 생성됨");

        return wishlist;
    }

    // TODO: 이미 찜목록되어 있으면, 알려야하나?
    @Transactional
    public RecommendOutputDto addPostToWishlist(WishlistDto wishlistDto){
        Wishlist wishlist = null;
        RecommendOutputDto recommendOutputDto = null;
        try {
            //log.info("wishlist 1 : " + wishlistDto.getKakaoName());
            Optional<User> userObj = userRepository.findById(wishlistDto.getUserId());
            if(userObj.isEmpty())   throw new Exception("유저가 없습니다.");
            User user = userObj.get();

            //log.info("wishlist 2 : " + user.getKakaoName());
            wishlist = wishlistRepository.findByUser(user);
            //log.info("wish: " + wishlist.toString());

            int postCategory = wishlistDto.getPostCategory();
            Long postId = wishlistDto.getPostId();

            if(wishlist == null)    throw new Exception("wishlist is null");


            Optional<Post> tradePostObj = postRepository.findById(postId);
            Post tradePost = tradePostObj.get();
            log.info("TradePost: "+ tradePost.toString());

            if(tradePost == null)    throw new Exception("wishlist is null");

            wishlist.addTradePost(tradePost);
            wishlistRepository.save(wishlist);
            // wishlistCount 증가
            tradePost.setWishlistCount(tradePost.getWishlistCount() + 1);

            //FastAPI 서버에 요청 보내서 추천 게시글 제목 받아오기
            //interaction(찜 여부)는 알 수 없음(기능 구현 x) --> 임시 값으로 설정 (1 or 0)
            RecommendInputDto recommendInputDto = new RecommendInputDto(
                    user.getId(), tradePost.getId().intValue(), tradePost.getTitle(), 1, 5);
            recommendOutputDto = webClientService.sendHttpPostRequestToFastApi(recommendInputDto);

            log.info("wishlist의 tradeposts: " + wishlist.getTradePosts().toString());


        } catch(Exception e){
            log.info(e.getMessage());
            return null;
        }
//        return wishlist;
        return recommendOutputDto;
    }


    public Wishlist deletePostAtWishlist(String kakaoName, int postCategory, Long postId) {
        Wishlist wishlist= null;
        try {
            User user = userRepository.findByKakaoName(kakaoName);
            wishlist = wishlistRepository.findByUser(user);

            if(wishlist == null)    throw new Exception("wishlist is null");


                Optional<Post> tradePostObj = postRepository.findById(postId);
                Post tradePost = tradePostObj.get();

                if(tradePost == null)    throw new Exception("wishlist is null");
                List<Post> tradePosts = wishlist.getTradePosts();

                Iterator<Post> iter = tradePosts.iterator();
                while(iter.hasNext()) {
                    Post target = iter.next();
                    if(target.getId().equals(postId)) {
                        // wishlistCount 감소
                        tradePost.setWishlistCount(tradePost.getWishlistCount() - 1);
                        iter.remove();
                    }
                }

//                tradePosts.stream().filter(post  -> {
//                    if(Objects.equals(post.getId(), postId)) return true;
//                    else return false;
//                })
//                        .toList().forEach(tradePosts::remove);

                wishlistRepository.save(wishlist);

                log.info("위시 리스트에서 " + postId + "번 게시글이 삭제됨.");

        } catch(Exception e) {
            log.info(e.getMessage());
        }
        return wishlist;
    }

    public Wishlist getWishlistByUserId(int userId) {
        Optional<User> userObj = userRepository.findById(userId);
        if(userObj.isEmpty())   {
            log.error("유저가 없음");
            return null;
        }
        User user = userObj.get();

        return wishlistRepository.findByUser(user);
    }

    @Transactional
    public void deleteWishlistTable(Wishlist wishlist) {
        wishlistRepository.deleteById(wishlist.getId());

    }

}
