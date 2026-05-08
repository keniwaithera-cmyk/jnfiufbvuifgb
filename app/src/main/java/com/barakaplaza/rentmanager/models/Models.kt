package com.barakaplaza.rentmanager.models

data class BuildingModel(
    val id: Int = 0,
    val name: String = "",
    val location: String = "",
    val totalHouses: Int = 0
) {
    companion object {
        val BUILDINGS = listOf(
            BuildingModel(1, "Baraka Plaza",    "Main Street",   10),
            BuildingModel(2, "Emanuel Building","Church Road",    8),
            BuildingModel(3, "Shalom Building", "Peace Avenue",   6),
            BuildingModel(4, "Adonai Building", "Grace Lane",     8)
        )
    }
}

data class TenantModel(
    val id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val idNumber: String = "",
    val houseNumber: String = "",
    val buildingName: String = "",
    val moveInDate: String = "",
    val emergencyContact: String = "",
    val isActive: Boolean = true,
    val balance: Double = 0.0,
    val selfRegistered: Boolean = false
)

data class HouseModel(
    val id: Int = 0,
    val houseNumber: String = "",
    val buildingName: String = "",
    val floor: String = "",
    val type: String = "",
    val monthlyRent: Double = 0.0,
    val isOccupied: Boolean = false,
    val description: String = "",
    val imageUrl: String = ""
) {
    fun getDisplayName() = "House $houseNumber - $type ($floor)"
    fun getRentDisplay() = "KSh ${"%,.0f".format(monthlyRent)}/month"
}

data class PaymentModel(
    val id: Int = 0,
    val tenantId: Int = 0,
    val tenantName: String = "",
    val houseNumber: String = "",
    val buildingName: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = METHOD_MPESA,
    val paymentDate: String = "",
    val paymentMonth: String = "",
    val paymentYear: String = "",
    val mpesaCode: String = "",
    val status: String = STATUS_CONFIRMED,
    val notes: String = ""
) {
    fun getAmountDisplay() = "KSh ${"%,.0f".format(amount)}"
    companion object {
        const val METHOD_MPESA = "M-PESA"
        const val METHOD_CASH  = "CASH"
        const val STATUS_CONFIRMED = "CONFIRMED"
        const val STATUS_PENDING   = "PENDING"
    }
}

data class SuggestionModel(
    val id: Int = 0,
    val tenantId: Int = 0,
    val tenantName: String = "",
    val houseNumber: String = "",
    val buildingName: String = "",
    val suggestion: String = "",
    val dateSubmitted: String = "",
    val isRead: Boolean = false
)

data class PaymentPromiseModel(
    val id: Int = 0,
    val tenantId: Int = 0,
    val tenantName: String = "",
    val houseNumber: String = "",
    val buildingName: String = "",
    val promisedAmount: Double = 0.0,
    val promisedDate: String = "",
    val message: String = "",
    val dateSubmitted: String = "",
    val isRead: Boolean = false
)
