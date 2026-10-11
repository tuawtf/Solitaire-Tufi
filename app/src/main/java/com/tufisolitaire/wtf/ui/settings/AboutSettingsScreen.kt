package com.tufisolitaire.wtf.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tufisolitaire.wtf.BuildConfig
import com.tufisolitaire.wtf.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val linkFailedMsg = stringResource(R.string.msg_link_open_failed)
  fun openUrl(url: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
      scope.launch {
        snackbarHostState.showSnackbar(linkFailedMsg)
      }
    } catch (_: Exception) {
      scope.launch {
        snackbarHostState.showSnackbar(linkFailedMsg)
      }
    }
  }

  val versionName = try {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    packageInfo.versionName ?: BuildConfig.VERSION_NAME
  } catch (_: Exception) {
    BuildConfig.VERSION_NAME
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.settings_about),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("about_settings_back_button"),
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
          .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        // En üstte büyük, ortalı logo: ic_app_logo ikonla aynı renklerle (koyu arka plan #1B1B2F üzerinde beyaz glif) yuvarlak köşeli kare içinde ~140dp
        Surface(
          shape = RoundedCornerShape(28.dp),
          color = Color(0xFF1B1B2F),
          modifier = Modifier
            .size(140.dp)
            .testTag("about_app_logo"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              painter = painterResource(id = R.drawable.ic_app_logo),
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(78.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Düz ListItem listesi (ayırıcı çizgi yok, sol outlined ikon, başlık, altta açıklama)
        // 1. Neler yeni? -> Sürüm notlarını gör -> Releases
        AboutListItem(
          headline = stringResource(R.string.about_whats_new),
          supporting = stringResource(R.string.about_whats_new_desc),
          icon = Icons.Outlined.NewReleases,
          testTag = "about_item_whats_new",
          onClick = { openUrl(AppLinks.RELEASES_URL) },
        )

        // 2. Sürüm -> versionName -> dokununca bir şey yapmaz
        AboutListItem(
          headline = stringResource(R.string.about_version),
          supporting = versionName,
          icon = Icons.Outlined.Info,
          testTag = "about_item_version",
          onClick = null,
        )

        // 3. Geliştirici -> AppInfo.AUTHOR
        AboutListItem(
          headline = stringResource(R.string.about_developer),
          supporting = AppInfo.AUTHOR,
          icon = Icons.Outlined.Person,
          testTag = "about_item_developer",
          onClick = null,
        )

        // 4. Lisans -> MIT -> LICENSE
        AboutListItem(
          headline = stringResource(R.string.about_license),
          supporting = stringResource(R.string.about_license_mit),
          icon = Icons.Outlined.Balance,
          testTag = "about_item_license",
          onClick = { openUrl(AppLinks.LICENSE_URL) },
        )

        // 5. Kaynak kodu -> GitHub'da incele -> repo
        AboutListItem(
          headline = stringResource(R.string.about_source_code),
          supporting = stringResource(R.string.about_source_code_desc),
          icon = Icons.Outlined.Code,
          testTag = "about_item_source_code",
          onClick = { openUrl(AppLinks.GITHUB_URL) },
        )

        // 6. Hata bildir -> GitHub'da kayıt aç -> issues
        AboutListItem(
          headline = stringResource(R.string.about_report_issue),
          supporting = stringResource(R.string.about_report_issue_desc),
          icon = Icons.Outlined.BugReport,
          testTag = "about_item_report_issue",
          onClick = { openUrl(AppLinks.ISSUES_URL) },
        )
      }
    }
  }
}

@Composable
private fun AboutListItem(
  headline: String,
  supporting: String,
  icon: ImageVector,
  testTag: String,
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val clickModifier = if (onClick != null) {
    Modifier.clickable(onClick = onClick)
  } else {
    Modifier
  }

  ListItem(
    headlineContent = {
      Text(
        text = headline,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
    },
    supportingContent = {
      Text(
        text = supporting,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    },
    leadingContent = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp),
      )
    },
    modifier = modifier
      .fillMaxWidth()
      .then(clickModifier)
      .testTag(testTag),
  )
}
