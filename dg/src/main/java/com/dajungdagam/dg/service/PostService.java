package com.dajungdagam.dg.service;

import com.dajungdagam.dg.domain.dto.PostDto;
import com.dajungdagam.dg.domain.dto.TradePostSummaryDto;
import com.dajungdagam.dg.domain.entity.*;
import com.dajungdagam.dg.repository.ImageRepository;
import com.dajungdagam.dg.repository.ItemCategoryRepository;
import com.dajungdagam.dg.repository.PostRepository;
import com.dajungdagam.dg.repository.WishListJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostService {

    private PostRepository postRepository;
    private ImageRepository imageRepository;


    private WishlistService wishlistService;

    private WishListJpaRepository wishlistRepository;

    private ItemCategoryRepository itemCategoryRepository;

    private static final int BLOCK_PAGE_NUM_COUNT = 5; // 블럭에 존재하는 페이지 수
    private static final int PAGE_POST_COUNT = 9; // 한 페이지에 존재하는 게시글 수

    @Autowired
    public PostService(PostRepository postRepository, ImageRepository imageRepository, WishlistService wishlistService, WishListJpaRepository wishlistRepository, ItemCategoryRepository itemCategoryRepository) {
        this.postRepository = postRepository;
        this.imageRepository = imageRepository;
        this.wishlistService = wishlistService;
        this.wishlistRepository = wishlistRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }




    @Transactional
    public List<PostDto> searchPosts(String keyword) {
        List<Post> posts = postRepository.findByTitleContaining(keyword);
        List<PostDto> postDtoList = new ArrayList<>();

        if (posts.isEmpty()) return postDtoList;

        for (Post post : posts) {
            postDtoList.add(this.convertEntityToDto(post));
        }

        return postDtoList;
    }

    private PostDto convertEntityToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .user(post.getUser())
                .title(post.getTitle())
                .postType(post.getPostType())
                .tradeArea(post.getTradeArea())
                .content(post.getContent())
                .createdTime(post.getCreatedTime())
                .updateTime(post.getUpdateTime())
                .viewCount(post.getViewCount())
                .wishlistCount(post.getWishlistCount())
                .chatLink(post.getChatLink())
                .tradeStatus(post.getTradeStatus())
                .itemCategory(post.getItemCategory())
                .build();
    }


    // 절대 경로임. Mac 기준 경로임을 유의
    private final String imagePath = "F:\\";

    @Transactional // 게시글 작성 이미지 업로드 기능 추가
    public void savePost(PostDto postDto, MultipartFile[] images) throws IOException {

         Path uploadPath = Paths.get(imagePath);

         // 만약 경로가 없다면 경로 생성
         if (!Files.exists(uploadPath)) {
             try {
                 Files.createDirectories(uploadPath);
             } catch (IOException e) {
                 // 예외 처리 추가 (예: 로깅)
                 e.printStackTrace();
             }
         }

         // 게시글 DB에 저장 후 pk을 받아옴
         Long id = postRepository.save(postDto.toEntity()).getId();
         Post post = postRepository.findById(id).get();
         if (images != null && images.length > 0) {

             // 최소 하나의 이미지를 업로드하도록 검증
             if (images.length < 1) {
                 throw new IllegalArgumentException("최소 하나의 이미지를 업로드해야 합니다.");
             }
             // 최대 5개의 이미지만 업로드하도록 검증
             if (images.length > 5) {
                 throw new IllegalArgumentException("최대 5개의 이미지만 업로드할 수 있습니다.");
             }

             // 파일 정보 저장
             for(MultipartFile image : images) {
                 // 파일명 추출
                 String originalImageName = image.getOriginalFilename();

                 // 이미지가 비어있는지 확인
                 if (image.isEmpty()) {
                     throw new IllegalArgumentException("이미지가 비어있습니다.");
                 }

                 if (originalImageName != null && !originalImageName.isEmpty()) {
                     // 확장자 추출 및 소문자로 변환
                     String formatType = originalImageName.substring(
                             originalImageName.lastIndexOf(".")).toLowerCase();

                     // jpg, jpeg 또는 png 형식의 이미지만 업로드 가능하도록 검증
                     if (!formatType.equals(".jpg") && !formatType.equals(".jpeg") && !formatType.equals(".png")) {
                         throw new IllegalArgumentException("jpg, jpeg 또는 png 형식의 이미지만 업로드 가능합니다.");
                     }

                     // UUID 생성
                     String uuid = UUID.randomUUID().toString();

                     // 경로 지정
                     String path = imagePath + uuid + originalImageName;

                     // 경로에 파일을 저장 (DB 아님)
                     image.transferTo(new File(path) );

                     Image image1 = Image.builder()
                             .imagePath(imagePath)
                             .imageName(originalImageName)
                             .uuid(uuid)
                             .imageType(formatType)
                             .imageSize(image.getSize())
                             .post(post)
                             .build();

                     imageRepository.save(image1);

                 }
             }
         }

     }

    public List<Image> getImagesByTradePost(Post post) {
        return imageRepository.findByPost(post);
    }

    @Transactional
    public List<PostDto> getPostlist() {

        List<Post> posts = postRepository
                .findAll(Sort.by(Sort.Direction.ASC, "createdTime"));

        List<PostDto> postDtoList = new ArrayList<>();

        for (Post post : posts) {
            PostDto postDto = PostDto.builder()
                    .id(post.getId())
                    .user(post.getUser())
                    .title(post.getTitle())
                    .postType(post.getPostType())
                    .tradeArea(post.getTradeArea())
                    .content(post.getContent())
                    .createdTime(post.getCreatedTime())
                    .updateTime(post.getUpdateTime())
                    .viewCount(post.getViewCount())
                    .wishlistCount(post.getWishlistCount())
                    .chatLink(post.getChatLink())
                    .tradeStatus(post.getTradeStatus())
                    .itemCategory(post.getItemCategory())
                    .build();
            postDtoList.add(postDto);
        }

        return postDtoList;
    }

    @Transactional
    public Integer[] getPageList(Integer curPageNum) {
         Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        // 총 게시글 수
         Double postsTotalCount = Double.valueOf(this.getPostCount());

         // 총 게시글 수를 기준으로 계산한 마지막 페이지 번호 계산
         Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));

        // 현재 페이지를 기준으로 블럭의 마지막 페이지 번호 계산
         Integer blockLastPageNum = (totalLastPageNum > curPageNum + BLOCK_PAGE_NUM_COUNT)
                 ? curPageNum + BLOCK_PAGE_NUM_COUNT
                 : totalLastPageNum;

         // 페이지 시작 번호 조정
         curPageNum = (curPageNum<=3) ? 1 : curPageNum-2;

         // 페이지 번호 할당
         for(int val=curPageNum, i=0; val<=blockLastPageNum; val++, i++) {
             pageList[i] = val;
         }

         return pageList;
    }

    @Transactional
    public Long getPostCount() {
         return postRepository.count();
    }

    @Transactional
    public PostDto getPost(Long id) {
        Optional<Post> tradePostWrapper = postRepository.findById(id);
        Post post = tradePostWrapper.get();

        PostDto postDto = PostDto.builder()
                .id(post.getId())
                .user(post.getUser())
                .title(post.getTitle())
                .postType(post.getPostType())
                .tradeArea(post.getTradeArea())
                .content(post.getContent())
                .createdTime(post.getCreatedTime())
                .updateTime(post.getUpdateTime())
                .viewCount(post.getViewCount())
                .wishlistCount(post.getWishlistCount())
                .chatLink(post.getChatLink())
                .tradeStatus(post.getTradeStatus())
                .itemCategory(post.getItemCategory())
                .build();

        return postDto;
    }

    @Transactional // 게시물 수정
    public void updatePost(PostDto postDto) {
        postRepository.save(postDto.toEntity()).getId();
    }

    @Transactional // 게시물 삭제
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }


    @Transactional // 조회수 기능
    public int updateView(Long id) {
        return postRepository.updateviewCount(id);
    }

    @Transactional
    public List<Post> getAllTradePostWithUserId(int userId) {
        List<Post> tradePosts = postRepository.findAllByUserId(userId);

        if(tradePosts.isEmpty()){
            log.info("tradePosts is Empty");
            return null;
        }

        return tradePosts;
    }

    @Transactional
    public boolean deleteAllPost(User user) {
        int userId = user.getId();

        List<Post> tradePostList= this.getAllTradePostWithUserId(userId);
        Wishlist wishlist = wishlistService.getWishlistByUserId(userId);

        if(tradePostList == null)
            return true;

        // 찜목록 삭제
        for(Post tradePost : wishlist.getTradePosts()) {
            postRepository.deleteById(tradePost.getId());
        }

        // 작성글 삭제
        for(Post tradePost : tradePostList) {
            postRepository.deleteById(tradePost.getId());
        }

        tradePostList= this.getAllTradePostWithUserId(userId);
        return tradePostList == null;
    }

    @Transactional
    public List<PostDto> searchPostsByUserId(int userId) {
        List<Post> tradePosts = postRepository.findByUserId(userId);
        List<PostDto> tradePostDtoList = new ArrayList<>();

        if(tradePosts.isEmpty()){
            log.info("tradePosts is Empty");
            return tradePostDtoList;
        }

        for(Post tradePost : tradePosts){
            tradePostDtoList.add(this.convertEntityToDto(tradePost));
        }

        return tradePostDtoList;
    }

    public List<TradePostSummaryDto> getLikePosts() {
        List<Post> likePosts = postRepository.findTop3ByOrderByWishlistCountDesc();
        List<TradePostSummaryDto> summaryDtos = new ArrayList<>();
        for (Post likePost : likePosts) {
            TradePostSummaryDto tradePostSummaryDto = TradePostSummaryDto.builder()
                    .id(likePost.getId())
                    .user(likePost.getUser())
                    .title(likePost.getTitle())
                    .tradeArea(likePost.getTradeArea())
                    .content(likePost.getContent())
                    .viewCount(likePost.getViewCount())
                    .wishlistCount(likePost.getWishlistCount())
                    .tradeStatus(likePost.getTradeStatus())
                    .build();
            summaryDtos.add(tradePostSummaryDto);
        }
        return summaryDtos;
    }

    @Transactional
    public List<PostDto> getPostsByStatus(TradeStatus tradeStatus) {
        List<Post> postList = postRepository.findByTradeStatus(tradeStatus);
        return postList.stream()
                .map(post -> new PostDto(post.getId(), post.getUser(), post.getArea(), post.getTitle(),
                        post.getPostType(), post.getTradeArea(), post.getContent(), post.getCreatedTime(),
                        post.getUpdateTime(), post.getViewCount(), post.getWishlistCount(), post.getChatLink(),
                        post.getTradeStatus(), post.getItemCategory()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostDto> getPostsByCategory(ItemCategory itemCategory) {
        List<Post> postsInCategory = postRepository.findByItemCategory(itemCategory);
        return postsInCategory.stream()
                .map(post -> new PostDto(post.getId(), post.getUser(), post.getArea(), post.getTitle(),
                        post.getPostType(), post.getTradeArea(), post.getContent(), post.getCreatedTime(),
                        post.getUpdateTime(), post.getViewCount(), post.getWishlistCount(), post.getChatLink(),
                        post.getTradeStatus(), post.getItemCategory()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PostDto> getPostsByArea(Area area) {
        List<Post> postsInArea = postRepository.findByArea(area);
        return postsInArea.stream()
                .map(post -> new PostDto(post.getId(), post.getUser(), post.getArea(), post.getTitle(),
                        post.getPostType(), post.getTradeArea(), post.getContent(), post.getCreatedTime(),
                        post.getUpdateTime(), post.getViewCount(), post.getWishlistCount(), post.getChatLink(),
                        post.getTradeStatus(), post.getItemCategory()))
                .collect(Collectors.toList());
    }

}

