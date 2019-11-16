package ru.raid.smartdiary.net

import com.google.gson.annotations.SerializedName

class AddUserResponse(
        @SerializedName("u_id") val uid: Int
)