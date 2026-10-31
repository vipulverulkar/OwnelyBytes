# Ovenly Bytes - Standalone Android POS & Bakery Management App 🧁🎂

> **"Freshly Baked With Love ❤️ • 100% Eggless"**  
> A modern, offline-first, hardware-encrypted Android bakery POS & management app built with **Kotlin**, **Jetpack Compose (Material 3)**, **SQLCipher**, and **SQLite (Room Database)**.

---

## 🚀 Key Features

### 1. Hardware-Encrypted Security & Android 15/16 16 KB Compliance
- **256-Bit AES SQLCipher Database**: Entire SQLite database (`ovenly_bites_database.db`) is encrypted at rest on disk.
- **Android Keystore & EncryptedSharedPreferences**: Shop profile settings, phone numbers, and keys are encrypted using hardware-backed Master Keys (`MasterKey`).
- **Android 15/16 16 KB Page Alignment**: Native C++ shared libraries (`libsqlcipher.so` 4.6.1) aligned for 16 KB memory page size hardware requirements.
- **Zero Cloud Leakage**: 100% offline-first architecture—customer contacts, orders, and sales data never leave the phone.

### 2. Delivery Policy & Same-Day Express Delivery Option
- **Explicit Booking & Delivery Date Display**: Every order card displays both **Booking Date** and **Delivery Date** clearly (e.g. `Booked: 24 Oct 2024 • Deliv: 26 Oct 2024 (5:00 PM)`).
- **1-Day Advance Notice Policy**: Standard minimum delivery date requires at least 1 day advance notice.
- **⚡ Same-Day Express Delivery Toggle**: Toggle switch (`⚡ Allow Same-Day Express Delivery`) enables today's delivery date for urgent same-day orders.

### 3. WhatsApp & Phone Call Order Tracking
- **Channel Indicators**: Distinct color badges (🟢 **WhatsApp**, 🔵 **Phone Call**, 🟣 **Walk-in**).
- **One-Tap WhatsApp Receipt Generator**: Formats itemized order summaries with weights, piped cake message, advance paid, and balance due.
- **Automated Customer Updates**:
  - *Order Confirmation & Invoice*
  - *Baking Progress Notice* ("Your cake is baking with love in our oven!")
  - *Ready for Pickup / Out for Delivery Alert*
- **Native Phone Dialer**: Instant call to customer from order card or customer directory.

### 4. Order Sorting & Advanced Filtering
- **Multi-Option Order Sorting**: Sort orders by **Delivery Date**, **Order ID**, **Total Amount**, or **Balance Due**.
- **Status Filters**: Quick filtering by *All*, *Received*, *Baking*, *Ready*, *Delivered*, and *Cancelled*.

### 5. Unlimited Custom Portion Sizes & Flexible Pricing
- **Dynamic Portion Variants**: Add unlimited custom portion sizes and prices (e.g., *½ KG, 1 KG, 1.5 KG, 2 KG, 250 g, 500 g, 6-Inch, Custom*) to any menu item.
- **On-The-Fly Custom Overrides**: Override prices or add ad-hoc custom items when placing or editing orders.

### 6. Advanced Financial Reports & Revenue Analytics
- **Primary KPIs**: Total Orders, Total Revenue, Total Pending Balance Due.
- **Advanced Metrics**: **Average Order Value (AOV)** and **Payment Collection Rate (%)**.
- **Sales by Bakery Category**: Visual breakdown displaying revenue share across *Classic Cakes*, *Premium Collection*, *Brownies*, *Cookies*, etc.
- **Time Comparison & Trends**: Compare Today, This Month, and This Year with daily, monthly, and annual sales progress bars.

### 7. Multi-Format Database Backup & Intelligent Restore (SQL / CSV / JSON)
- **SQL Script Dump (.sql)**: Exports complete database as standard SQL `INSERT INTO` statements.
- **CSV Spreadsheet (.csv)**: Exports records into multi-section CSV files compatible with Excel & Google Sheets.
- **JSON Backup (.json)**: Full structured JSON document backup/restore.
- **Intelligent Auto-Format Detection**: Sniffs the first 1 KB of file header content to detect `.sql`, `.csv`, or `.json` formats automatically during restore.

### 8. 12 Bakery Color Themes with 3-Color Swatch Previews
- 🍫 **Artisan Cocoa** (Classic Warm Bakery Cocoa & Caramel - Default)
- 🍓 **Strawberry Shortcake** (Rose Pink & Berry Cream)
- 👑 **Royal Red Velvet** (Deep Red Velvet Cocoa & Cream Cheese)
- 🍯 **Golden Vanilla** (Honey Amber & Baked Pastry Gold)
- 🧈 **Salted Caramel** (Burnt Caramel & Butterscotch)
- 🍵 **Pistachio Matcha** (Fresh Green Tea & Pistachio)
- 🌲 **Emerald Mint** (Dark Chocolate & Peppermint Bark)
- 🍋 **Lemon Chiffon** (Citrus Yellow & Vanilla Chiffon)
- 🪻 **Lavender Earl Grey** (Earl Grey Tea & Lavender Bloom)
- 🫐 **Blueberry Macaron** (Lavender Blueberry & Sweet Cream)
- 🍑 **Peach Cobbler** (Apricot Peach & Cinnamon Bake)
- 🌙 **Midnight Truffle** (Gold Amber & Dark Truffle Mode)

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose with Material 3 & Navigation Compose
- **Database & Security**: SQLCipher 4.6.1 / SQLite / Room 2.8 with hardware MasterKey (`EncryptedSharedPreferences`)
- **Architecture**: Clean MVVM with Unidirectional Data Flow (`StateFlow` & Coroutines)
- **Unit Testing**: JUnit 4, Kotlin Coroutines Test, input sanitization, and SQL/CSV injection prevention.

---

## 🧪 Testing Summary

Ovenly Bytes includes comprehensive Unit, Integration, and Security Test coverage:
- **Run Tests Command**: `./gradlew test`
- **Pass Rate**: 100% Green (`20 passed, 0 skipped, 0 failed`)

---

## 📱 How to Run in Android Studio

1. Open **Android Studio** (Ladybug / Koala / Hedgehog or newer).
2. Open the root project directory (`OvenlyBytes`).
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator or connected device (Android 7.0 / API 24+).
5. Click **Run (▶)**!
