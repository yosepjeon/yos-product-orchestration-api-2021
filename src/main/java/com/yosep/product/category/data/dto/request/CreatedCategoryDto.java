package com.yosep.product.category.data.dto.request;

import com.yosep.product.category.data.entity.Category;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class CreatedCategoryDto {
    @NotNull
    private Long id = null;

    @NotNull
    private String name = "";

    public CreatedCategoryDto(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }
}
