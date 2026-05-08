package com.barakaplaza.rentmanager.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.barakaplaza.rentmanager.models.*
import com.barakaplaza.rentmanager.util.AppConstants

class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, AppConstants.DB_NAME, null, AppConstants.DB_VERSION) {

    companion object {
        @Volatile private var instance: DatabaseHelper? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: DatabaseHelper(context).also { instance = it }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE tenants(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT, phone TEXT, email TEXT, id_number TEXT,
            house_number TEXT, building_name TEXT, move_in_date TEXT,
            emergency_contact TEXT, is_active INTEGER DEFAULT 1,
            balance REAL DEFAULT 0, self_registered INTEGER DEFAULT 0)""")

        db.execSQL("""CREATE TABLE houses(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            house_number TEXT, building_name TEXT, floor TEXT,
            type TEXT, monthly_rent REAL, is_occupied INTEGER DEFAULT 0,
            description TEXT, image_url TEXT DEFAULT '')""")

        db.execSQL("""CREATE TABLE payments(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tenant_id INTEGER, tenant_name TEXT, house_number TEXT,
            building_name TEXT, amount REAL, payment_method TEXT,
            payment_date TEXT, payment_month TEXT, payment_year TEXT,
            mpesa_code TEXT, status TEXT DEFAULT 'CONFIRMED', notes TEXT)""")

        db.execSQL("""CREATE TABLE landlord(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT, phone TEXT, email TEXT, password TEXT,
            paybill TEXT, account TEXT, mpesa_phone TEXT)""")

        db.execSQL("""CREATE TABLE suggestions(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tenant_id INTEGER, tenant_name TEXT, house_number TEXT,
            building_name TEXT, suggestion TEXT, date_submitted TEXT,
            is_read INTEGER DEFAULT 0)""")

        db.execSQL("""CREATE TABLE promises(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            tenant_id INTEGER, tenant_name TEXT, house_number TEXT,
            building_name TEXT, promised_amount REAL, promised_date TEXT,
            message TEXT, date_submitted TEXT, is_read INTEGER DEFAULT 0)""")

        seedLandlord(db)
        seedHouses(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {
        listOf("tenants","houses","payments","landlord","suggestions","promises")
            .forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    // FIXED: onDowngrade was missing. Without it, SQLiteOpenHelper throws a fatal
    // "Can't downgrade database from version X to Y" exception before the app even
    // reaches the login logic. This override drops and recreates all tables so the
    // app recovers cleanly regardless of which direction the version jumps.
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun seedLandlord(db: SQLiteDatabase) {
        db.insert("landlord", null, ContentValues().apply {
            put("name", "Baraka Plaza Manager")
            put("phone", AppConstants.ADMIN_PHONE)
            put("email", "manager@barakaplaza.co.ke")
            put("password", "admin1234")
            put("paybill", AppConstants.MPESA_PAYBILL)
            put("account", AppConstants.MPESA_ACCOUNT)
            put("mpesa_phone", AppConstants.ADMIN_PHONE)
        })
    }

    private fun seedHouses(db: SQLiteDatabase) {
        val buildings = mapOf(
            "Baraka Plaza"    to listOf(arrayOf("A1","G","Bedsitter","5000"), arrayOf("A2","G","Bedsitter","5000"), arrayOf("A3","1st","1 Bedroom","8000"), arrayOf("B1","1st","1 Bedroom","8000"), arrayOf("B2","2nd","2 Bedroom","12000")),
            "Emanuel Building" to listOf(arrayOf("E1","G","Bedsitter","4500"), arrayOf("E2","G","1 Bedroom","7500"), arrayOf("E3","1st","1 Bedroom","7500"), arrayOf("E4","1st","2 Bedroom","11000")),
            "Shalom Building" to listOf(arrayOf("S1","G","Bedsitter","4000"), arrayOf("S2","G","1 Bedroom","7000"), arrayOf("S3","1st","2 Bedroom","10000")),
            "Adonai Building" to listOf(arrayOf("AD1","G","Bedsitter","5000"), arrayOf("AD2","G","1 Bedroom","8000"), arrayOf("AD3","1st","2 Bedroom","13000"), arrayOf("AD4","2nd","3 Bedroom","18000"))
        )
        buildings.forEach { (building, houses) ->
            houses.forEach { h ->
                db.insert("houses", null, ContentValues().apply {
                    put("house_number", h[0]); put("building_name", building)
                    put("floor", h[1]); put("type", h[2])
                    put("monthly_rent", h[3].toDouble()); put("is_occupied", 0)
                    put("image_url", "")
                })
            }
        }
    }

    // ══ Landlord ══
    fun validateLogin(phone: String, password: String): Boolean {
        val c = readableDatabase.rawQuery("SELECT id FROM landlord WHERE phone=? AND password=?", arrayOf(phone, password))
        return c.use { it.count > 0 }
    }
    fun getLandlordName(): String {
        val c = readableDatabase.rawQuery("SELECT name FROM landlord", null)
        return c.use { if (it.moveToFirst()) it.getString(0) else "Manager" }
    }
    fun getLandlordPhone(): String {
        val c = readableDatabase.rawQuery("SELECT mpesa_phone FROM landlord", null)
        return c.use { if (it.moveToFirst()) it.getString(0) else AppConstants.ADMIN_PHONE }
    }

    // ══ Stats ══
    fun countActiveTenants(building: String = ""): Int {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT COUNT(*) FROM tenants WHERE is_active=1", null)
        else
            readableDatabase.rawQuery("SELECT COUNT(*) FROM tenants WHERE is_active=1 AND building_name=?", arrayOf(building))
        return c.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }
    fun countVacantHouses(building: String = ""): Int {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT COUNT(*) FROM houses WHERE is_occupied=0", null)
        else
            readableDatabase.rawQuery("SELECT COUNT(*) FROM houses WHERE is_occupied=0 AND building_name=?", arrayOf(building))
        return c.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }
    fun totalCollectedThisMonth(month: String, year: String, building: String = ""): Double {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT SUM(amount) FROM payments WHERE payment_month=? AND payment_year=? AND status='CONFIRMED'", arrayOf(month, year))
        else
            readableDatabase.rawQuery("SELECT SUM(amount) FROM payments WHERE payment_month=? AND payment_year=? AND status='CONFIRMED' AND building_name=?", arrayOf(month, year, building))
        return c.use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }
    }
    fun countUnreadSuggestions(): Int {
        val c = readableDatabase.rawQuery("SELECT COUNT(*) FROM suggestions WHERE is_read=0", null)
        return c.use { if (it.moveToFirst()) it.getInt(0) else 0 }
    }

    // ══ Tenants ══
    fun addTenant(t: TenantModel): Long {
        val id = writableDatabase.insert("tenants", null, ContentValues().apply {
            put("name", t.name); put("phone", t.phone); put("email", t.email)
            put("id_number", t.idNumber); put("house_number", t.houseNumber)
            put("building_name", t.buildingName); put("move_in_date", t.moveInDate)
            put("emergency_contact", t.emergencyContact)
            put("is_active", 1); put("balance", 0.0)
            put("self_registered", if (t.selfRegistered) 1 else 0)
        })
        if (id > 0) writableDatabase.execSQL(
            "UPDATE houses SET is_occupied=1 WHERE house_number=? AND building_name=?",
            arrayOf(t.houseNumber, t.buildingName))
        return id
    }
    fun getAllActiveTenants(building: String = ""): List<TenantModel> {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT * FROM tenants WHERE is_active=1 ORDER BY name", null)
        else
            readableDatabase.rawQuery("SELECT * FROM tenants WHERE is_active=1 AND building_name=? ORDER BY name", arrayOf(building))
        return c.use { cur -> buildList { while (cur.moveToNext()) add(cursorToTenant(cur)) } }
    }
    fun getTenantById(id: Int): TenantModel? {
        val c = readableDatabase.rawQuery("SELECT * FROM tenants WHERE id=?", arrayOf(id.toString()))
        return c.use { if (it.moveToFirst()) cursorToTenant(it) else null }
    }
    fun getTenantByHouseAndBuilding(house: String, building: String): TenantModel? {
        val c = readableDatabase.rawQuery("SELECT * FROM tenants WHERE house_number=? AND building_name=? AND is_active=1", arrayOf(house, building))
        return c.use { if (it.moveToFirst()) cursorToTenant(it) else null }
    }
    fun updateTenant(t: TenantModel): Boolean {
        return writableDatabase.update("tenants", ContentValues().apply {
            put("name", t.name); put("phone", t.phone); put("email", t.email)
            put("id_number", t.idNumber); put("emergency_contact", t.emergencyContact)
        }, "id=?", arrayOf(t.id.toString())) > 0
    }
    fun deleteTenant(id: Int, house: String, building: String): Boolean {
        val rows = writableDatabase.delete("tenants", "id=?", arrayOf(id.toString()))
        if (rows > 0) writableDatabase.execSQL("UPDATE houses SET is_occupied=0 WHERE house_number=? AND building_name=?", arrayOf(house, building))
        return rows > 0
    }
    fun deactivateTenant(id: Int, house: String, building: String): Boolean {
        val rows = writableDatabase.update("tenants", ContentValues().apply { put("is_active", 0) }, "id=?", arrayOf(id.toString()))
        if (rows > 0) writableDatabase.execSQL("UPDATE houses SET is_occupied=0 WHERE house_number=? AND building_name=?", arrayOf(house, building))
        return rows > 0
    }
    private fun cursorToTenant(c: android.database.Cursor) = TenantModel(
        id = c.getInt(c.getColumnIndexOrThrow("id")),
        name = c.getString(c.getColumnIndexOrThrow("name")) ?: "",
        phone = c.getString(c.getColumnIndexOrThrow("phone")) ?: "",
        email = c.getString(c.getColumnIndexOrThrow("email")) ?: "",
        idNumber = c.getString(c.getColumnIndexOrThrow("id_number")) ?: "",
        houseNumber = c.getString(c.getColumnIndexOrThrow("house_number")) ?: "",
        buildingName = c.getString(c.getColumnIndexOrThrow("building_name")) ?: "",
        moveInDate = c.getString(c.getColumnIndexOrThrow("move_in_date")) ?: "",
        emergencyContact = c.getString(c.getColumnIndexOrThrow("emergency_contact")) ?: "",
        isActive = c.getInt(c.getColumnIndexOrThrow("is_active")) == 1,
        balance = c.getDouble(c.getColumnIndexOrThrow("balance")),
        selfRegistered = c.getInt(c.getColumnIndexOrThrow("self_registered")) == 1
    )

    // ══ Houses ══
    fun getHouses(building: String = ""): List<HouseModel> {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT * FROM houses ORDER BY building_name, house_number", null)
        else
            readableDatabase.rawQuery("SELECT * FROM houses WHERE building_name=? ORDER BY house_number", arrayOf(building))
        return c.use { cur -> buildList { while (cur.moveToNext()) add(cursorToHouse(cur)) } }
    }
    fun getVacantHouses(building: String): List<HouseModel> {
        val c = readableDatabase.rawQuery("SELECT * FROM houses WHERE building_name=? AND is_occupied=0 ORDER BY house_number", arrayOf(building))
        return c.use { cur -> buildList { while (cur.moveToNext()) add(cursorToHouse(cur)) } }
    }
    fun addHouse(h: HouseModel): Long = writableDatabase.insert("houses", null, ContentValues().apply {
        put("house_number", h.houseNumber); put("building_name", h.buildingName)
        put("floor", h.floor); put("type", h.type); put("monthly_rent", h.monthlyRent)
        put("is_occupied", 0); put("description", h.description); put("image_url", h.imageUrl)
    })
    fun updateHouseImage(house: String, building: String, url: String) =
        writableDatabase.execSQL("UPDATE houses SET image_url=? WHERE house_number=? AND building_name=?", arrayOf(url, house, building))
    private fun cursorToHouse(c: android.database.Cursor) = HouseModel(
        id = c.getInt(c.getColumnIndexOrThrow("id")),
        houseNumber = c.getString(c.getColumnIndexOrThrow("house_number")) ?: "",
        buildingName = c.getString(c.getColumnIndexOrThrow("building_name")) ?: "",
        floor = c.getString(c.getColumnIndexOrThrow("floor")) ?: "",
        type = c.getString(c.getColumnIndexOrThrow("type")) ?: "",
        monthlyRent = c.getDouble(c.getColumnIndexOrThrow("monthly_rent")),
        isOccupied = c.getInt(c.getColumnIndexOrThrow("is_occupied")) == 1,
        description = c.getString(c.getColumnIndexOrThrow("description")) ?: "",
        imageUrl = try { c.getString(c.getColumnIndexOrThrow("image_url")) ?: "" } catch (_: Exception) { "" }
    )

    // ══ Payments ══
    fun addPayment(p: PaymentModel): Long = writableDatabase.insert("payments", null, ContentValues().apply {
        put("tenant_id", p.tenantId); put("tenant_name", p.tenantName)
        put("house_number", p.houseNumber); put("building_name", p.buildingName)
        put("amount", p.amount); put("payment_method", p.paymentMethod)
        put("payment_date", p.paymentDate); put("payment_month", p.paymentMonth)
        put("payment_year", p.paymentYear); put("mpesa_code", p.mpesaCode)
        put("status", p.status); put("notes", p.notes)
    })
    fun getAllPayments(building: String = ""): List<PaymentModel> {
        val c = if (building.isBlank())
            readableDatabase.rawQuery("SELECT * FROM payments ORDER BY payment_date DESC", null)
        else
            readableDatabase.rawQuery("SELECT * FROM payments WHERE building_name=? ORDER BY payment_date DESC", arrayOf(building))
        return c.use { cur -> buildList { while (cur.moveToNext()) add(cursorToPayment(cur)) } }
    }
    fun getPaymentsByTenant(tenantId: Int): List<PaymentModel> {
        val c = readableDatabase.rawQuery("SELECT * FROM payments WHERE tenant_id=? ORDER BY payment_date DESC", arrayOf(tenantId.toString()))
        return c.use { cur -> buildList { while (cur.moveToNext()) add(cursorToPayment(cur)) } }
    }
    private fun cursorToPayment(c: android.database.Cursor) = PaymentModel(
        id = c.getInt(c.getColumnIndexOrThrow("id")),
        tenantId = c.getInt(c.getColumnIndexOrThrow("tenant_id")),
        tenantName = c.getString(c.getColumnIndexOrThrow("tenant_name")) ?: "",
        houseNumber = c.getString(c.getColumnIndexOrThrow("house_number")) ?: "",
        buildingName = c.getString(c.getColumnIndexOrThrow("building_name")) ?: "",
        amount = c.getDouble(c.getColumnIndexOrThrow("amount")),
        paymentMethod = c.getString(c.getColumnIndexOrThrow("payment_method")) ?: "",
        paymentDate = c.getString(c.getColumnIndexOrThrow("payment_date")) ?: "",
        paymentMonth = c.getString(c.getColumnIndexOrThrow("payment_month")) ?: "",
        paymentYear = c.getString(c.getColumnIndexOrThrow("payment_year")) ?: "",
        mpesaCode = c.getString(c.getColumnIndexOrThrow("mpesa_code")) ?: "",
        status = c.getString(c.getColumnIndexOrThrow("status")) ?: "",
        notes = c.getString(c.getColumnIndexOrThrow("notes")) ?: ""
    )

    // ══ Suggestions ══
    fun addSuggestion(s: SuggestionModel): Long = writableDatabase.insert("suggestions", null, ContentValues().apply {
        put("tenant_id", s.tenantId); put("tenant_name", s.tenantName)
        put("house_number", s.houseNumber); put("building_name", s.buildingName)
        put("suggestion", s.suggestion); put("date_submitted", s.dateSubmitted); put("is_read", 0)
    })
    fun getAllSuggestions(): List<SuggestionModel> {
        val c = readableDatabase.rawQuery("SELECT * FROM suggestions ORDER BY date_submitted DESC", null)
        return c.use { cur -> buildList { while (cur.moveToNext()) add(SuggestionModel(
            id = cur.getInt(cur.getColumnIndexOrThrow("id")),
            tenantId = cur.getInt(cur.getColumnIndexOrThrow("tenant_id")),
            tenantName = cur.getString(cur.getColumnIndexOrThrow("tenant_name")) ?: "",
            houseNumber = cur.getString(cur.getColumnIndexOrThrow("house_number")) ?: "",
            buildingName = cur.getString(cur.getColumnIndexOrThrow("building_name")) ?: "",
            suggestion = cur.getString(cur.getColumnIndexOrThrow("suggestion")) ?: "",
            dateSubmitted = cur.getString(cur.getColumnIndexOrThrow("date_submitted")) ?: "",
            isRead = cur.getInt(cur.getColumnIndexOrThrow("is_read")) == 1
        )) } }
    }
    fun markSuggestionRead(id: Int) = writableDatabase.update("suggestions",
        ContentValues().apply { put("is_read", 1) }, "id=?", arrayOf(id.toString()))

    // ══ Promises ══
    fun addPromise(p: PaymentPromiseModel): Long = writableDatabase.insert("promises", null, ContentValues().apply {
        put("tenant_id", p.tenantId); put("tenant_name", p.tenantName)
        put("house_number", p.houseNumber); put("building_name", p.buildingName)
        put("promised_amount", p.promisedAmount); put("promised_date", p.promisedDate)
        put("message", p.message); put("date_submitted", p.dateSubmitted); put("is_read", 0)
    })
}
