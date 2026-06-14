package com.deltaclause.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "internships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Internship {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String detail;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String duration; // e.g., "4 Weeks", "8 Weeks"

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "internship_domains", joinColumns = @JoinColumn(name = "internship_id"))
    @Column(name = "domain")
    @Builder.Default
    private List<String> domains = new ArrayList<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    @Builder.Default
    private List<TaskSheetItem> taskSheets = new ArrayList<>();
}
