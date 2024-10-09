package com.sometimes.code.controller;

import com.sometimes.code.domain.article.Article;
import com.sometimes.code.dto.article.ArticleDetailInfo;
import com.sometimes.code.dto.article.ArticleWriteRequest;
import com.sometimes.code.dto.article.ArticlesInfo;
import com.sometimes.code.dto.article.SearchDetailCriteriaDto;
import com.sometimes.code.dto.comment.CommentInfo;
import com.sometimes.code.dto.comment.CommentRequestDto;
import com.sometimes.code.service.ArticleService;
import com.sometimes.code.service.S3ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/article")
public class ArticleController {
    private final ArticleService articleService;
    private final S3ImageService s3ImageService;

    @PostMapping("/search/{page}")
    public ResponseEntity<ArticlesInfo> searchDetailArticles(
            @RequestBody SearchDetailCriteriaDto searchDetailCriteriaDto,
            @PathVariable("page") Long page) {

        // 서비스에서 결과를 가져옵니다.
        ArticlesInfo articles = articleService.searchArticles(searchDetailCriteriaDto, page);

        // 결과를 포함하여 응답을 반환합니다.
        return ResponseEntity.ok(articles);
    }
    @PostMapping("/articles/{category}/{page}")
    public ResponseEntity<ArticlesInfo> getArticles(@PathVariable("page") Integer page,
                                                    @PathVariable("category") String category){
        ArticlesInfo articlesInfo = articleService.getArticles(page , category);
        return ResponseEntity.ok(articlesInfo);
    }

    @PostMapping("/articles/author/{nickname}/{page}")
    public ResponseEntity<ArticlesInfo> getArticlesByAuthor(
            @PathVariable("page") Integer page,
            @PathVariable("nickname") String nickname) {
        ArticlesInfo articlesInfo = articleService.getArticlesByAuthor(page, nickname);
        return ResponseEntity.ok(articlesInfo);
    }




    @PostMapping("/write")
    public ResponseEntity<Long> writeArticle(
            @RequestPart("articleWriteRequest") ArticleWriteRequest articleWriteRequest,
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        Article savedArticle = articleService.writeArticle(articleWriteRequest, token, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedArticle.getArticleId());
    }


    @PostMapping("/delete/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable("articleId") Long articleId) {
        try {
            articleService.deleteArticle(articleId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/update/{articleId}")
    public ResponseEntity<Void> updateArticle(@RequestPart("articleWriteRequest") ArticleWriteRequest articleWriteRequest,
                                              @RequestHeader("Authorization") String token,
                                              @RequestPart(value = "images", required = false) List<MultipartFile> images,
                                              @PathVariable("articleId") Long articleId) {

        articleService.updateArticle(articleId, articleWriteRequest, token, images);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDetailInfo> getArticleDetail(@PathVariable("articleId") Long articleId,
                                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            ArticleDetailInfo articleDetailInfo = articleService.getArticleDetail(articleId, token);
            return ResponseEntity.ok(articleDetailInfo);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }


    @PostMapping("/{articleId}/like")
    public ResponseEntity<Void> likeArticle(@PathVariable("articleId") Long articleId,
                                            @RequestHeader("Authorization") String token) {
        try {
            articleService.likeArticle(articleId, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{articleId}/unlike")
    public ResponseEntity<Void> unlikeArticle(@PathVariable("articleId") Long articleId,
                                              @RequestHeader("Authorization") String token) {
        try {
            articleService.unlikeArticle(articleId, token);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }





    @GetMapping("/commentInfo/{articleId}")
    public ResponseEntity<List<CommentInfo>> getCommentsByArticleId(@PathVariable Long articleId) {
        List<CommentInfo> comments = articleService.getCommentsByArticleId(articleId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comment/{articleId}")
    public ResponseEntity<Void> createComment(@PathVariable Long articleId,
                                              @RequestBody CommentRequestDto commentRequestDto,
                                              @RequestHeader("Authorization") String token) {
        articleService.createComment(articleId, token, commentRequestDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable("commentId") Long commentId,
                                              @RequestHeader("Authorization") String token) {
        try {
            articleService.deleteComment(commentId, token);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/update/comment/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable("commentId") Long commentId,
                                              @RequestHeader("Authorization") String token,
                                              @RequestBody CommentRequestDto commentRequestDto) {
        try {
            articleService.updateComment(commentId, token, commentRequestDto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
