package com.example.cambiateapp.presentation.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList

enum class IdiomaApp(val codigo: String) {
    AUTOMATICO(""),
    ESPANOL("es"),
    INGLES("en"),
    PORTUGUES("pt")
}

object GestorIdioma {

    val disponible: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun actual(context: Context): IdiomaApp {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return IdiomaApp.AUTOMATICO
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) return IdiomaApp.AUTOMATICO
        val idioma = locales[0]?.language
        return IdiomaApp.entries.firstOrNull { it.codigo == idioma } ?: IdiomaApp.AUTOMATICO
    }

    fun aplicar(context: Context, idioma: IdiomaApp) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (idioma == IdiomaApp.AUTOMATICO) LocaleList.getEmptyLocaleList()
            else LocaleList.forLanguageTags(idioma.codigo)
    }
}