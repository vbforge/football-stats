# Football Statistics App

A simple Spring Boot application for tracking football statistics.

---

## Features

- Add player actions (goals and assists) for specific match days
- Create new players and clubs
- View comprehensive player statistics table
- Calculate tournament points (1 point for goal + 1 point for assist)
- Track goal and assist streaks for each player
- Filter statistics by club
- Responsive web interface using Bootstrap

## Technology Stack

- **Backend:** Spring Boot 3.1.5, Java 17
- **Database:** MySQL with JPA/Hibernate
- **Frontend:** Thymeleaf, Bootstrap 5, Font Awesome
- **Tools:** Lombok, Maven

---

## Setup Instructions

### 1. Database Setup
Create a MySQL database and update the connection details in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/football_stats?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

---

### 2. Project Structure (in process)
```
src/main/java/com/example/footballstats/
├── FootballStatsApplication.java          # Main Spring Boot application
├── controller/
│   └── FootballStatsController.java       # Web controller
├── service/
│   └── FootballStatsService.java          # Business logic
├── repository/
│   ├── PlayerRepository.java              # Player data access
│   ├── ClubRepository.java                # Club data access  
│   ├── MatchDayRepository.java            # Match day data access
│   └── ActionRepository.java              # Action data access
├── entity/
│   ├── Player.java                        # Player entity
│   ├── Club.java                          # Club entity
│   ├── MatchDay.java                      # Match day entity
│   └── Action.java                        # Action entity
└── dto/
    ├── PlayerStatisticsDTO.java           # Statistics transfer object
    ├── ActionFormDTO.java                 # Form transfer object
    └── StreakResultDTO.java               # Streak calculation result

src/main/resources/
├── application.properties                 # Application configuration
└── templates/
    ├── index.html                         # Main page (add actions)
    └── statistics.html                    # Statistics page
```

---

### 3. Running the Application

1. Make sure MySQL is running
2. Update database credentials in `application.properties`
3. Run the application:
```bash
mvn spring-boot:run
```
4. Open your browser and navigate to `http://localhost:8080`

---

## Usage

### Adding Player Actions
 * 
 * 

### Viewing Statistics
 * 
 * 

---

## Key Features Explained

### Streak Calculation
The app calculates the maximum consecutive match days where a player scored goals or made assists. If a player doesn't score/assist in a match day, their streak resets.

### Tournament Points System  
- 1 point for each goal
- 1 point for each assist
- Players are ranked by total points in descending order

### Flexible Data Entry
- Add new players and clubs dynamically
- No need to pre-register players or clubs
- Easy match day tracking

---

## Future Enhancements

Here are some features you can add later:
- Match details (opponent, score, venue)
- Player positions and more detailed stats
- Season management
- Data export functionality
- Advanced analytics and charts
- Player comparison features

---

## Troubleshooting

1. **Database Connection Issues:** Make sure MySQL is running and credentials are correct
2. **Port Already in Use:** Change `server.port` in `application.properties`
3. **Lombok Issues:** Make sure Lombok is properly installed in your IDE
4. **Bootstrap Not Loading:** Check internet connection for CDN resources