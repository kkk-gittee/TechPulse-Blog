package com.blog.service;

import com.blog.mapper.ArticleMapper;
import com.blog.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CounterSyncServiceTest {

    @Mock
    private RedisUtils redisUtils;
    @Mock
    private ArticleMapper articleMapper;
    @InjectMocks
    private CounterSyncService counterSyncService;

    @Test
    void incrementLike_incrementsRedis() {
        counterSyncService.incrementLike(1L);
        verify(redisUtils).increment("article:like:1", 1);
    }

    @Test
    void decrementLike_decrementsRedis() {
        counterSyncService.decrementLike(1L);
        verify(redisUtils).decrement("article:like:1");
    }

    @Test
    void incrementView_incrementsRedis() {
        counterSyncService.incrementView(1L);
        verify(redisUtils).increment("article:view:1", 1);
    }

    @Test
    void syncArticleCounters_withDeltas_updatesDb() {
        when(redisUtils.get("article:like:1")).thenReturn(5);
        when(redisUtils.get("article:view:1")).thenReturn(10);

        counterSyncService.syncArticleCounters(1L);

        verify(articleMapper).updateCounters(1L, 5, 10);
        verify(redisUtils).delete("article:like:1");
        verify(redisUtils).delete("article:view:1");
        verify(redisUtils).delete("article:dirty:1");
    }

    @Test
    void syncArticleCounters_noDeltas_skipsUpdate() {
        when(redisUtils.get("article:like:1")).thenReturn(null);
        when(redisUtils.get("article:view:1")).thenReturn(null);

        counterSyncService.syncArticleCounters(1L);

        verify(articleMapper, never()).updateCounters(anyLong(), anyInt(), anyInt());
    }

    @Test
    void syncArticleCounters_zeroDeltas_skipsUpdate() {
        when(redisUtils.get("article:like:1")).thenReturn(0);
        when(redisUtils.get("article:view:1")).thenReturn(0);

        counterSyncService.syncArticleCounters(1L);

        verify(articleMapper, never()).updateCounters(anyLong(), anyInt(), anyInt());
    }

    @Test
    void syncToDatabase_withDirtyKeys_syncsAll() {
        when(redisUtils.scan("article:dirty:*")).thenReturn(Set.of("article:dirty:1", "article:dirty:2"));
        when(redisUtils.get("article:like:1")).thenReturn(3);
        when(redisUtils.get("article:view:1")).thenReturn(null);
        when(redisUtils.get("article:like:2")).thenReturn(null);
        when(redisUtils.get("article:view:2")).thenReturn(7);

        counterSyncService.syncToDatabase();

        verify(articleMapper).updateCounters(1L, 3, 0);
        verify(articleMapper).updateCounters(2L, 0, 7);
    }

    @Test
    void syncToDatabase_noDirtyKeys_skips() {
        when(redisUtils.scan("article:dirty:*")).thenReturn(Set.of());

        counterSyncService.syncToDatabase();

        verify(articleMapper, never()).updateCounters(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getLikeCount_returnsDelta() {
        when(redisUtils.get("article:like:1")).thenReturn(5);
        assertEquals(5, counterSyncService.getLikeCount(1L));
    }

    @Test
    void getLikeCount_nullReturnsZero() {
        when(redisUtils.get("article:like:1")).thenReturn(null);
        assertEquals(0, counterSyncService.getLikeCount(1L));
    }
}
