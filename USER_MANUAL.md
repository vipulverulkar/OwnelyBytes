# Ovenly Bytes - Comprehensive User Manual & Operations Guide 📖🧁

Welcome to **Ovenly Bytes**, your all-in-one, offline-first Android Bakery POS and Order Tracking App! This manual provides step-by-step instructions for managing your bakery, taking orders, tracking stock, customizing branding, backing up data in SQL/CSV/JSON formats, and customizing color themes.

---

## 📋 Table of Contents
1. [Overview & Quick Start](#1-overview--quick-start)
2. [Dashboard POS & Daily Operations](#2-dashboard-pos--daily-operations)
3. [Taking Orders & Sending WhatsApp Receipts](#3-taking-orders--sending-whatsapp-receipts)
4. [Order Status Lifecycle & Customer Updates](#4-order-status-lifecycle--customer-updates)
5. [Menu Catalog & Item Icon Management](#5-menu-catalog--item-icon-management)
6. [Inventory Stock & Low Stock Alerts](#6-inventory-stock--low-stock-alerts)
7. [Customer Directory & CRM](#7-customer-directory--crm)
8. [Shop Profile & Branding Settings](#8-shop-profile--branding-settings)
9. [Database Backup & Restore (SQL / CSV / JSON)](#9-database-backup--restore-sql--csv--json)
10. [Customizing App Color Themes](#10-customizing-app-color-themes)
11. [Security, Data Privacy & Best Practices](#11-security-data-privacy--best-practices)

---

## 1. Overview & Quick Start

Ovenly Bytes is designed for home bakers, artisan cake studios, and commercial bakeries to streamline order management without cloud subscription fees or internet dependency.

### Key Capabilities:
- **100% Offline Local Database**: All data is stored safely on your device using SQLite (Room DB).
- **WhatsApp Integration**: Generate and send formatted receipts, invoice confirmations, baking updates, and pickup alerts in one tap.
- **Multi-Format Backup**: Export and import your data anytime in **SQL (.sql)**, **CSV (.csv)**, or **JSON (.json)** format.
- **Custom Themes**: Switch between 5 warm bakery color palettes.

---

## 2. Dashboard POS & Daily Operations

When you launch Ovenly Bytes, the **Home Dashboard** displays your key operational indicators (KPIs):

- **Active Orders Summary**: Shows pending, baking, ready for pickup, and delivered order counts.
- **Financial Metrics**: Real-time total revenue and total balance pending collection.
- **Orders Due Today Alert**: Displays orders scheduled for delivery today.
- **Low Stock Banner**: Displays an urgent alert if raw ingredients or cake boxes drop below safety thresholds.

---

## 3. Taking Orders & Sending WhatsApp Receipts

To create a new order, tap the **"Take New Order"** button on the Dashboard or Orders list:

### Step 1: Order Channel & Lead Time Notice
1. Select the order channel: **WhatsApp (🟢)**, **Phone Call (🔵)**, or **Walk-in (🟣)**.
2. Select the delivery date and time slot. If the delivery date is within 48 hours, an advance notice reminder is displayed.

### Step 2: Customer Selection or Registration
1. Search and select an existing customer from your directory, or enter details for a new customer (Name, WhatsApp Phone Number, Delivery Address, Dietary Notes).

### Step 3: Item Selection & Customizations
1. Browse menu categories or search items.
2. Select portion weight/variant (e.g. ½ KG, 1 KG, 250 g, 500 g) and quantity.
3. Enter piped message on cake (e.g. *"Happy 30th Birthday Rahul ❤️"*).
4. Add customization decoration notes and extra charges if applicable.

### Step 4: Advance Payment & Balance Calculation
1. Enter advance amount paid and select payment mode (UPI, Cash, Card). The app automatically calculates the balance due.
2. Tap **"Save & Send WhatsApp Receipt"**. The order is recorded in SQLite and WhatsApp launches with a pre-formatted itemized invoice!

---

## 4. Order Status Lifecycle & Customer Updates

Each order progresses through 5 clear stages:
1. **Received (Received Order)**: Initial booking confirmed.
2. **Baking & Preparing**: Cake is in the oven. Tap *"Send 'Cake is Baking' Update"* to notify the customer on WhatsApp.
3. **Ready for Pickup**: Cake is decorated and ready. Tap *"Send 'Order Ready' Alert"* to notify the customer.
4. **Delivered**: Order handed over and final balance collected.
5. **Cancelled**: Order cancelled.

---

## 5. Menu Catalog & Item Icon Management

Go to the **Menu** tab to view and manage your product catalog:

- **Visual Category Badges & Icons**:
  - 🎂 **Classic Cakes**: Vanilla, Pineapple, Black Forest, Red Velvet, Strawberry, Mango.
  - ✨ **Premium Collection**: Rasmalai, Belgian Chocolate Truffle, Biscoff, Nutella, Tiramisu.
  - 🥮 **Brownies**: Fudgy Brownie, Nutella Swirl, Whole Wheat Walnut.
  - ☕ **Tea Cakes**: Kesar Badam, Honey Oats, Pan Bahar.
  - 🍪 **Cookies**: Oats & Almond, Multigrain, Choco Chip, Karachi Biscuits.
- **Adding Custom Items**: Tap the `+` button to add new items with custom portion names, prices, 100% eggless badge, and seasonal rate options.
- **In-Stock Toggle**: Switch item availability on/off instantly.

---

## 6. Inventory Stock & Low Stock Alerts

Go to the **Stock** tab to track raw materials and packaging:

- **Categories**: Ingredients (Flour, Cocoa, Chocolate), Dairy (Whipping Cream, Butter), Toppings (Biscoff, Nutella), Packaging (½ KG / 1 KG Window Boxes, Gift Boxes).
- **Stock Adjustments**: Tap `+` or `-` to update physical quantities.
- **Threshold Alerts**: Items below `minThreshold` trigger automatic low-stock warning banners on the main Dashboard.

---

## 7. Customer Directory & CRM

Go to the **Clients** tab to manage customer records:

- Search customers instantly by name or phone number.
- View total orders placed and total spent per customer.
- One-tap native phone call dialer and WhatsApp chat launcher.

---

## 8. Shop Profile & Branding Settings

Go to **Settings** (Gear icon on Dashboard) to customize your bakery profile:

- **Shop Details**: Shop / Bakery Name, Tagline, Owner Name, Contact Phone/WhatsApp, Email Address, Full Shop Address, FSSAI License Number, and GSTIN.
- **100% Eggless Badge**: Enable or disable the green eggless indicator across the app top app bar and receipts.
- Tap **"Save Shop Details"** to commit updates.

---

## 9. Database Backup & Restore (SQL / CSV / JSON)

Keep your data safe with multi-format backup tools in **Settings ➔ Database Backup & Restore**:

### Exporting Backups:
1. **Export SQL Script (.sql)**: Generates a complete database dump with `DELETE` and `INSERT INTO` statements. Ideal for database administrators and SQL tools.
2. **Export CSV Spreadsheet (.csv)**: Generates a multi-section CSV file (`SHOP_DETAILS`, `CUSTOMERS`, `MENU_ITEMS`, `INVENTORY`, `ORDERS`, `ORDER_ITEMS`) compatible with Microsoft Excel and Google Sheets.
3. **Export JSON Document (.json)**: Generates a structured JSON document.

### Restoring Backups:
1. Tap **"Select Backup File (.sql / .csv / .json)"**.
2. Select any `.sql`, `.csv`, or `.json` file from your device storage or Google Drive.
3. Confirm the restoration dialog. The app auto-detects the format and restores all database tables and shop settings instantly!

---

## 10. Customizing App Color Themes

In **Settings ➔ App Color Themes**, choose from 5 warm bakery color palettes:

1. 🍫 **Artisan Cocoa** *(Default)*: Warm deep artisan cocoa, caramel, and cream.
2. 🍓 **Berry Velvet**: Rich red velvet burgundy, strawberry pink, and rose.
3. 🍯 **Golden Vanilla**: Deep honey amber, baked golden pastry, and warm cream.
4. 🍵 **Pistachio Matcha**: Fresh green tea forest, pistachio mint, and light cream.
5. 🌙 **Dark Mocha**: Rich espresso dark chocolate mode for low-light environments.

Tapping any theme swatch instantly applies the new color scheme across all screens and status bars!

---

## 11. Security, Data Privacy & Best Practices

- **Zero Cloud Leakage**: All customer addresses, order financial figures, and contact details remain 100% local on your Android device.
- **SQL & CSV Injection Prevention**: All text fields, notes, and messages are automatically sanitized and escaped (`''` for SQL, `""` for CSV) during export/import operations.
- **Safe External Intents**: Phone calls and WhatsApp messages use native Android `ACTION_DIAL` and `ACTION_VIEW` intents with sanitized numerical phone numbers.

---

## 💡 Support & Feedback

Thank you for choosing **Ovenly Bytes**! For custom feature requests, template additions, or queries, visit the settings menu or reach out to your app developer.

*Freshly Baked With Love ❤️ • Ovenly Bytes Version 1.0.0*
