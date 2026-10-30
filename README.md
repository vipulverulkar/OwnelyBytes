# Ovenly Bytes - Standalone Android POS & Bakery Management App 🧁🎂

> **"Freshly Baked With Love ❤️ • 100% Eggless"**  
> A modern, offline-first Android bakery POS & management app built with **Kotlin**, **Jetpack Compose (Material 3)**, and **SQLite (Room Database)**.

---

## 🚀 Key Features

### 1. WhatsApp & Phone Call Order Tracking
- **Channel Indicators**: Distinct color badges (🟢 **WhatsApp**, 🔵 **Phone Call**, 🟣 **Walk-in**).
- **One-Tap WhatsApp Receipt Generator**: Formats itemized order summaries with weights, piped cake message, advance paid, and balance due.
- **Automated Customer Updates**:
  - *Order Confirmation & Invoice*
  - *Baking Progress Notice* ("Your cake is baking with love in our oven!")
  - *Ready for Pickup / Out for Delivery Alert*
- **Native Phone Dialer**: Instant call to customer from order card or customer directory.

### 2. Shop Details & Branding Customization
- **Custom Shop Profile**: Configure shop name, tagline, owner name, WhatsApp contact, email, address, FSSAI license number, and GSTIN.
- **100% Eggless Badge**: Toggleable global eggless badge displayed across top app bar and receipts.

### 3. Multi-Format Database Backup & Restore (SQL / CSV / JSON)
- **SQL Script Dump (.sql)**: Exports complete database as standard SQL `INSERT INTO` statements. Imports and executes SQL scripts.
- **CSV Spreadsheet (.csv)**: Exports records into multi-section CSV files compatible with Microsoft Excel & Google Sheets.
- **JSON Backup (.json)**: Full structured JSON document backup/restore.
- **Auto-Format Detection**: Instant restore from any `.sql`, `.csv`, or `.json` file selected via document picker.

### 4. 5 Bakery Color Themes
- 🍫 **Artisan Cocoa** (Classic Warm Bakery Cocoa & Caramel - Default)
- 🍓 **Berry Velvet** (Red Velvet Burgundy & Strawberry Pink)
- 🍯 **Golden Vanilla** (Honey Amber & Baked Pastry Gold)
- 🍵 **Pistachio Matcha** (Fresh Green Tea & Pistachio)
- 🌙 **Dark Mocha** (Espresso & Dark Chocolate Mode)

### 5. Dynamic Menu Catalog with Visual Icons
- **Visual Item Badges**: Dedicated icons for Classic Cakes (🎂), Premium Collection (✨), Brownies (🥮), Tea Cakes (☕), and Cookies (🍪).
- **Multi-Tier Portion Pricing**: Flexible size pairs (½ KG / 1 KG, 250 g / 500 g) or customizable custom variant names and market rates.

### 6. Customer Directory & CRM
- Customer profiles with phone numbers, delivery landmarks, and dietary notes.
- Instant search and customer order history tracking.

### 7. Inventory Stock & Low Stock Alerts
- Ingredient tracking (Belgian Chocolate, Biscoff, Nutella, Butter, Flour, Cocoa).
- Packaging supply tracking (½ KG boxes, 1 KG boxes, ribbons, tins).
- Real-time **Low Stock Alert** notifications on the Dashboard.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.1
- **UI Framework**: Jetpack Compose with Material 3 & Navigation Compose
- **Database**: SQLite / Room 2.8 (Offline-First)
- **Architecture**: Clean MVVM with Unidirectional Data Flow (`StateFlow` & Coroutines)
- **Unit & Security Testing**: JUnit 4, Kotlin Coroutines Test, input sanitization, and SQL/CSV injection prevention.

---

## 🧪 Testing Summary

Ovenly Bytes includes comprehensive Unit, Integration, and Security Test coverage:
- **Run Tests Command**: `./gradlew app:testDebugUnit`
- **Pass Rate**: 100% Green (`18 passed, 0 skipped, 0 failed`)

---

## 📱 How to Run in Android Studio

1. Open **Android Studio** (Ladybug / Koala / Hedgehog or newer).
2. Open the root project directory (`OvenlyBytes`).
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator or connected device (Android 7.0 / API 24+).
5. Click **Run (▶)**!
