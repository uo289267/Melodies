# 🎵 Melodies – Sheet Music Learning Assistant for Tablets

> **Melodies** is an Android application designed to assist students in learning how to read and practice sheet music using their tablets.  
> Built with **Kotlin** and **Firebase**, it focuses on **independent learning**, **real-time feedback**, and a **smooth, intuitive experience** for recorder music students.

---

## 📱 Features

- 🎼 **Digital Sheet Visualization**  
  Import, organize, and view your sheet music in a clean, paginated interface.

- 📂 **Folder-Based Organization**  
  Keep your music sorted by folders, making it easy to access your repertoire.

- 🔊 **Feedback System** *(prototype)*  
  The app analyzes the notes played and provides feedback to improve pitch accuracy.

- ☁️ **Cloud Storage with Firestore**
   Easily login in new devices and keep the folders you already have.

- 🌙 **Future Work**  
  - Offline mode  
  - Dark theme  
  - Enhanced feedback  
  - Pre-saved SVG rendering 

---

## 🧠 Project Context

This project was developed as a **Final Degree Project (TFG)** for the  
**Software Engineering Degree at the University of Oviedo**.

> The goal is to **enhance music education** by bringing technology closer to students —  
> enabling them to **practice independently** while still receiving meaningful feedback.

---

## ⚙️ Tech Stack

| Component | Technology |
|------------|-------------|
| **Language** | Kotlin |
| **Architecture** | MVVM (Model–View–ViewModel) |
| **Database** | Firebase Firestore |
| **UI Design** | Android XML + ViewBinding |
| **Testing** | Unit, Integration & Usability tests |
| **Version Control** | Git & GitHub |

---

## 📂 Project Structure

- `tfg.uniovi.melodies/`
  - `application/` — Core app setup and main entry point (e.g. `MelodiesApplication.kt`)
  - `fragments/` — UI Fragments (Home, Library, Import, Profile…)
    - `adapters/` — RecyclerView adapters
    - `utils/` — UI helpers and decorators
    - `viewmodels/` — Shared and scoped ViewModels (MVVM)
  - `model/` — Data classes (Folder, Sheet, Note…)
    - `notes/` — Note Entities
  - `preferences/` — User settings and configuration manager
  - `processing/` — Pitch Detector, Checker and XMLParser
    - `parser/` — Note Entities
  - `repositories/` — Data access layer
  
- `assets/` — Static resources and MusicXML test files
  
- `res/` — Android resources
  - `drawable/` — Icons and images
  - `layout/` — XML layouts
  - `menu/` — Menu XMLs
  - `mipmap/` — Launcher icons
  - `navigation/` — Navigation graph
  - `values/` — strings, colors, themes
  
## 📋 Credits
 - **Verovio Toolkit** — Licensed under **GNU GPL-3.0**  
  Copyright © RISM Digital Center  
  Repository: [https://github.com/rism-digital/verovio](https://github.com/rism-digital/verovio)

- **TarsosDSP** — Licensed under **GNU GPL-3.0**  
  Copyright © 2011–2025 Joren Six and contributors  
  Repository: [https://github.com/JorenSix/TarsosDSP](https://github.com/JorenSix/TarsosDSP)
  
- **Bravura Font** — Licensed under the **SIL Open Font License 1.1 (OFL-1.1)**  
  Copyright © Steinberg Media Technologies GmbH  
  Repository: [https://github.com/steinbergmedia/bravura](https://github.com/steinbergmedia/bravura)

- Icons
  <a href="https://www.flaticon.es/iconos-gratis/clave-de-sol" title="clave de sol iconos">Clave de sol iconos creados por Freepik - Flaticon</a>

© 2025 Lucía Ruiz Núñez — This documentatios is licensed under **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0)**.

