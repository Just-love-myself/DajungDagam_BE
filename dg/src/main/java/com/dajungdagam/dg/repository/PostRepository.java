package com.dajungdagam.dg.repository;

import com.dajungdagam.dg.domain.dto.PostDto;
import com.dajungdagam.dg.domain.entity.Area;
import com.dajungdagam.dg.domain.entity.ItemCategory;
import com.dajungdagam.dg.domain.entity.Post;
import com.dajungdagam.dg.domain.entity.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByTitleContaining(String keyword);

    List<Post> findByUserId(int userId);

    List<Post> findAllByUserId(int userId);

    List<Post> findTop3ByOrderByWishlistCountDesc();

    @Modifying // 조회수 구현
    @Query("update Post p set p.viewCount = p.viewCount + 1 where p.id = :id")
    int updateviewCount(Long id);

    List<Post> findByItemCategory(ItemCategory itemCategory);

    List<Post> findByTradeStatus(TradeStatus tradeStatus);

    List<Post> findByArea(Area area);


//    // 특정 타입의 글 조회
//    List<PostDto> findByType(int postType);

}
