package com.cg.fairshare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Don’t recurse Participant → User → Participant
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Don’t recurse Participant → Group → Participant
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private LocalDateTime joinedAt;
}
