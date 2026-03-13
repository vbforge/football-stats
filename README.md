# ⚽ Football Statistics App

**A full-stack Spring Boot web application for tracking football league statistics — players, clubs, match days, seasons, standings, and more.**

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Template%20Engine-005F0F?logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-7952B3?logo=bootstrap&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)

---

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Data Model](#data-model)
- [Project Structure](#project-structure)
- [Quick Start with Docker](#quick-start-with-docker)
- [Running Locally (Development)](#running-locally-development)
- [Docker Architecture Explained](#docker-architecture-explained)
- [Database Backup & Data Updates](#database-backup--data-updates)
- [Updating the App to a New Version](#updating-the-app-to-a-new-version)
- [Troubleshooting](#troubleshooting)
- [Author](#author)

---

## Introduction

**Football Statistics App** is a personal league management tool built to track real football statistics across seasons. It provides a clean web interface for managing clubs, players, match days, and games — with automatic standings calculation, player scoring streaks, and paginated statistics tables.

The app ships with **real pre-loaded data** (`fs-app.sql`) so you can explore it immediately after running — no manual setup required.

---

## Features

### 🏆 League & Season Management
- Create and manage multiple seasons
- Set a current active season
- Automatic league standings calculation based on game results
- Win/Draw/Loss tracking with points, goal difference, and goals for/against

### ⚽ Game Management
- Schedule and record games between clubs
- Track home/away results with scores
- Prevent duplicate games within the same match day
- Filter scheduled vs finished games

### 👤 Player Management
- Full CRUD for players with validation
- Track player position, nationality, date of birth, shirt number
- Per-club shirt number uniqueness enforcement
- Player detail pages with individual statistics

### 📊 Statistics Engine
- Paginated player statistics table (sortable by goals, assists, points, streaks)
- Filter statistics by club
- Consecutive goal streak calculation per player (across 38 match days)
- Consecutive assist streak calculation per player
- Combined streak tracking (goals OR assists in consecutive match days)
- Top scorers and top assisters per club
- Streak leaderboards

### 🏟️ Club Management
- Club detail pages with full info (stadium, coach, colors, founded year)
- Club-level standings (position, matches played, W/D/L, points, goal difference)
- Top 5 scorers and assisters per club

### 🌆 City Management
- Cities with coordinates and population data
- Club-city relationships
- Search and filter cities by name or population range

### 🎯 Match Day Actions
- Record goals and assists per player per match day
- Update existing actions or create new ones
- Actions scoped to the current active season

### 🎨 UI Features
- Responsive Bootstrap 5 interface
- Font Awesome icons
- Pagination controls on statistics tables
- Dynamic sorting on all columns

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.5 |
| Language | Java 17 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate 6 |
| Template Engine | Thymeleaf |
| Frontend | HTML5, CSS3, JavaScript |
| UI Framework | Bootstrap 5, Font Awesome |
| Build Tool | Maven |
| Containerization | Docker, Docker Compose |
| Utilities | Lombok |

---

## Data Model

```
┌──────────┐       ┌───────────┐       ┌──────────────┐
│  Season  │──────<│ MatchDay  │──────<│    Action    │
├──────────┤       ├───────────┤       ├──────────────┤
│ id       │       │ id        │       │ id           │
│ name     │       │ number    │       │ goals        │
│ start    │       │ season_id │       │ assists      │
│ end      │       └───────────┘       │ player_id    │
│ isCurrent│                           │ match_day_id │
└──────────┘                           └──────────────┘
                                              │
┌──────────┐       ┌───────────┐             │
│  City    │──────<│   Club    │──────<┌──────────────┐
├──────────┤       ├───────────┤       │    Player    │
│ id       │       │ id        │       ├──────────────┤
│ name     │       │ name      │       │ id           │
│ population│      │ coach     │       │ name         │
│ coord_x  │       │ stadium   │       │ position     │
│ coord_y  │       │ city_id   │       │ nationality  │
└──────────┘       │ logoPath  │       │ dateOfBirth  │
                   │ founded   │       │ shirtNumber  │
                   └───────────┘       │ club_id      │
                        │              └──────────────┘
                   ┌────┴────┐
              ┌────┴───┐ ┌───┴────┐
              │  Game  │ │  Game  │ (home/away)
              ├────────┤ └────────┘
              │ home_id│
              │ away_id│
              │ season │
              │ matchDay│
              │ status │
              │ homeGoals│
              │ awayGoals│
              └────────┘
```

---

## Project Structure

```
src/main/java/com/vbforge/footballstats/
├── FootballStatsApplication.java
├── config/
├── controller/
├── dto/
│   ├── action/         # PlayerStatisticsDTO, ActionFormDTO, StreakResultDTO, etc.
│   ├── club/           # ClubDetailDTO
│   ├── game/           # GameFormDTO
│   ├── league/         # ClubStandingsDTO
│   └── player/         # PlayerDTO, PlayerStatsDTO
├── entity/             # Player, Club, City, Season, MatchDay, Game, Action
├── exception/
├── mapper/
├── repository/
└── service/
    └── impl/           # Full service layer implementations

src/main/resources/
├── application.properties          # Profile switcher only
├── application-dev.properties      # Local dev config (gitignored)
└── templates/                      # Thymeleaf HTML templates
```

---

## Quick Start with Docker

This is the recommended way to run the app. **No Java or MySQL installation required.**

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/vbforge/football-stats.git
cd football-stats
```

**2. Create the `.env` file** in the project root:
```env
DB_USERNAME=tracker_user
DB_PASSWORD=yourpassword123
DB_ROOT_PASSWORD=yourrootpassword123
```
> ⚠️ Choose any passwords you like — just keep them consistent in this file.
> This file is gitignored and stays on your machine only.

**3. Start the app**
```bash
docker-compose up
```

**4. Open in browser**
```
http://localhost:8082
```

That's it! The app starts with **real pre-loaded football data** automatically. ✅

---

### Stopping the app

```bash
# In terminal (Ctrl+C first, then):
docker-compose down

# Or use Docker Desktop — find the container and click Stop
```

> ⚠️ Never use `docker-compose down -v` — the `-v` flag deletes your data volume!

---

### Starting again (Docker Desktop)

1. Open Docker Desktop
2. Go to **Containers**
3. Find `football-stats` group
4. Click ▶️ **Play**
5. Open `http://localhost:8082`

---

## Running Locally (Development)

For development in IntelliJ IDEA with a local MySQL instance.

### Prerequisites
- Java 17
- Maven 3.6+
- MySQL 8.0 running locally

### Steps

**1. Create the database**
```sql
CREATE DATABASE football_stats;
```

**2. Create `application-dev.properties`** at `src/main/resources/`:
```properties
spring.application.name=football-stats

spring.datasource.url=jdbc:mysql://localhost:3306/football_stats?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_local_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

spring.thymeleaf.cache=false
server.port=8080
```
> ⚠️ This file is gitignored — it stays local only, never pushed to GitHub.

**3. Run**
```bash
mvn spring-boot:run
```

**4. Open**
```
http://localhost:8080
```

---

## Docker Architecture Explained

Understanding how the pieces fit together:

```
GitHub Repository                    DockerHub
─────────────────                    ─────────────────────────────
Source code (.java, .xml)            Built Docker image
docker-compose.yml        →build→    vladbogdadocker/football-stats
fs-app.sql (data dump)               (Spring Boot app, ready to run)
.gitignore
README.md
```

### Two independent components:

| Component | What it is | Where it lives | Changes when |
|---|---|---|---|
| Docker Image | The compiled Spring Boot app | DockerHub | You change Java code |
| `fs-app.sql` | Database snapshot with data | GitHub | You update season data |

They are **completely independent** — updating data never requires rebuilding the image, and updating code never affects data. ✅

### Data persistence:

```
docker-compose up
      ↓
MySQL container starts
      ↓
First run? → loads fs-app.sql automatically → data appears ✅
Already has volume? → skips init → uses existing data ✅
      ↓
Your data lives in Docker Volume: mysql_data_football
      ↓
Stored on your local disk (managed by Docker)
```

### Data safety table:

| Action | Data Safe? |
|---|---|
| `docker-compose down` | ✅ Yes |
| Stop container in Docker Desktop | ✅ Yes |
| Delete container | ✅ Yes |
| Delete image | ✅ Yes |
| Restart PC | ✅ Yes |
| Pull new app version | ✅ Yes |
| `docker-compose down -v` | ❌ NO — deletes volume! |
| `docker volume rm football-stats_mysql_data_football` | ❌ NO — deletes volume! |

---

## Database Backup & Data Updates

This section covers how to export your local data and publish it so everyone gets the latest version.

### When to do this
- End of a season — all match day results are recorded
- Mid-season update — significant new data added
- Any time you want to share the latest state with others

---

### Step 1 — Export backup from MySQL Workbench

1. Open **MySQL Workbench**
2. Go to **Server → Data Export**
3. Select database: `football_stats`
4. Select all tables
5. Choose **"Export to Self-Contained File"**
6. Set output path to your project root, filename: `fs-app.sql`
7. Check **"Include Create Schema"** → OFF (database is created by Docker)
8. Click **"Start Export"**

**Or via terminal:**
```bash
mysqldump -u root -p football_stats > fs-app.sql
```

---

### Step 2 — Replace and push

```bash
# The new fs-app.sql is already in project root (overwritten by export)
git add fs-app.sql
git commit -m "Update data - Season 2025/26 final standings"
git push
```

---

### Step 3 — What happens for existing users

> ⚠️ Important: MySQL only loads `fs-app.sql` on a **fresh volume**.
> If someone already ran the app before, they need to reset their volume to get new data:

```bash
docker-compose down
docker volume rm football-stats_mysql_data_football
docker-compose up
```

This wipes their local data and reloads from the new `fs-app.sql`. Worth mentioning when you share an update!

---

### Summary: What requires what

| Change type | Action needed |
|---|---|
| New season data | Export → replace `fs-app.sql` → push to GitHub |
| Bug fix / new feature | Rebuild image → push to DockerHub |
| Both code and data changed | Do both steps above |

---

## Updating the App to a New Version

When you make code changes and want to publish a new version:

```bash
# 1. Rebuild the image
docker build -t vladbogdadocker/football-stats:latest .

# 2. Push to DockerHub
docker push vladbogdadocker/football-stats:latest

# 3. Restart containers to use new image
docker-compose down
docker-compose up
```

For others to get the update:
```bash
docker-compose down
docker pull vladbogdadocker/football-stats:latest
docker-compose up
```

---

## Troubleshooting

**App fails — `Access denied for user`**
- Check your `.env` file exists at project root
- Make sure `DB_USERNAME` and `DB_PASSWORD` match between `app` and `mysql` sections
- If you changed credentials after first run, wipe the volume and restart:
  ```bash
  docker-compose down
  docker volume rm football-stats_mysql_data_football
  docker-compose up
  ```

**App fails — `profile dev is active` but DB connection refused**
- The image was built with `application-dev.properties` embedded
- Make sure `application.properties` contains only: `spring.profiles.active=${SPRING_PROFILE:dev}`
- Rebuild the image after fixing: `docker build -t vladbogdadocker/football-stats:latest .`

**Port 8082 already in use**
- Change `"8082:8080"` to `"8083:8080"` in `docker-compose.yml`
- Access via `http://localhost:8083`

**New data not showing after pulling updated `fs-app.sql`**
- MySQL only runs init scripts on a fresh volume
- Reset volume:
  ```bash
  docker-compose down
  docker volume rm football-stats_mysql_data_football
  docker-compose up
  ```

**MySQL timezone warnings in logs**
- These are cosmetic warnings about missing timezone files — they don't affect functionality, safe to ignore ✅

---

## Author

**`vbforge`**
- [GitHub](https://github.com/vbforge)
- [LinkedIn](https://www.linkedin.com/in/vlad-bogdantsev-7897662b2/)
