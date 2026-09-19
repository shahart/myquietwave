package com.shahartal.myquietchannel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import com.shahartal.myquietchannel.parasha.HebCalZmanimModel

internal data class DailyLearningUiState(
    val summary: DailyLearningSummary? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

internal data class ShabbatUiState(
    val summary: ShabbatSummary? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

internal data class ZmanimUiState(
    val model: HebCalZmanimModel? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

internal data class ParashaUiState(
    val hebcal: com.shahartal.myquietchannel.parasha.HebCal? = null,
    val error: Throwable? = null,
    val isLoading: Boolean = false,
)

internal class MainViewModel(
    private val hebcalRepository: HebcalRepository,
) : ViewModel() {
    private val _dailyLearning = MutableStateFlow(DailyLearningUiState())
    val dailyLearning: StateFlow<DailyLearningUiState> = _dailyLearning.asStateFlow()
    private val _shabbat = MutableStateFlow(ShabbatUiState())
    val shabbat: StateFlow<ShabbatUiState> = _shabbat.asStateFlow()
    private val _zmanim = MutableStateFlow(ZmanimUiState())
    val zmanim: StateFlow<ZmanimUiState> = _zmanim.asStateFlow()
    private val _parasha = MutableStateFlow(ParashaUiState())
    val parasha: StateFlow<ParashaUiState> = _parasha.asStateFlow()

    fun fetchDailyLearning(date: LocalDate = LocalDate.now()) {
        val isoDate = date.toString()
        _dailyLearning.value = _dailyLearning.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    HebcalPresentation.dailyLearning(
                        hebcalRepository.dailyLearning(isoDate).items,
                        isoDate,
                    )
                }
            }.onSuccess { summary ->
                _dailyLearning.value = DailyLearningUiState(summary = summary)
            }.onFailure { error ->
                _dailyLearning.value = DailyLearningUiState(error = error)
            }
        }
    }

    fun fetchShabbat(query: LocationQuery) {
        _shabbat.value = _shabbat.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    HebcalPresentation.shabbat(hebcalRepository.shabbat(query).items)
                }
            }.onSuccess { summary ->
                _shabbat.value = ShabbatUiState(summary = summary)
            }.onFailure { error ->
                _shabbat.value = ShabbatUiState(error = error)
            }
        }
    }

    fun fetchZmanim(query: LocationQuery) {
        _zmanim.value = _zmanim.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { hebcalRepository.zmanim(query) }
            }.onSuccess { model ->
                _zmanim.value = ZmanimUiState(model = model)
            }.onFailure { error ->
                _zmanim.value = ZmanimUiState(error = error)
            }
        }
    }

    fun fetchParasha() {
        _parasha.value = _parasha.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { hebcalRepository.parasha() }
            }.onSuccess { hebcal ->
                _parasha.value = ParashaUiState(hebcal = hebcal)
            }.onFailure { error ->
                _parasha.value = ParashaUiState(error = error)
            }
        }
    }
}

internal class MainViewModelFactory(
    private val hebcalRepository: HebcalRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            "Unsupported ViewModel: ${modelClass.name}"
        }
        return MainViewModel(hebcalRepository) as T
    }
}
