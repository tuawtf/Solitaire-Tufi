package com.tufisolitaire.wtf.ui.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tufisolitaire.wtf.R
import com.tufisolitaire.wtf.data.settings.AppLanguage
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val settings by viewModel.settings.collectAsStateWithLifecycle()

  fun applyLanguage(lang: AppLanguage) {
    viewModel.setAppLanguage(lang)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      val localeManager = context.getSystemService(LocaleManager::class.java)
      val localeList = when (lang) {
        AppLanguage.SYSTEM -> LocaleList.getEmptyLocaleList()
        AppLanguage.TR -> LocaleList.forLanguageTags("tr")
        AppLanguage.EN -> LocaleList.forLanguageTags("en")
      }
      localeManager?.applicationLocales = localeList
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("language_settings_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.action_back),
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface,
          titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
      )
    },
    modifier = modifier.fillMaxSize(),
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 640.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 8.dp),
      ) {
        val options = listOf(
          AppLanguage.SYSTEM to stringResource(R.string.language_system),
          AppLanguage.TR to stringResource(R.string.language_turkish),
          AppLanguage.EN to stringResource(R.string.language_english),
        )

        options.forEachIndexed { index, (lang, title) ->
          val isSelected = settings.appLanguage == lang
          ListItem(
            headlineContent = {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              )
            },
            trailingContent = {
              RadioButton(
                selected = isSelected,
                onClick = { applyLanguage(lang) },
                modifier = Modifier.testTag("radio_lang_${lang.name.lowercase()}"),
              )
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable { applyLanguage(lang) }
              .testTag("item_lang_${lang.name.lowercase()}"),
          )
          if (index < options.lastIndex) {
            HorizontalDivider(
              modifier = Modifier.padding(horizontal = 16.dp),
              color = MaterialTheme.colorScheme.outlineVariant,
            )
          }
        }
      }
    }
  }
}
