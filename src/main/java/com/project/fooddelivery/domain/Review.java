package com.project.fooddelivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private CustomerOrder order;

    @Column(nullable = false)
    private String customerUsername;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String comment;

    protected Review() {
    }

    public Review(CustomerOrder order, String customerUsername, Integer rating, String comment) {
        this.order = order;
        this.customerUsername = customerUsername;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public CustomerOrder getOrder() {
        return order;
    }

    public String getCustomerUsername() {
        return customerUsername;
    }

    public Integer getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
