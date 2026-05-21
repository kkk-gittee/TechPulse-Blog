package com.blog.controller;

import com.blog.common.GlobalExceptionHandler;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentVO;
import com.blog.service.CommentService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;
    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ========== POST /api/comment/add ==========

    @Test
    void addComment_success() throws Exception {
        CommentDTO dto = new CommentDTO();
        dto.setArticleId(1L);
        dto.setContent("Great article!");

        mockMvc.perform(post("/api/comment/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(commentService).addComment(eq(2L), any(CommentDTO.class));
    }

    // ========== GET /api/comment/list/{articleId} ==========

    @Test
    void getComments_returnsList() throws Exception {
        CommentVO vo = new CommentVO();
        vo.setId(1L);
        vo.setContent("Nice post");
        vo.setUserId(1L);
        when(commentService.getArticleComments(1L, null)).thenReturn(List.of(vo));

        mockMvc.perform(get("/api/comment/list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].content").value("Nice post"));
    }

    @Test
    void getComments_empty() throws Exception {
        when(commentService.getArticleComments(1L, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/comment/list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ========== DELETE /api/comment/delete/{id} ==========

    @Test
    void deleteComment_success() throws Exception {
        mockMvc.perform(delete("/api/comment/delete/1")
                        .requestAttr("userId", 1L))
                .andExpect(status().isOk());

        verify(commentService).deleteComment(1L, 1L);
    }

    @Test
    void deleteComment_notOwner_throws() throws Exception {
        doThrow(new RuntimeException("无权删除此评论"))
                .when(commentService).deleteComment(2L, 1L);

        mockMvc.perform(delete("/api/comment/delete/1")
                        .requestAttr("userId", 2L))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("系统内部错误"));
    }

    // ========== POST /api/comment/like/{id} ==========

    @Test
    void toggleLikeComment_success() throws Exception {
        when(commentService.toggleLikeComment(2L, 1L)).thenReturn(true);

        mockMvc.perform(post("/api/comment/like/1")
                        .requestAttr("userId", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.liked").value(true));
    }
}
