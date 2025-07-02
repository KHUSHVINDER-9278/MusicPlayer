# MusicPlayer
# 🎵 Java Swing Music Player

A simple desktop music player built using Java Swing that supports:
- Playback of `.wav` audio files
- Adding/removing songs to/from a **Favorites** list
- Persistent favorites storage using `favorites.txt`
- Auto-scanning your system for `.wav` files

---

## 🚀 Features

- 🔁 **Play, Pause, Stop** audio playback
- 🌟 **Add to Favorites** with a click
- 📂 **Auto-scans** your system for `.wav` files
- 🗃️ **Persistence**: Favorites are saved on exit and loaded on launch
- 🖱️ Double-click to play songs from either list

---

## 🖼️ GUI Overview

- Left panel: All `.wav` audio files found
- Right panel: Your favorite songs
- Bottom buttons: Playback and song management controls
- Status label: Shows current action or status

---

## 🛠 Requirements

- Java 8 or higher
- `.wav` audio files in your system

---

## 🧰 How It Works

- On startup, it:
  - Loads favorite songs from `favorites.txt`
  - Recursively scans your home directory for `.wav` files
- Songs can be:
  - Double-clicked to play
  - Added to favorites with the **"Add to Favorites"** button
  - Removed from lists with the appropriate buttons
- On application exit, favorites are saved back to `favorites.txt` automatically

---

## 📂 Project Structure

