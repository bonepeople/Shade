package com.bonepeople.android.shade.util

import android.util.Base64
import com.bonepeople.android.shade.Lighting
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESUtil {
    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(Lighting.appInformation.secret.toByteArray(), "AES")
        val iv = IvParameterSpec(Lighting.appInformation.salt.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val encrypted = cipher.doFinal(data.toByteArray())

        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(data: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(Lighting.appInformation.secret.toByteArray(), "AES")
        val iv = IvParameterSpec(Lighting.appInformation.salt.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val decrypted = cipher.doFinal(Base64.decode(data, Base64.DEFAULT))

        return String(decrypted)
    }
}