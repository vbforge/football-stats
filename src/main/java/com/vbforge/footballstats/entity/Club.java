package com.vbforge.footballstats.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clubs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Player> players;

    @Column
    private String coach;

    @Column(name = "logo_path")
    private String logoPath;

    // Replace String city with City entity relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", referencedColumnName = "id")
    private City city;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Column
    private String stadium;

    @Column(name = "stadium_capacity")
    private Integer stadiumCapacity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String website;

    @Column
    private String nickname;

    @Column(name = "stadium_image_path")
    private String stadiumImagePath;

    @Column(name = "primary_color")
    private String primaryColor;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @OneToMany(mappedBy = "homeClub", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Game> homeGames;

    @OneToMany(mappedBy = "awayClub", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Game> awayGames;

    // Convenience method to get city name (for backward compatibility)
    public String getCityName() {
        return city != null ? city.getName() : null;
    }

}
