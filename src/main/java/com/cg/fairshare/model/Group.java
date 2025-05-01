package com.cg.fairshare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Don’t recurse back into Group → createdBy → Group
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    // The “parent” side of Group → Participant
    @JsonManagedReference
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @JsonBackReference
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<Expense> expenses;
}
