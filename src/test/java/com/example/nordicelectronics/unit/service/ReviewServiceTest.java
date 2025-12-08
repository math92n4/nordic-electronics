package com.example.nordicelectronics.unit.service;

import com.example.nordicelectronics.entity.Order;
import com.example.nordicelectronics.entity.Product;
import com.example.nordicelectronics.entity.Review;
import com.example.nordicelectronics.entity.User;
import com.example.nordicelectronics.entity.dto.review.ReviewDTO;
import com.example.nordicelectronics.repositories.sql.ReviewRepository;
import com.example.nordicelectronics.service.OrderService;
import com.example.nordicelectronics.service.ProductService;
import com.example.nordicelectronics.service.ReviewService;
import com.example.nordicelectronics.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewId;
    private UUID userId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    void getById_found() {
        Review review = new Review();
        review.setReviewId(reviewId);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        Review result = reviewService.getById(reviewId);

        assertThat(result).isSameAs(review);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    void getById_notFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getById(reviewId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void getByUserId_returnsList() {
        Review review = new Review();
        when(reviewRepository.findByUser_UserId(userId)).thenReturn(List.of(review));

        List<Review> reviews = reviewService.getByUserId(userId);

        assertThat(reviews).containsExactly(review);
    }

    @Test
    void getByUserEmail_happyPath() {
        User user = new User();
        user.setUserId(userId);
        Review review = new Review();
        when(userService.findByEmail("a@b.c")).thenReturn(user);
        when(reviewRepository.findByUser_UserId(userId)).thenReturn(List.of(review));

        List<Review> reviews = reviewService.getByUserEmail("a@b.c");

        assertThat(reviews).containsExactly(review);
        verify(userService).findByEmail("a@b.c");
    }

    @Test
    void getByProductId_happyPath() {
        Product product = new Product();
        product.setProductId(productId);
        Review review = new Review();
        when(productService.getEntityById(productId)).thenReturn(product);
        when(reviewRepository.findByProduct_ProductId(productId)).thenReturn(List.of(review));

        List<Review> reviews = reviewService.getByProductId(productId);

        assertThat(reviews).containsExactly(review);
        verify(productService).getEntityById(productId);
    }

    @Test
    void save_delegatesToRepository() {
        Review review = new Review();
        when(reviewRepository.save(review)).thenReturn(review);

        Review savedReview = reviewService.save(review);

        assertThat(savedReview).isSameAs(review);
        verify(reviewRepository).save(review);
    }

    @Test
    void saveForUser_setsUserProductAndCreatedAt() {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("u@x.y");
        Product product = new Product();
        product.setProductId(productId);

        Order dummyOrder = new Order();
        dummyOrder.setOrderId(UUID.randomUUID());
        when(orderService.getOrderById(any(UUID.class))).thenReturn(dummyOrder);

        ReviewDTO incomingReview = new ReviewDTO();
        incomingReview.setTitle("T");
        incomingReview.setComment("Great product");
        incomingReview.setReviewValue(5);
        incomingReview.setOrderId(dummyOrder.getOrderId());
        incomingReview.setIsVerifiedPurchase(false);
        incomingReview.setProductId(productId);

        when(userService.findByEmail("u@x.y")).thenReturn(user);
        when(productService.getEntityById(productId)).thenReturn(product);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review savedReview = reviewService.saveForUser("u@x.y", incomingReview);

        assertThat(savedReview.getUser()).isSameAs(user);
        assertThat(savedReview.getProduct()).isSameAs(product);
        assertThat(savedReview.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void update_updatesFields() {
        Review existing = new Review();
        existing.setReviewId(reviewId);
        existing.setTitle("old");
        existing.setComment("oldc");
        existing.setReviewValue(1);
        existing.setVerifiedPurchase(false);

        ReviewDTO updateReview = new ReviewDTO();
        updateReview.setTitle("new");
        updateReview.setComment("newc");
        updateReview.setReviewValue(5);
        updateReview.setIsVerifiedPurchase(true);

        Product product = new Product();
        product.setProductId(productId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(productService.getEntityById(productId)).thenReturn(product);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review updatedReview = reviewService.update(reviewId, updateReview, productId);

        assertThat(updatedReview.getTitle()).isEqualTo("new");
        assertThat(updatedReview.getComment()).isEqualTo("newc");
        assertThat(updatedReview.getReviewValue()).isEqualTo(5);
        assertThat(updatedReview.isVerifiedPurchase()).isTrue();
        assertThat(updatedReview.getProduct()).isSameAs(product);
    }

    @Test
    void updateForUser_happyPath() {
        User user = new User();
        user.setUserId(userId);
        user.setEmail("u@a.b");

        Review existing = new Review();
        existing.setReviewId(reviewId);
        existing.setUser(user);

        ReviewDTO updateReview = new ReviewDTO();
        updateReview.setTitle("changed");
        updateReview.setProductId(productId);
        updateReview.setIsVerifiedPurchase(true);

        Product product = new Product();
        product.setProductId(productId);

        when(userService.findByEmail("u@a.b")).thenReturn(user);
        when(reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId)).thenReturn(Optional.of(existing));
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existing));
        when(productService.getEntityById(productId)).thenReturn(product);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Review updatedReview = reviewService.updateForUser("u@a.b", reviewId, updateReview);

        assertThat(updatedReview.getTitle()).isEqualTo("changed");
    }

    @Test
    void updateForUser_unauthorized_throws() {
        User user = new User();
        user.setUserId(userId);
        when(userService.findByEmail("bad@u"))
                .thenReturn(user);
        when(reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateForUser("bad@u", reviewId, new ReviewDTO()))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found or you don't have permission to update it");
    }

    @Test
    void deleteById_softDeletes() {
        Review review = new Review();
        review.setReviewId(reviewId);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.deleteById(reviewId);

        // soft delete should find entity, set deletedAt, and save
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(review);
        assertThat(review.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteById_throwsEntityNotFoundException_whenReviewNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteById(reviewId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void deleteForUser_softDeletes() {
        User user = new User();
        user.setUserId(userId);
        Review existing = new Review();
        existing.setReviewId(reviewId);
        existing.setUser(user);

        when(userService.findByEmail("x@x"))
                .thenReturn(user);
        when(reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId))
                .thenReturn(Optional.of(existing));
        when(reviewRepository.save(any(Review.class))).thenReturn(existing);

        reviewService.deleteForUser("x@x", reviewId);

        // soft delete should save with deletedAt set
        verify(reviewRepository).save(existing);
        assertThat(existing.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteForUser_unauthorized_throws() {
        User user = new User();
        user.setUserId(userId);
        when(userService.findByEmail("x@x"))
                .thenReturn(user);
        when(reviewRepository.findByReviewIdAndUser_UserId(reviewId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteForUser("x@x", reviewId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found or you don't have permission to delete it");
    }
}
