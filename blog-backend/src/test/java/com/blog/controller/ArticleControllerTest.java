package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.GlobalExceptionHandler;
import com.blog.dto.ArticleDTO;
import com.blog.dto.ArticleVO;
import com.blog.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ArticleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ArticleService articleService;
    @Mock
    private LikeService likeService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private BrowseHistoryService browseHistoryService;
    @Mock
    private HotArticleService hotArticleService;
    @InjectMocks
    private ArticleController articleController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(articleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ========== GET /api/article/list ==========

    @Test
    void getArticleList_returnsPaginatedResults() throws Exception {
        Page<ArticleVO> page = new Page<>(1, 10, 1);
        ArticleVO vo = new ArticleVO();
        vo.setId(1L);
        vo.setTitle("Test Article");
        page.setRecords(java.util.List.of(vo));

        when(articleService.getArticleList(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/article/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("Test Article"));
    }

    @Test
    void getArticleList_defaultParams() throws Exception {
        Page<ArticleVO> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);

        when(articleService.getArticleList(1, 10, null)).thenReturn(page);

        mockMvc.perform(get("/api/article/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    // ========== GET /api/article/detail/{id} ==========

    @Test
    void getArticleDetail_success() throws Exception {
        ArticleVO vo = new ArticleVO();
        vo.setId(1L);
        vo.setTitle("Detail Article");
        vo.setViewCount(10);

        when(articleService.getArticleDetail(1L, null)).thenReturn(vo);

        mockMvc.perform(get("/api/article/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Detail Article"))
                .andExpect(jsonPath("$.data.viewCount").value(10));

        verify(hotArticleService).recordView(1L);
    }

    @Test
    void getArticleDetail_notFound_returnsError() throws Exception {
        when(articleService.getArticleDetail(999L, null))
                .thenThrow(new RuntimeException("文章不存在"));

        mockMvc.perform(get("/api/article/detail/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("系统内部错误"));
    }

    // ========== POST /api/article/create ==========

    @Test
    void createArticle_success() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("New Article");
        dto.setContent("Content here");

        mockMvc.perform(post("/api/article/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(articleService).createArticle(eq(1L), any(ArticleDTO.class));
    }

    // ========== PUT /api/article/update/{id} ==========

    @Test
    void updateArticle_success() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setTitle("Updated");
        dto.setContent("Updated content");

        mockMvc.perform(put("/api/article/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(articleService).updateArticle(1L, 1L, dto);
    }

    // ========== DELETE /api/article/delete/{id} ==========

    @Test
    void deleteArticle_success() throws Exception {
        mockMvc.perform(delete("/api/article/delete/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(articleService).deleteArticle(1L, 1L);
    }

    // ========== POST /api/article/like/{id} ==========

    @Test
    void toggleLike_success() throws Exception {
        when(likeService.toggleLike(1L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/article/like/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }
}
