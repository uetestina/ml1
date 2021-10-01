package com.disid.restful.model;

import org.springframework.roo.addon.javabean.annotations.RooJavaBean;
import org.springframework.roo.addon.javabean.annotations.RooToString;
import org.springframework.roo.addon.jpa.annotations.entity.RooJpaEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name = "my_product")
public class Product {

  /**
   */
  @Column(name = "my_name")
  private String name;

  /**
   */
  @Column(name = "my_description")
  private String description;

  /**
   * Bidirectional aggregation many-to-many relationship. Child side.
   */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "my_products_categories",
      joinColumns = @JoinColumn(name = "my_product", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "my_category", referencedColumnName = "id"))
  private Set<Category> categories = new HashSet<Category>();

}
