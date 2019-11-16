package com.qatasoft.videocall.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val sendingTime: String) {
    constructor() : this("", "", "", "", "")
}

class LoginInfo(val email: String, val password: String) {
    constructor() : this("", "")
}

data class Token(
        val token: String
)

//Bu classın nesnesinin diğer activitylere gönderilebilmesi için Parcelable olması gerekiyor. Bunu da kalıtım ve de eklentiyle yapıyoruz.Eklenti gradle app kısmında androidExtensions tagıyla yazılan fonksiyondur
@Parcelize
class User(val profileImageUrl: String, val uid: String, val username: String, val token: String, val about: String, val mobile: String, val email: String, var isFollowed: Boolean) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", false)
}

@Parcelize
class GeneralInfo(val img_general_url: String, val title: String, val text: String) : Parcelable {
    constructor() : this("", "", "")
}