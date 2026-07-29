package com.ministore.blog.config;

import com.ministore.blog.model.Post;
import com.ministore.blog.repository.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Seeds the demo articles the frontend's blog pages expect on first run. */
@Configuration
public class DataSeeder {

    private static final String AUTHOR_NAME = "MiniStore Editor";
    private static final String AUTHOR_EMAIL = "editor@ministore.local";

    @Bean
    CommandLineRunner seedPosts(PostRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return; // already seeded
            }
            repo.saveAll(List.of(
                    post("top-10-small-camera-in-the-world",
                            "Top 10 Small Camera In The World",
                            "Compact bodies, full-frame sensors. We ranked the ten travel cameras "
                                    + "that give up the least when you give up the bulk.",
                            "Camera",
                            "/images/blog-item1.jpg",
                            List.of("Camera", "Gear", "Travel"),
                            "2023-02-22T09:00:00Z"),
                    post("how-to-choose-your-first-lens",
                            "How To Choose Your First Lens",
                            "A 50mm prime or a kit zoom? The honest answer depends on what you "
                                    + "actually shoot, not on what the reviews rank highest.",
                            "Camera",
                            "/images/blog-item2.jpg",
                            List.of("Camera", "Lens", "Beginner"),
                            "2023-03-14T09:00:00Z"),
                    post("mechanical-watches-worth-the-money",
                            "Mechanical Watches Worth The Money",
                            "Six automatics under a thousand dollars that keep good time and "
                                    + "still feel like an heirloom on the wrist.",
                            "Watches",
                            "/images/blog-item3.jpg",
                            List.of("Watches", "Classic"),
                            "2023-04-02T09:00:00Z"),
                    post("a-minimal-desk-setup-that-works",
                            "A Minimal Desk Setup That Works",
                            "One lamp, one cable, nothing you don't reach for daily. How to "
                                    + "strip a workspace back without making it useless.",
                            "Lifestyle",
                            "/images/blog-item4.jpg",
                            List.of("Lifestyle", "Modern"),
                            "2023-05-19T09:00:00Z"),
                    post("shipping-and-returns-explained",
                            "Shipping And Returns, Explained",
                            "What happens between checkout and your doorstep, and exactly how "
                                    + "long you have to change your mind.",
                            "Lifestyle",
                            "/images/blog-item5.jpg",
                            List.of("Lifestyle", "Shop"),
                            "2023-06-08T09:00:00Z"),
                    post("phone-cameras-vs-real-cameras",
                            "Phone Cameras vs Real Cameras",
                            "Computational photography closed most of the gap. Here is where the "
                                    + "remaining gap still shows up in a print.",
                            "Camera",
                            "/images/blog-item6.jpg",
                            List.of("Camera", "Phones"),
                            "2023-07-21T09:00:00Z")
            ));
        };
    }

    private static Post post(String slug, String title, String excerpt, String category,
                             String coverImage, List<String> tags, String publishedAt) {
        // The seeded body is placeholder prose: enough paragraphs for the single-post
        // page to look right, replaced by real content through the admin endpoints.
        String content = """
                %s

                Every choice here comes down to the same trade-off: what you carry versus
                what you capture. Spend a week with any of these and the spec sheet stops
                mattering — what matters is whether it is in your bag when the light is good.

                We tested each one over a month of ordinary use: commutes, weekends, one
                badly-lit dinner. No studio, no tripod, no second chances at the shot.

                If you take one thing away, take this: the best gear is the gear you stop
                thinking about.
                """.formatted(excerpt);

        return new Post(slug, title, excerpt, content, category, coverImage,
                AUTHOR_NAME, AUTHOR_EMAIL, new ArrayList<>(tags), true, Instant.parse(publishedAt));
    }
}
