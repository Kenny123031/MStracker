# MStracker 🎬

MStracker is a modern, responsive Android application designed for movie and TV series enthusiasts. It allows users to search the global TMDB database, track their progress, and manage a personal library with local data persistence.

## ✨ Features

- **TMDB Integration:** Search for any movie or TV show using the TMDB API.
- **Local Library:** Save items to "Plan to Watch", "Watching", or "Completed" using Room Database.
- **Completion Tracking:** Automatically records the date when an item is marked as "Completed".
- **Visual Statistics:** Color-coded progress bars (Blue for Movies, Green for Series) and average ratings.
- **Modern UI:** Dark-themed aesthetic (#0A0A0F) with gold accents (#E8C547) and smooth "pill-style" components.
- **Dynamic Profile:** Showcases "Top Picks" (4+ stars) and a dynamic genre cloud based on your watch history.

## 🛠️ Tech Stack

- **Language:** Java (Android)
- **Networking:** Retrofit 2 & GSON
- **Database:** Room Persistence Library
- **Image Loading:** Glide 4
- **UI Components:** Material Design, ConstraintLayout, CardView, Custom Drawables

## ⚙️ Setup Instructions

1.  **Clone the repo:**
    ```bash
    git clone https://github.com/YOUR_USERNAME/MStracker.git
    ```
2.  **Add API Key:**
    The app uses TMDB. The `API_KEY` is located in the source code. For production, consider moving this to `local.properties`.
3.  **Build:**
    Open the project in **Android Studio** and sync Gradle.

## 📝 License
This project is for educational purposes. Movie data provided by [TheMovieDB](https://www.themoviedb.org/).
