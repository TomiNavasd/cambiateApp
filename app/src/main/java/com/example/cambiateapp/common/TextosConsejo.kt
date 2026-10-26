package com.example.cambiateapp.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.cambiateapp.R
import com.example.cambiateapp.domain.model.ConsejoClima
import com.example.cambiateapp.domain.model.TipoConsejo

fun ConsejoClima.tituloLocalizado(context: Context): String = when (tipo) {
    is TipoConsejo.Lluvia -> context.getString(R.string.consejo_lluvia_titulo)
    is TipoConsejo.Frio -> context.getString(R.string.consejo_frio_titulo)
    is TipoConsejo.Calor -> context.getString(R.string.consejo_calor_titulo)
    TipoConsejo.Agradable -> context.getString(R.string.consejo_agradable_titulo)
    TipoConsejo.Prueba -> context.getString(R.string.consejo_prueba_titulo)
    null -> titulo
}

fun ConsejoClima.mensajeLocalizado(context: Context): String = when (val t = tipo) {
    is TipoConsejo.Lluvia ->
        if (t.abrigo != null) context.getString(R.string.consejo_lluvia_con_abrigo, t.probLluviaPct, t.abrigo)
        else context.getString(R.string.consejo_lluvia_sin_abrigo, t.probLluviaPct)
    is TipoConsejo.Frio ->
        if (t.abrigo != null) context.getString(R.string.consejo_frio_con_abrigo, t.minC, t.abrigo)
        else context.getString(R.string.consejo_frio_sin_abrigo, t.minC)
    is TipoConsejo.Calor -> context.getString(R.string.consejo_calor_mensaje, t.maxC)
    TipoConsejo.Agradable -> context.getString(R.string.consejo_agradable_mensaje)
    TipoConsejo.Prueba -> context.getString(R.string.consejo_prueba_mensaje)
    null -> mensaje
}

@Composable
fun ConsejoClima.tituloLocalizado(): String = tituloLocalizado(LocalContext.current)

@Composable
fun ConsejoClima.mensajeLocalizado(): String = mensajeLocalizado(LocalContext.current)