package com.chelly.backend.controller;

import com.chelly.backend.handler.ResponseHandler;
import com.chelly.backend.models.Category;
import com.chelly.backend.models.payload.response.SuccessResponse;
import com.chelly.backend.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@AllArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<SuccessResponse<List<Category>>> getAllCategories() {
        return ResponseHandler.buildSuccessResponse(
                HttpStatus.OK,
                "Success fetch all categories",
                categoryRepository.findAll()
        );
    }
}
