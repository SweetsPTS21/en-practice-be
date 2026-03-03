package com.swpts.enpracticebe.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ielts_sections", uniqueConstraints = @UniqueConstraint(columnNames = { "test_id", "section_order" }))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IeltsSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "test_id", nullable = false)
    private UUID testId;

    @Column(name = "section_order", nullable = false)
    private Integer sectionOrder;

    @Column(length = 500)
    private String title;

    @Column(name = "audio_url", length = 1000)
    private String audioUrl;

    @Column(columnDefinition = "TEXT")
    private String instructions;
}
