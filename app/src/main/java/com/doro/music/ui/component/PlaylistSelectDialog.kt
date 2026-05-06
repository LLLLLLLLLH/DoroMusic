package com.doro.music.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.paging.compose.LazyPagingItems
import com.doro.music.R
import com.doro.music.data.model.Playlist

@Composable
fun PlaylistSelectDialog(
    modifier: Modifier = Modifier,
    show: Boolean,
    items: LazyPagingItems<Playlist>,
    onConfirm: (Set<Playlist>) -> Unit,
    onDismiss: () -> Unit,
) {

    val selectedItems = rememberSaveable(show) { mutableStateSetOf<Playlist>() }

    if (show) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                modifier = modifier,
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column {

                    Text(
                        text = stringResource(R.string.add_to_playlist),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Box(modifier = Modifier.heightIn(max = LocalWindowInfo.current.containerDpSize.height.times(0.6F))) {
                        LazyColumn {
                            items(count = items.itemCount, key = { items[it]?.id ?: it }) { index ->
                                val curItem = items[index] ?: return@items
                                val curSelected = curItem in selectedItems
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            role = Role.Checkbox,
                                            onClick = {
                                                if (curSelected) selectedItems.remove(curItem) else selectedItems.add(curItem)
                                            }
                                        )
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Checkbox(checked = curSelected, onCheckedChange = null)
                                    Text(
                                        text = curItem.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            onConfirm(selectedItems)
                            onDismiss()
                        }) {
                            Text(text = stringResource(R.string.confirm))
                        }
                    }
                }
            }
        }
    }
}