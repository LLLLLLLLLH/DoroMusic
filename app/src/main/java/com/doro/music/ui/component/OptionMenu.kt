package com.doro.music.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

private class MenuLayer(
    // 当前层级的标题（用于子菜单顶部的返回按钮显示）
    val title: @Composable () -> Unit,
    // 当前层级的菜单内容
    val content: @Composable OptionMenuScope.() -> Unit
)

@Composable
fun OptionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable OptionMenuScope.() -> Unit
) {
    val stack = remember(expanded) { mutableStateListOf<MenuLayer>() }

    // 如果栈为空，将最外层的 content 作为根节点入栈
    if (stack.isEmpty()) {
        stack.add(MenuLayer(title = {}, content = content))
    }

    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        if (stack.size > 1) {
            DropdownMenuItem(
                text = stack.last().title,
                onClick = { stack.removeLastOrNull() }
            )
        }
        // 当前层级的菜单项
        val scope = OptionMenuScopeImpl(
            onDismissRequest = onDismissRequest,
            navigateTo = { newLayer -> stack.add(newLayer) }
        )
        stack.last().content(scope)
    }
}

interface OptionMenuScope {
    @Composable
    fun Option(onClick: () -> Unit, content: @Composable () -> Unit)

    @Composable
    fun OptionGroup(
        groupLabel: @Composable () -> Unit,
        content: @Composable OptionMenuScope.() -> Unit
    )

    @Composable
    fun <T> SingleChoiceGroup(
        currentValue: T,
        onValueChanged: (T) -> Unit,
        choices: @Composable SingleChoiceGroupScope<T>.() -> Unit
    )

    @Composable
    fun ToggleOption(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        content: @Composable () -> Unit
    )
}

interface SingleChoiceGroupScope<T> {
    @Composable
    fun Choice(value: T, label: String)
}

private val LocalCurrentChoiceState = compositionLocalOf<Any?> { null }
private val LocalCurrentChoiceCallback = compositionLocalOf<(Any?) -> Unit> { {} }

@Suppress("UNCHECKED_CAST")
private class OptionMenuScopeImpl(
    private val onDismissRequest: () -> Unit,
    private val navigateTo: (MenuLayer) -> Unit // 🌟 传入导航方法
) : OptionMenuScope {
    @Composable
    override fun Option(onClick: () -> Unit, content: @Composable () -> Unit) {
        DropdownMenuItem(
            text = { content() },
            onClick = {
                onClick()
                onDismissRequest()
            }
        )
    }

    @Composable
    override fun ToggleOption(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        content: @Composable () -> Unit
    ) {
        DropdownMenuItem(
            text = { content() },
            trailingIcon = {
                Checkbox(checked = checked, onCheckedChange = null)
            },
            onClick = {
                onCheckedChange(!checked)
                onDismissRequest()
            }
        )
    }

    @Composable
    override fun OptionGroup(
        groupLabel: @Composable () -> Unit,
        content: @Composable OptionMenuScope.() -> Unit
    ) {
        DropdownMenuItem(
            text = { groupLabel() },
            trailingIcon = { Icon(Icons.Rounded.ChevronRight, null) },
            onClick = { navigateTo(MenuLayer(title = groupLabel, content = content)) }
        )
    }

    @Composable
    override fun <T> SingleChoiceGroup(
        currentValue: T,
        onValueChanged: (T) -> Unit,
        choices: @Composable SingleChoiceGroupScope<T>.() -> Unit
    ) {
        CompositionLocalProvider(
            LocalCurrentChoiceState provides currentValue,
            LocalCurrentChoiceCallback provides onValueChanged as (Any?) -> Unit
        ) {
            val scope = SingleChoiceGroupScopeImpl<T>(onDismissRequest)
            scope.choices()
        }
    }

    private class SingleChoiceGroupScopeImpl<T>(
        private val dismiss: () -> Unit
    ) : SingleChoiceGroupScope<T> {
        @Composable
        override fun Choice(value: T, label: String) {
            val currentValue = LocalCurrentChoiceState.current
            val onValueChanged = LocalCurrentChoiceCallback.current
            val isSelected = value == currentValue
            DropdownMenuItem(
                text = { Text(text = label) },
                trailingIcon = {
                    RadioButton(selected = isSelected, onClick = null)
                },
                onClick = {
                    onValueChanged(value)
                    dismiss()
                },
                colors = MenuDefaults.itemColors(
                    textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}