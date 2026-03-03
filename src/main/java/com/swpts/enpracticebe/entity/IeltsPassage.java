package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ielts_passages", uniqueConstraints = @UniqueConstraint(columnNames = { "section_id", "passage_order" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IeltsPassage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "passage_order", nullable = false)
    private Integer passageOrder;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
}
