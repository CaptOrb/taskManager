package com.conor.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.conor.taskmanager.model.User;

public class NtfyTopicResolverTest {

    private final NtfyTopicResolver resolver = new NtfyTopicResolver();

    @Test
    void resolvePublishTopic_returnsPrefixedTopic() {
        User user = createUser(5L, "my-topic");
        assertEquals("tm-5-my-topic", resolver.resolvePublishTopic(user));
    }

    @Test
    void resolvePublishTopic_nullUser_returnsNull() {
        assertNull(resolver.resolvePublishTopic(null));
    }

    @Test
    void resolvePublishTopic_nullTopic_returnsNull() {
        User user = createUser(1L, null);
        assertNull(resolver.resolvePublishTopic(user));
    }

    @Test
    void resolvePublishTopic_blankTopic_returnsNull() {
        User user = createUser(1L, "   ");
        assertNull(resolver.resolvePublishTopic(user));
    }

    @Test
    void getTopicPrefix_nullUserId_returnsNull() {
        User user = new User();
        assertNull(resolver.getTopicPrefix(user));
    }

    private User createUser(Long id, String ntfyTopic) {
        User user = new User();
        user.setId(id);
        user.setNtfyTopic(ntfyTopic);
        return user;
    }
}
