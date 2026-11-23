package cl.duocuc.pagoflex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun ViewModel.ejecutar(onError: (String) -> Unit, accion: suspend () -> Unit): Job =
    viewModelScope.launch {
        try {
            accion()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError(e.message ?: "Ocurrió un error inesperado")
        }
    }
