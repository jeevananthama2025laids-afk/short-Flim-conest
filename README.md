# FilmJury Backend

FilmJury is a Java 17, Spring Boot, Maven application with a same-origin dashboard and REST API. The dashboard runs at `http://localhost:8080`; API routes are under `/api`. It uses MySQL database `filmjury_db`.

## Project Structure

Java source is under `src/main/java/com/example/filmjury/`:

- `controller/`: REST endpoints
- `service/`: entry, judge, scorecard, and leaderboard rules
- `repository/`: Spring Data JPA repositories
- `entity/`: Entry, Judge, Criterion, and ScoreCard
- `dto/`: validated request and response objects
- `exception/`: resource, business-rule, and validation errors

The dashboard is served from `src/main/resources/static/` and provides Overview, Film Entries, Jury Desk, and Leaderboard views.

JPA creates and updates the tables when the application starts. The four fixed criteria are inserted if missing.

## MySQL Workbench Setup

1. Install and start MySQL Server, then open MySQL Workbench.
2. Open your local MySQL connection (commonly `Local instance MySQL80`) and enter the MySQL root password.
3. Open a SQL tab, or choose **File > Open SQL Script** and open `create-filmjury-db.sql`, then run:

```sql
CREATE DATABASE filmjury_db;
```

4. Click the lightning-bolt execute button. Refresh the Schemas panel; `filmjury_db` should appear. Do not create tables manually.

After the application starts, inspect the data created through the API in Workbench:

```sql
USE filmjury_db;
SHOW TABLES;
SELECT * FROM film_entries;
SELECT * FROM judges;
SELECT * FROM entry_judges;
SELECT * FROM score_cards;
SELECT * FROM score_card_scores;
```

The application connects as `root` to `jdbc:mysql://localhost:3306/filmjury_db`. Set the password in PowerShell before running the app:

```powershell
$env:MYSQL_PASSWORD = "your actual MySQL password"
```

Alternatively, replace `YOUR_MYSQL_PASSWORD` in `src/main/resources/application.properties`. Do not commit a real password.

## Run in VS Code

1. Install JDK 17.
2. Install Visual Studio Code.
3. Install **Extension Pack for Java** and **Spring Boot Extension Pack**.
4. Open the project folder containing `pom.xml` in VS Code.
5. Ensure VS Code and Maven use JDK 17; set `JAVA_HOME` to the JDK 17 installation if needed.
6. Create the database and configure the MySQL password as described above.
7. In the VS Code terminal, run:

```powershell
.\mvnw.cmd spring-boot:run
```

8. When the startup log says the application has started, open `http://localhost:8080` for the dashboard. The REST API remains available under `/api`, as listed below.

## API Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/api/entries` | Create an entry |
| GET | `/api/entries` | List entries |
| GET | `/api/entries/{id}` | Get an entry |
| PUT | `/api/entries/{id}` | Update an entry |
| DELETE | `/api/entries/{id}` | Delete an entry |
| POST | `/api/judges` | Create a judge |
| GET | `/api/judges` | List judges |
| GET | `/api/judges/{id}` | Get a judge |
| POST | `/api/entries/{entryId}/judges/{judgeId}` | Assign a judge |
| POST | `/api/entries/{entryId}/judges/{judgeId}/score` | Submit one scorecard |
| GET | `/api/entries/{entryId}/score` | Get the entry's judge-average score |
| GET | `/api/leaderboard` | List scored entries by average descending |

## Postman Walkthrough

In Postman, choose **Import > Files** and select `FilmJury.postman_collection.json`. Start Spring Boot first, then open the **FilmJury Local API** collection and run its requests in order. The collection stores the created entry and judge IDs automatically, checks response statuses, validates the duplicate-score conflict and average calculations, and uses `http://localhost:8080` as its base URL. For manual requests, select **Body > raw > JSON** and set `Content-Type: application/json`. The examples assume new database IDs start at 1.

### 1. Create Entry

`POST http://localhost:8080/api/entries`

```json
{
  "title": "The Last Hope",
  "genre": "Drama",
  "videoLink": "https://example.com/video"
}
```

Expected `201 Created` response:

```json
{
  "id": 1,
  "title": "The Last Hope",
  "genre": "Drama",
  "videoLink": "https://example.com/video"
}
```

### 2. Create Judge One

`POST http://localhost:8080/api/judges`

```json
{
  "name": "Judge One",
  "email": "judge1@gmail.com"
}
```

Expected `201 Created` response:

```json
{
  "id": 1,
  "name": "Judge One",
  "email": "judge1@gmail.com"
}
```

### 3. Create Judge Two

`POST http://localhost:8080/api/judges`

```json
{
  "name": "Judge Two",
  "email": "judge2@gmail.com"
}
```

Expected `201 Created` response:

```json
{
  "id": 2,
  "name": "Judge Two",
  "email": "judge2@gmail.com"
}
```

### 4. Assign Judge One to the Entry

`POST http://localhost:8080/api/entries/1/judges/1`

Expected `200 OK` response:

```json
{
  "message": "Judge assigned to entry."
}
```

### 5. Submit Judge One's Scorecard

`POST http://localhost:8080/api/entries/1/judges/1/score`

```json
{
  "scores": {
    "Story": 8,
    "Direction": 9,
    "Acting": 7,
    "Cinematography": 8
  }
}
```

Expected `200 OK` response:

```json
{
  "entryId": 1,
  "title": "The Last Hope",
  "averageScore": 8.0
}
```

### 6. Try the Duplicate Scorecard

Send the same request from step 5 again. Expected `409 Conflict` response:

```json
{
  "message": "Judge has already submitted a scorecard for this entry."
}
```

### 7. Get the Entry Average

`GET http://localhost:8080/api/entries/1/score`

Expected `200 OK` response:

```json
{
  "entryId": 1,
  "title": "The Last Hope",
  "averageScore": 8.0
}
```

### 8. Assign Judge Two and Submit a Scorecard

First assign Judge Two: `POST http://localhost:8080/api/entries/1/judges/2`

Expected `200 OK` response:

```json
{
  "message": "Judge assigned to entry."
}
```

Then submit:

`POST http://localhost:8080/api/entries/1/judges/2/score`

```json
{
  "scores": {
    "Story": 9,
    "Direction": 9,
    "Acting": 9,
    "Cinematography": 9
  }
}
```

Expected `200 OK` response (the mean of Judge One's 8.0 and Judge Two's 9.0 scorecard averages):

```json
{
  "entryId": 1,
  "title": "The Last Hope",
  "averageScore": 8.5
}
```

### 9. Get the Updated Average

`GET http://localhost:8080/api/entries/1/score`

Expected `200 OK` response:

```json
{
  "entryId": 1,
  "title": "The Last Hope",
  "averageScore": 8.5
}
```

### 10. Get the Leaderboard

`GET http://localhost:8080/api/leaderboard`

Expected `200 OK` response:

```json
[
  {
    "rank": 1,
    "entryId": 1,
    "title": "The Last Hope",
    "genre": "Drama",
    "averageScore": 8.5
  }
]
```

An entry with no scorecards returns `"averageScore": null` and is omitted from the leaderboard. Invalid request fields and out-of-range scores return `400 Bad Request`; missing entries or judges return `404 Not Found`; duplicate scorecards return `409 Conflict`.
