package com.sometimes.code.service;

import com.sometimes.code.domain.article.Article;
import com.sometimes.code.domain.article.ArticleImage;
import com.sometimes.code.domain.article.Comment;
import com.sometimes.code.domain.article.Likes;
import com.sometimes.code.domain.auth.ProfileImage;
import com.sometimes.code.domain.auth.User;
import com.sometimes.code.dto.article.ArticleDetailInfo;
import com.sometimes.code.dto.article.ArticleWriteRequest;
import com.sometimes.code.dto.article.ArticlesInfo;
import com.sometimes.code.dto.article.SearchDetailCriteriaDto;
import com.sometimes.code.dto.comment.CommentInfo;
import com.sometimes.code.dto.comment.CommentRequestDto;
import com.sometimes.code.jwt.TokenProvider;
import com.sometimes.code.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final S3ImageService s3ImageService;
    private final CommentRepository commentRepository;
    private final LikesRepository likesRepository;
    private final ArticleImageRepository articleImageRepository;
    private final ProfileImageRepository profileImageRepository;


    public ArticlesInfo getArticles(Integer page, String category) {
        int pageSize = 15;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        Page<Article> articlePage;
        long totalCount;

        // 카테고리가 'all'일 경우 모든 게시글 조회
        if ("all".equalsIgnoreCase(category)) {
            articlePage = articleRepository.findAllArticles(pageable);
            totalCount = articleRepository.countAllArticles();
        } else {
            // 카테고리 값에 맞는 게시글 조회
            Article.Category articleCategory = Article.Category.valueOf(category.toLowerCase());
            articlePage = articleRepository.findArticlesByCategory(articleCategory, pageable);
            totalCount = articleRepository.countArticlesByCategory(articleCategory);
        }

        // 전체 페이지 수 계산
        long totalPages = (long) Math.ceil((double) totalCount / pageSize);

        // Article 객체를 ArticleInfo로 변환하여 리스트로 저장
        List<ArticlesInfo.ArticleInfo> articleInfoList = articlePage.getContent().stream()
                .map(article -> {
                    User user = article.getUser();

                    // 좋아요 수를 likes 테이블에서 가져오기
                    int likeCount = likesRepository.countByArticle_ArticleId(article.getArticleId());

                    // 댓글 수
                    int commentCount = article.getComments().size();

                    // 이미지가 존재하는지 여부
                    boolean existImg = !article.getImages().isEmpty();

                    // Article 객체를 ArticleInfo로 매핑
                    return ArticlesInfo.ArticleInfo.builder()
                            .articleId(article.getArticleId())
                            .userId(user.getUserId())
                            .category(article.getCategory())
                            .title(article.getTitle())
                            .author(user.getNickName())
                            .createdAt(article.getCreatedAt())
                            .likes(likeCount)
                            .existImg(existImg)
                            .commentCnt(commentCount)
                            .build();
                }).collect(Collectors.toList());

        // ArticlesInfo 객체에 담아서 반환
        return ArticlesInfo.builder()
                .pageNum(totalPages)
                .articles(articleInfoList)
                .build();
    }


    public ArticlesInfo getArticlesByAuthor(Integer page, String nickname) {
        int pageSize = 15;
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        // Fetch the user by nickname
        User user = userRepository.findByNickName(nickname)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        // Fetch the articles by the user
        Page<Article> articlePage = articleRepository.findArticlesByUser(user, pageable);
        long totalCount = articleRepository.countArticlesByUser(user);

        // Calculate total pages
        long totalPages = (long) Math.ceil((double) totalCount / pageSize);

        // Convert articles to ArticleInfo list and sort by articleId descending
        List<ArticlesInfo.ArticleInfo> articleInfoList = articlePage.getContent().stream()
                .sorted(Comparator.comparingLong(Article::getArticleId).reversed()) // articleId 기준 내림차순 정렬
                .map(article -> {
                    int likeCount = likesRepository.countByArticle_ArticleId(article.getArticleId());
                    int commentCount = article.getComments().size();

                    // Fetch images associated with the article
                    boolean existImg = !article.getImages().isEmpty();

                    return ArticlesInfo.ArticleInfo.builder()
                            .articleId(article.getArticleId())
                            .userId(user.getUserId())
                            .category(article.getCategory())
                            .title(article.getTitle())
                            .author(user.getNickName())
                            .createdAt(article.getCreatedAt())
                            .likes(likeCount)
                            .existImg(existImg)  // Check if the article has images
                            .commentCnt(commentCount)
                            .build();
                })
                .collect(Collectors.toList());

        // Return the ArticlesInfo object
        return ArticlesInfo.builder()
                .pageNum(totalPages)
                .articles(articleInfoList)
                .build();
    }



    @Transactional
    public Article writeArticle(ArticleWriteRequest articleWriteRequest, String token, List<MultipartFile> images) {
        Long userId = tokenProvider.getUserIdFromToken(token);
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 새로운 Article 생성
        Article article = Article.builder()
                .user(user)
                .title(articleWriteRequest.getTitle())
                .content(articleWriteRequest.getContent())
                .category(articleWriteRequest.getCategory())
                .createdAt(LocalDate.now())
                .hits(0)
                .build();

        // Article 저장
        Article savedArticle = articleRepository.save(article);

        // 이미지 처리 및 저장
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String imageUrl = s3ImageService.uploadImage(image,"articleImg");  // S3에 이미지 업로드
                ArticleImage articleImage = ArticleImage.builder()
                        .article(savedArticle)
                        .url(imageUrl)
                        .build();
                articleImageRepository.save(articleImage);  // ArticleImage 저장
            }
        }

        return savedArticle;
    }



    @Transactional
    public void deleteArticle(Long articleId) {
        // Article 존재 여부 확인
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + articleId));

        // S3에서 관련된 이미지 삭제
        List<ArticleImage> articleImages = articleImageRepository.findByArticle(article);
        for (ArticleImage articleImage : articleImages) {
            s3ImageService.deleteImageFromS3(articleImage.getUrl());
        }

        // Article 삭제
        articleRepository.deleteById(articleId);
        // ArticleImage 삭제
        articleImageRepository.deleteAll(articleImages); // 관련 이미지 데이터도 삭제
    }


    @Transactional
    public ResponseEntity<Void> updateArticle(Long articleId, ArticleWriteRequest articleWriteRequest, String token, List<MultipartFile> images) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // 기존 이미지 삭제
        List<ArticleImage> existingImages = articleImageRepository.findByArticle(article);
        for (ArticleImage existingImage : existingImages) {
            try {
                s3ImageService.deleteImageFromS3(existingImage.getUrl());
                // 이미지 삭제 후 ArticleImage 엔티티도 삭제
                articleImageRepository.delete(existingImage);
            } catch (Exception e) {
                // 삭제 실패에 대한 로그 추가
                System.err.println("Failed to delete image from S3: " + existingImage.getUrl());
            }
        }

        // Article 정보 업데이트
        article.setTitle(articleWriteRequest.getTitle());
        article.setCategory(articleWriteRequest.getCategory());
        article.setContent(articleWriteRequest.getContent());

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                String newImageUrl = s3ImageService.uploadImage(image,"articleImg");
                ArticleImage articleImage = ArticleImage.builder()
                        .article(article)
                        .url(newImageUrl)
                        .build();
                articleImageRepository.save(articleImage);  // 새 이미지를 ArticleImage 테이블에 저장
            }
        }

        articleRepository.save(article); // Article 저장

        return ResponseEntity.ok().build();
    }



    public ArticlesInfo searchArticles(SearchDetailCriteriaDto criteria, Long page) {
        int pageSize = 15;
        Pageable pageable = PageRequest.of(page.intValue() - 1, pageSize);

        // 검색된 게시글을 페이지네이션하여 가져옴
        Page<Article> articlesPage = articleRepository.searchArticles(
                criteria.getKeyword(),
                pageable
        );

        // 전체 페이지 수 계산
        int totalPageNum = (int) Math.ceil((double) articlesPage.getTotalElements() / pageSize);

        // Article 객체를 ArticleInfo로 변환하여 리스트로 저장
        List<ArticlesInfo.ArticleInfo> articleInfoList = articlesPage.getContent().stream()
                .map(article -> {
                    // 각 게시글에 대한 좋아요 수 계산
                    int likeCount = likesRepository.countByArticle_ArticleId(article.getArticleId());

                    // 댓글 수 계산
                    int commentCount = article.getComments().size();

                    // 이미지 존재 여부 확인 (ArticleImage 테이블 사용)
                    boolean existImg = !article.getImages().isEmpty();

                    // Article 객체를 ArticleInfo로 매핑
                    return ArticlesInfo.ArticleInfo.builder()
                            .articleId(article.getArticleId())
                            .userId(article.getUser().getUserId()) // 작성자 ID
                            .category(article.getCategory())
                            .title(article.getTitle())
                            .author(article.getUser().getNickName()) // 작성자 닉네임
                            .createdAt(article.getCreatedAt())
                            .likes(likeCount) // 좋아요 수
                            .commentCnt(commentCount)
                            .existImg(existImg) // 이미지 존재 여부
                            .build();
                })
                .collect(Collectors.toList());

        // ArticlesInfo 객체에 담아서 반환
        return ArticlesInfo.builder()
                .pageNum((long) totalPageNum)
                .articles(articleInfoList)
                .build();
    }

    @Transactional
    public ArticleDetailInfo getArticleDetail(Long articleId, String token) {
        Long userId = null;
        boolean mine = false;
        boolean pushLikes = false;

        // JWT가 있을 경우 사용자 ID 추출
        if (token != null && !token.isEmpty()) {
            userId = tokenProvider.getUserIdFromToken(token);
        }

        // 게시글 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // 조회수 증가
        article.setHits(article.getHits() + 1);
        articleRepository.save(article);

        // JWT가 있을 경우 좋아요 상태 및 본인 글 여부 확인
        if (userId != null) {
            // 작성자가 본인의 게시글인지 여부 확인
            mine = article.getUser().getUserId().equals(userId);

            // 사용자가 해당 게시글에 좋아요를 눌렀는지 여부 확인
            pushLikes = likesRepository.existsByUser_UserIdAndArticle_ArticleId(userId, articleId);
        }

        // 좋아요 수 조회
        int likeCount = likesRepository.countByArticle_ArticleId(articleId);

        // ArticleImage 목록 가져오기
        List<String> imageUrls = article.getImages().stream()
                .map(ArticleImage::getUrl)
                .collect(Collectors.toList());

        // 사용자 프로필 이미지가 여러 개일 수 있으므로 리스트로 처리
        List<String> profileImgs = profileImageRepository.findByUser_UserId(article.getUser().getUserId()).stream()
                .map(ProfileImage::getUrl)
                .collect(Collectors.toList());

        // ArticleDetailInfo 객체에 게시글 정보를 매핑하여 반환
        return ArticleDetailInfo.builder()
                .articleId(article.getArticleId())
                .userId(article.getUser().getUserId())
                .category(article.getCategory())
                .title(article.getTitle())
                .nickName(article.getUser().getNickName()) // 작성자 닉네임
                .createAt(article.getCreatedAt())
                .content(article.getContent())
                .imageUrls(imageUrls)  // 게시글에 포함된 이미지 URL 리스트
                .likes(likeCount)  // 좋아요 수
                .hits(article.getHits())  // 업데이트된 조회수
                .commentNum(article.getComments().size()) // 댓글 수
                .mine(mine)  // 사용자가 작성자인지 여부 (JWT 있을 때만 true)
                .pushLikes(pushLikes)  // 사용자가 좋아요를 눌렀는지 여부 (JWT 있을 때만 true)
                .profileImgs(profileImgs) // 프로필 이미지 리스트
                .build();
    }



    @Transactional
    public void likeArticle(Long articleId, String token) {
        // JWT 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 게시글 정보 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // 이미 좋아요를 눌렀는지 확인
        boolean alreadyLiked = likesRepository.existsByUser_UserIdAndArticle_ArticleId(userId, articleId);

        if (alreadyLiked) {
            throw new RuntimeException("You have already liked this article");
        }

        // 좋아요 저장
        Likes like = new Likes();
        like.setUser(user);
        like.setArticle(article);
        likesRepository.save(like);
    }

    @Transactional
    public void unlikeArticle(Long articleId, String token) {
        // JWT 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 게시글 정보 조회
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // 해당 사용자가 이미 좋아요를 눌렀는지 확인
        Likes like = likesRepository.findByUser_UserIdAndArticle_ArticleId(userId, articleId)
                .orElseThrow(() -> new RuntimeException("You have not liked this article"));

        // 좋아요 삭제
        likesRepository.delete(like);
    }


    public List<CommentInfo> getCommentsByArticleId(Long articleId) {

        return commentRepository.findByArticleArticleId(articleId)
                .stream()
                .map(comment -> {
                    // 해당 유저의 프로필 이미지 리스트 가져오기
                    List<String> profileImages = profileImageRepository.findByUser_UserId(comment.getUser().getUserId())
                            .stream()
                            .map(ProfileImage::getUrl)
                            .collect(Collectors.toList());

                    // CommentInfo 객체로 매핑
                    return CommentInfo.builder()
                            .userId(comment.getUser().getUserId())
                            .commentId(comment.getCommentId())
                            .nickName(comment.getUser().getNickName())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .profileImages(profileImages)  // 여러 프로필 이미지 설정
                            .build();
                })
                .toList();
    }

    @Transactional
    public Comment createComment(Long articleId, String token, CommentRequestDto commentRequestDto) {
        // 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid user.");
        }

        User user = userOptional.get();
        Optional<Article> articleOptional = articleRepository.findById(articleId);

        if (articleOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid article.");
        }

        Article article = articleOptional.get();

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setUser(user);
        comment.setContent(commentRequestDto.getContent());
        comment.setCreatedAt(LocalDate.now());

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String token) {
        // 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // 작성자가 본인인지 확인
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this comment");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

    @Transactional
    public void updateComment(Long commentId, String token, CommentRequestDto commentRequestDto) {
        // 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // 작성자가 본인인지 확인
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this comment");
        }

        // 댓글 내용 업데이트
        comment.setContent(commentRequestDto.getContent());
        comment.setCreatedAt(LocalDate.now());

        // 댓글 저장
        commentRepository.save(comment);
    }
}
