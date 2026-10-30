package com.ovenlybytes.app.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.repository.BakeryRepository
import com.ovenlybytes.app.model.ShopDetails
import com.ovenlybytes.app.util.BackupManager
import com.ovenlybytes.app.util.ShopPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BackupUiState {
    object Idle : BackupUiState
    object Loading : BackupUiState
    data class Success(val message: String) : BackupUiState
    data class Error(val message: String) : BackupUiState
}

class SettingsViewModel(
    private val repository: BakeryRepository,
    private val shopPreferences: ShopPreferences
) : ViewModel() {

    private val _shopDetails = MutableStateFlow(shopPreferences.getShopDetails())
    val shopDetails: StateFlow<ShopDetails> = _shopDetails.asStateFlow()

    private val _appTheme = MutableStateFlow(shopPreferences.getAppTheme())
    val appTheme: StateFlow<com.ovenlybytes.app.model.AppTheme> = _appTheme.asStateFlow()

    private val _backupUiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val backupUiState: StateFlow<BackupUiState> = _backupUiState.asStateFlow()

    fun selectAppTheme(theme: com.ovenlybytes.app.model.AppTheme) {
        viewModelScope.launch {
            shopPreferences.saveAppTheme(theme)
            _appTheme.value = theme
            _backupUiState.value = BackupUiState.Success("App theme changed to ${theme.displayName}!")
        }
    }

    fun updateShopDetails(newDetails: ShopDetails) {
        _shopDetails.value = newDetails
    }

    fun saveShopDetails() {
        viewModelScope.launch {
            shopPreferences.saveShopDetails(_shopDetails.value)
            _backupUiState.value = BackupUiState.Success("Shop profile updated successfully!")
        }
    }

    fun exportBackup(context: Context, uri: Uri, format: com.ovenlybytes.app.util.BackupFormat) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = BackupManager.exportBackup(context, uri, format, shopPreferences, repository)
            result.fold(
                onSuccess = { summary ->
                    _backupUiState.value = BackupUiState.Success(summary)
                },
                onFailure = { error ->
                    _backupUiState.value = BackupUiState.Error(error.message ?: "Failed to export backup")
                }
            )
        }
    }

    fun importBackup(context: Context, uri: Uri, format: com.ovenlybytes.app.util.BackupFormat) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val result = BackupManager.importBackup(context, uri, format, shopPreferences, repository)
            result.fold(
                onSuccess = { summary ->
                    // Reload shop details after import
                    _shopDetails.value = shopPreferences.getShopDetails()
                    _backupUiState.value = BackupUiState.Success(summary)
                },
                onFailure = { error ->
                    _backupUiState.value = BackupUiState.Error(error.message ?: "Failed to import backup")
                }
            )
        }
    }

    fun importBackupAuto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _backupUiState.value = BackupUiState.Loading
            val format = try {
                val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        cursor.getString(nameIndex)
                    } else ""
                } ?: ""
                when {
                    fileName.endsWith(".sql", ignoreCase = true) -> com.ovenlybytes.app.util.BackupFormat.SQL
                    fileName.endsWith(".csv", ignoreCase = true) -> com.ovenlybytes.app.util.BackupFormat.CSV
                    else -> com.ovenlybytes.app.util.BackupFormat.JSON
                }
            } catch (e: Exception) {
                com.ovenlybytes.app.util.BackupFormat.SQL
            }

            val result = BackupManager.importBackup(context, uri, format, shopPreferences, repository)
            result.fold(
                onSuccess = { summary ->
                    _shopDetails.value = shopPreferences.getShopDetails()
                    _backupUiState.value = BackupUiState.Success(summary)
                },
                onFailure = { error ->
                    _backupUiState.value = BackupUiState.Error(error.message ?: "Failed to import backup")
                }
            )
        }
    }

    fun resetBackupUiState() {
        _backupUiState.value = BackupUiState.Idle
    }

    class Factory(
        private val repository: BakeryRepository,
        private val shopPreferences: ShopPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository, shopPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
