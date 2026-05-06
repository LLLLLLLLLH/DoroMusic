package com.doro.music.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.doro.music.R
import com.doro.music.ext.getAppVersionName
import com.doro.music.ext.openUrl
import com.doro.music.ui.component.SettingsScaffold

fun EntryProviderScope<NavKey>.aboutSettingsEntry(onBack: () -> Unit) {
    entry<SettingsNavKey.About> {
        AboutSettingsScreen(onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val versionName = remember { context.getAppVersionName() }
    val githubUrl = stringResource(R.string.github_url)

    SettingsScaffold(
        title = stringResource(R.string.about_app),
        onBack = onBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = { Text(versionName) }
            )

            ListItem(
                modifier = Modifier.clickable {
                    context.openUrl(githubUrl)
                },
                headlineContent = { Text(stringResource(R.string.open_source_licenses)) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = stringResource(R.string.open_source_licenses)
                    )
                }
            )
        }
    }
}
