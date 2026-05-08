package com.barakaplaza.rentmanager.util

object AppConstants {
    // M-Pesa — paybill & account from Ken
    const val MPESA_PAYBILL         = "247247"
    const val MPESA_ACCOUNT         = "0726352340"
    const val ADMIN_PHONE           = "0726352338"

    // Cloudinary — Ken's cloud name
    const val CLOUDINARY_CLOUD_NAME    = "dxbm4rqv1"
    const val CLOUDINARY_UPLOAD_PRESET = "baraka_houses"

    const val DB_NAME    = "BarakaPlaza.db"
    // FIXED: Bumped to 4 (above the version 3 already on the device).
    // Using version 1 while the phone had version 3 caused a fatal
    // "Can't downgrade database" crash before any login code could run.
    const val DB_VERSION = 4
}
