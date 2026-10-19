package com.example.cambiateapp.domain.usecase

import com.example.cambiateapp.domain.model.PreferenciasRecordatorio
import com.example.cambiateapp.domain.notification.AgendadorRecordatorio
import com.example.cambiateapp.domain.repository.RecordatorioRepository
import javax.inject.Inject

class ConfigurarRecordatorioUseCase @Inject constructor(
    private val repositorio: RecordatorioRepository,
    private val agendador: AgendadorRecordatorio,
) {
    suspend operator fun invoke(preferencias: PreferenciasRecordatorio) {
        require(preferencias.hora in 0..23 && preferencias.minuto in 0..59) { "Hora inválida" }
        repositorio.guardar(preferencias)
        if (preferencias.activo) {
            agendador.programar(preferencias.hora, preferencias.minuto)
        } else {
            agendador.cancelar()
        }
    }
}