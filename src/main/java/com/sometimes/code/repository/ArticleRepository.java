package com.sometimes.code.repository;

import com.sometimes.code.domain.article.Article;
import com.sometimes.code.domain.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query("SELECT a FROM Article a " +
            "WHERE (:keyword IS NULL OR a.title LIKE %:keyword%) " +
            "ORDER BY a.articleId DESC")
    Page<Article> searchArticles(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT a FROM Article a WHERE a.category = :category ORDER BY a.articleId DESC")
    Page<Article> findArticlesByCategory(@Param("category") Article.Category category, Pageable pageable);

    @Query("SELECT a FROM Article a ORDER BY a.articleId DESC")
    Page<Article> findAllArticles(Pageable pageable);

    @Query("SELECT COUNT(a) FROM Article a WHERE a.category = :category")
    long countArticlesByCategory(@Param("category") Article.Category category);

    @Query("SELECT COUNT(a) FROM Article a")
    long countAllArticles();

    @Query("SELECT a FROM Article a WHERE a.user = :user ORDER BY a.createdAt DESC")
    Page<Article> findArticlesByUser(@Param("user") User user, Pageable pageable);

    long countArticlesByUser(User user);

}
