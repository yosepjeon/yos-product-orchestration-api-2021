package com.yosep.product.jpa.category.data.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.yosep.product.jpa.common.entity.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "categoryId")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "yos_product_category")
public class Category extends BaseEntity {
    @Id
    @Column
    private String categoryId;

    @Column(name = "category_name", length = 50, nullable = false)
    private String name;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory = null;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentCategory", cascade = CascadeType.ALL)
    private List<Category> childs = new ArrayList<>();

    public void addChildCategory(Category child) {
        this.childs.add(child);
        child.setParentCategory(this);
    }
}
