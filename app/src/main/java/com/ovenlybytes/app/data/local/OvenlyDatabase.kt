package com.ovenlybytes.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ovenlybytes.app.data.local.dao.CustomerDao
import com.ovenlybytes.app.data.local.dao.InventoryDao
import com.ovenlybytes.app.data.local.dao.MenuItemDao
import com.ovenlybytes.app.data.local.dao.OrderDao
import com.ovenlybytes.app.data.local.entities.*
import com.ovenlybytes.app.model.MenuCategory
import com.ovenlybytes.app.model.OrderSource
import com.ovenlybytes.app.model.OrderStatus
import com.ovenlybytes.app.model.PaymentStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        MenuItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        InventoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OvenlyDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: OvenlyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): OvenlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OvenlyDatabase::class.java,
                    "ovenly_bites_database.db"
                )
                    .addCallback(OvenlyDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class OvenlyDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val menuItemDao = database.menuItemDao()
                    val inventoryDao = database.inventoryDao()
                    if (menuItemDao.getMenuItemCount() == 0 || inventoryDao.getInventoryCount() == 0) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: OvenlyDatabase) {
            val menuItemDao = database.menuItemDao()
            val inventoryDao = database.inventoryDao()
            val customerDao = database.customerDao()
            val orderDao = database.orderDao()

            // 1. POPULATE MENU ITEMS FROM OVENLY BITES MENU
            val menuItems = listOf(
                // Classic Cakes (1/2 KG & 1 KG)
                MenuItemEntity(
                    name = "Vanilla Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Moist & tender vanilla sponge layered with pure vanilla cream. 100% Eggless.",
                    size1Name = "½ KG",
                    price1 = 550.0,
                    size2Name = "1 KG",
                    price2 = 950.0,
                    size3Name = "2 KG",
                    price3 = 1800.0
                ),
                MenuItemEntity(
                    name = "Pineapple Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Classic moist sponge infused with crushed pineapple and fresh whipped cream.",
                    size1Name = "½ KG",
                    price1 = 600.0,
                    size2Name = "1 KG",
                    price2 = 1050.0,
                    size3Name = "2 KG",
                    price3 = 2000.0
                ),
                MenuItemEntity(
                    name = "Black Forest Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Rich chocolate sponge layered with cherries and fresh cream, topped with chocolate curls.",
                    size1Name = "½ KG",
                    price1 = 600.0,
                    size2Name = "1 KG",
                    price2 = 1050.0,
                    size3Name = "2 KG",
                    price3 = 2000.0
                ),
                MenuItemEntity(
                    name = "Butterscotch Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Crunchy caramelized praline nuggets with butterscotch syrup & cream.",
                    size1Name = "½ KG",
                    price1 = 600.0,
                    size2Name = "1 KG",
                    price2 = 1100.0,
                    size3Name = "2 KG",
                    price3 = 2100.0
                ),
                MenuItemEntity(
                    name = "Strawberry Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Delicate strawberry sponge layered with natural strawberry compote & cream.",
                    size1Name = "½ KG",
                    price1 = 600.0,
                    size2Name = "1 KG",
                    price2 = 1050.0,
                    size3Name = "2 KG",
                    price3 = 2000.0
                ),
                MenuItemEntity(
                    name = "Red Velvet Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Signature velvety crimson crumb paired with luxurious cream cheese frosting.",
                    size1Name = "½ KG",
                    price1 = 700.0,
                    size2Name = "1 KG",
                    price2 = 1250.0,
                    size3Name = "2 KG",
                    price3 = 2400.0
                ),
                MenuItemEntity(
                    name = "Blueberry Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Wild blueberry filling blended with creamy vanilla layers.",
                    size1Name = "½ KG",
                    price1 = 650.0,
                    size2Name = "1 KG",
                    price2 = 1200.0,
                    size3Name = "2 KG",
                    price3 = 2300.0
                ),
                MenuItemEntity(
                    name = "Mango Cake (Seasonal)",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Fresh seasonal Alphonso mango chunks and mango mousse. Priced per fruit rate.",
                    size1Name = "½ KG",
                    price1 = 700.0,
                    size2Name = "1 KG",
                    price2 = 1300.0,
                    size3Name = "2 KG",
                    price3 = 2500.0,
                    isSeasonalRate = true
                ),
                MenuItemEntity(
                    name = "Fresh Fruit Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    description = "Loaded with fresh exotic seasonal fruits and light vanilla cream.",
                    size1Name = "½ KG",
                    price1 = 750.0,
                    size2Name = "1 KG",
                    price2 = 1400.0,
                    size3Name = "2 KG",
                    price3 = 2700.0,
                    isSeasonalRate = true
                ),

                // Premium Collection (1/2 KG & 1 KG & 2 KG)
                MenuItemEntity(
                    name = "Rasmalai Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Royal fusion infused with cardamom rabdi, pistachio, saffron, and soft rasmalai chunks.",
                    size1Name = "½ KG",
                    price1 = 800.0,
                    size2Name = "1 KG",
                    price2 = 1500.0,
                    size3Name = "2 KG",
                    price3 = 2900.0
                ),
                MenuItemEntity(
                    name = "Chocolate Truffle Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Intense Belgian dark chocolate ganache layered with moist chocolate sponge.",
                    size1Name = "½ KG",
                    price1 = 750.0,
                    size2Name = "1 KG",
                    price2 = 1400.0,
                    size3Name = "2 KG",
                    price3 = 2700.0
                ),
                MenuItemEntity(
                    name = "Biscoff Chocolate Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Signature speculoos Lotus Biscoff spread paired with rich chocolate ganache.",
                    size1Name = "½ KG",
                    price1 = 800.0,
                    size2Name = "1 KG",
                    price2 = 1500.0
                ),
                MenuItemEntity(
                    name = "Nutella Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Lavish hazelnut Nutella mousse encased in decadent chocolate sponge.",
                    size1Name = "½ KG",
                    price1 = 800.0,
                    size2Name = "1 KG",
                    price2 = 1500.0
                ),
                MenuItemEntity(
                    name = "Chocolate Truffle with Whipped Caramel",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Silky whipped salted caramel ribbons between rich dark chocolate layers.",
                    size1Name = "½ KG",
                    price1 = 750.0,
                    size2Name = "1 KG",
                    price2 = 1400.0
                ),
                MenuItemEntity(
                    name = "Tiramisu Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    description = "Italian espresso-soaked eggless sponge with velvety mascarpone style cream.",
                    size1Name = "½ KG",
                    price1 = 750.0,
                    size2Name = "1 KG",
                    price2 = 1400.0
                ),

                // Brownies (250 g & 500 g)
                MenuItemEntity(
                    name = "Classic Fudgy Brownie",
                    category = MenuCategory.BROWNIES.name,
                    description = "Dense, chewy, crackly top fudge brownies made with pure dark chocolate.",
                    size1Name = "250 g",
                    price1 = 270.0,
                    size2Name = "500 g",
                    price2 = 500.0
                ),
                MenuItemEntity(
                    name = "Nutella Brownie",
                    category = MenuCategory.BROWNIES.name,
                    description = "Fudgy brownie swirled generously with melted Nutella hazelnut cream.",
                    size1Name = "250 g",
                    price1 = 350.0,
                    size2Name = "500 g",
                    price2 = 600.0
                ),
                MenuItemEntity(
                    name = "Whole Wheat Walnut Brownie",
                    category = MenuCategory.BROWNIES.name,
                    description = "Guilt-free whole wheat brownie loaded with crunchy toasted Californian walnuts.",
                    size1Name = "250 g",
                    price1 = 350.0,
                    size2Name = "500 g",
                    price2 = 600.0
                ),
                MenuItemEntity(
                    name = "Mocha Brownie",
                    category = MenuCategory.BROWNIES.name,
                    description = "Infused with rich brewed espresso coffee and dark chocolate chips.",
                    size1Name = "250 g",
                    price1 = 280.0,
                    size2Name = "500 g",
                    price2 = 500.0
                ),

                // Tea Cakes (250 g & 500 g)
                MenuItemEntity(
                    name = "Kesar Badam Tea Cake",
                    category = MenuCategory.TEA_CAKES.name,
                    description = "Aromatic saffron strands with slivered roasted almonds. Perfect for high tea.",
                    size1Name = "250 g",
                    price1 = 300.0,
                    size2Name = "500 g",
                    price2 = 550.0
                ),
                MenuItemEntity(
                    name = "Honey Oats Tea Cake",
                    category = MenuCategory.TEA_CAKES.name,
                    description = "Wholesome rolled oats sweetened with pure organic honey and mild spices.",
                    size1Name = "250 g",
                    price1 = 300.0,
                    size2Name = "500 g",
                    price2 = 550.0
                ),
                MenuItemEntity(
                    name = "Pan Bahar Tea Cake",
                    category = MenuCategory.TEA_CAKES.name,
                    description = "Exotic Indian betel leaf aroma with gulkand and fennel notes.",
                    size1Name = "250 g",
                    price1 = 250.0,
                    size2Name = "500 g",
                    price2 = 450.0
                ),
                MenuItemEntity(
                    name = "Marble Tea Cake",
                    category = MenuCategory.TEA_CAKES.name,
                    description = "Classic swirl of rich vanilla bean and dark chocolate batter.",
                    size1Name = "250 g",
                    price1 = 250.0,
                    size2Name = "500 g",
                    price2 = 400.0
                ),
                MenuItemEntity(
                    name = "Whole Wheat Chocolate Walnut Tea Cake",
                    category = MenuCategory.TEA_CAKES.name,
                    description = "Hearty 100% whole wheat loaf with chocolate chunks and walnuts.",
                    size1Name = "250 g",
                    price1 = 250.0,
                    size2Name = "500 g",
                    price2 = 450.0
                ),

                // Cookies (250 g & 500 g)
                MenuItemEntity(
                    name = "Oats & Almond Cookies",
                    category = MenuCategory.COOKIES.name,
                    description = "Crispy buttery cookies with roasted almond flakes and golden oats.",
                    size1Name = "250 g",
                    price1 = 350.0,
                    size2Name = "500 g",
                    price2 = 650.0
                ),
                MenuItemEntity(
                    name = "Multigrain Cookies",
                    category = MenuCategory.COOKIES.name,
                    description = "Healthy crunchy cookies baked with wholesome multigrain flour blend.",
                    size1Name = "250 g",
                    price1 = 300.0,
                    size2Name = "500 g",
                    price2 = 550.0
                ),
                MenuItemEntity(
                    name = "Choco Chip Cookies",
                    category = MenuCategory.COOKIES.name,
                    description = "Crowd favorite crispy-edge cookies packed with gooey chocolate chips.",
                    size1Name = "250 g",
                    price1 = 300.0,
                    size2Name = "500 g",
                    price2 = 550.0
                ),
                MenuItemEntity(
                    name = "Karachi Biscuits",
                    category = MenuCategory.COOKIES.name,
                    description = "Traditional aromatic bakery biscuits with tutti frutti and cashews.",
                    size1Name = "250 g",
                    price1 = 200.0,
                    size2Name = "500 g",
                    price2 = 350.0
                )
            )
            menuItemDao.insertMenuItems(menuItems)

            // 2. POPULATE INITIAL INVENTORY
            val inventoryItems = listOf(
                InventoryEntity(
                    itemName = "Belgian Dark Chocolate (54%)",
                    category = "INGREDIENTS",
                    currentQuantity = 4.5,
                    unit = "kg",
                    minThreshold = 3.0,
                    costPerUnit = 850.0
                ),
                InventoryEntity(
                    itemName = "Lotus Biscoff Spread",
                    category = "TOPPINGS",
                    currentQuantity = 2.0,
                    unit = "kg",
                    minThreshold = 2.5, // Low stock demo!
                    costPerUnit = 920.0
                ),
                InventoryEntity(
                    itemName = "Nutella Hazelnut Spread",
                    category = "TOPPINGS",
                    currentQuantity = 5.0,
                    unit = "kg",
                    minThreshold = 2.0,
                    costPerUnit = 780.0
                ),
                InventoryEntity(
                    itemName = "Fine Bakery Flour (Maida)",
                    category = "INGREDIENTS",
                    currentQuantity = 25.0,
                    unit = "kg",
                    minThreshold = 10.0,
                    costPerUnit = 48.0
                ),
                InventoryEntity(
                    itemName = "Cocoa Powder (Dutch Processed)",
                    category = "INGREDIENTS",
                    currentQuantity = 3.0,
                    unit = "kg",
                    minThreshold = 2.0,
                    costPerUnit = 600.0
                ),
                InventoryEntity(
                    itemName = "Heavy Whipping Cream",
                    category = "DAIRY",
                    currentQuantity = 8.0,
                    unit = "liters",
                    minThreshold = 5.0,
                    costPerUnit = 220.0
                ),
                InventoryEntity(
                    itemName = "Unsalted Butter",
                    category = "DAIRY",
                    currentQuantity = 6.0,
                    unit = "kg",
                    minThreshold = 4.0,
                    costPerUnit = 520.0
                ),
                InventoryEntity(
                    itemName = "½ KG Cake Boxes with Window",
                    category = "PACKAGING",
                    currentQuantity = 15.0,
                    unit = "pcs",
                    minThreshold = 25.0, // Low stock demo!
                    costPerUnit = 18.0
                ),
                InventoryEntity(
                    itemName = "1 KG Cake Boxes with Window",
                    category = "PACKAGING",
                    currentQuantity = 35.0,
                    unit = "pcs",
                    minThreshold = 20.0,
                    costPerUnit = 24.0
                ),
                InventoryEntity(
                    itemName = "Brownie Gift Boxes (4-cavity)",
                    category = "PACKAGING",
                    currentQuantity = 40.0,
                    unit = "pcs",
                    minThreshold = 20.0,
                    costPerUnit = 22.0
                )
            )
            inventoryDao.insertItems(inventoryItems)

            // 3. POPULATE INITIAL SAMPLE CUSTOMERS & ORDERS (For immediate preview)
            val custId1 = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Priya Sharma",
                    phone = "9876543210",
                    email = "priya.sharma@example.com",
                    address = "Flat 402, Sunshine Heights, MG Road",
                    notes = "Prefers less sweet cakes, very punctual"
                )
            )

            val custId2 = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Rohit Verma",
                    phone = "9811223344",
                    email = "rohit.verma@example.com",
                    address = "Villa 12, Green Meadows, Sector 15",
                    notes = "Regular customer, orders for office celebrations"
                )
            )

            // Seed sample orders (one WhatsApp, one Phone)
            val todayMs = System.currentTimeMillis()
            val tomorrowMs = todayMs + (24 * 60 * 60 * 1000L)
            val twoDaysMs = todayMs + (48 * 60 * 60 * 1000L)

            // Order 1: Received via WhatsApp
            val order1 = OrderEntity(
                customerId = custId1,
                orderSource = OrderSource.WHATSAPP.name,
                orderStatus = OrderStatus.BAKING.name,
                orderDate = todayMs - (12 * 60 * 60 * 1000L),
                deliveryDate = tomorrowMs,
                deliveryTimeSlot = "5:00 PM - 6:00 PM",
                cakeMessage = "Happy 30th Birthday Rahul ❤️",
                customizationNotes = "Gold foil touches on border, 100% Eggless",
                customizationCharge = 150.0,
                subtotal = 1500.0,
                totalAmount = 1650.0,
                advancePaid = 1000.0,
                balanceDue = 650.0,
                paymentMode = "UPI",
                paymentStatus = PaymentStatus.PARTIAL.name,
                notes = "Customer confirmed order over WhatsApp with reference photo"
            )
            val items1 = listOf(
                OrderItemEntity(
                    orderId = 0,
                    menuItemId = 12L,
                    itemName = "Biscoff Chocolate Cake",
                    category = MenuCategory.PREMIUM_COLLECTION.name,
                    variantName = "1 KG",
                    unitPrice = 1500.0,
                    quantity = 1,
                    itemTotal = 1500.0
                )
            )
            orderDao.createOrderWithItems(order1, items1)

            // Order 2: Received via Phone Call
            val order2 = OrderEntity(
                customerId = custId2,
                orderSource = OrderSource.PHONE.name,
                orderStatus = OrderStatus.RECEIVED.name,
                orderDate = todayMs - (2 * 60 * 60 * 1000L),
                deliveryDate = twoDaysMs,
                deliveryTimeSlot = "11:00 AM - 12:00 PM",
                cakeMessage = "Welcome Home Mom!",
                customizationNotes = "Extra fresh cherries on top",
                customizationCharge = 50.0,
                subtotal = 1550.0,
                totalAmount = 1600.0,
                advancePaid = 500.0,
                balanceDue = 1100.0,
                paymentMode = "UPI",
                paymentStatus = PaymentStatus.PARTIAL.name,
                notes = "Called at 11am, confirmed eggless requirement"
            )
            val items2 = listOf(
                OrderItemEntity(
                    orderId = 0,
                    menuItemId = 3L,
                    itemName = "Black Forest Cake",
                    category = MenuCategory.CLASSIC_CAKES.name,
                    variantName = "1 KG",
                    unitPrice = 1050.0,
                    quantity = 1,
                    itemTotal = 1050.0
                ),
                OrderItemEntity(
                    orderId = 0,
                    menuItemId = 16L,
                    itemName = "Classic Fudgy Brownie",
                    category = MenuCategory.BROWNIES.name,
                    variantName = "500 g",
                    unitPrice = 500.0,
                    quantity = 1,
                    itemTotal = 500.0
                )
            )
            orderDao.createOrderWithItems(order2, items2)
        }
    }
}

