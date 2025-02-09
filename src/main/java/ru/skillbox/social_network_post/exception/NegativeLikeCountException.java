package ru.skillbox.social_network_post.exception;

public class NegativeLikeCountException extends RuntimeException {
    public NegativeLikeCountException(String message) {
        super(message);
    }
}
