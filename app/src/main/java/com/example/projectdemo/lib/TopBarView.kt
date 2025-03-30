package com.example.projectdemo.lib

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.projectdemo.R

@Composable
fun TopBarTitleAndBackView(
    modifier: Modifier = Modifier,
    title: String? = null,
    iconEnd: (@Composable BoxScope.() -> Unit)? = null,
    showBackIcon: Boolean = true
) {
    AppColumn(
        modifier = modifier.backgroundItem(
            bottomEndRadius = MyAppTheme.appDimens.sizeRadiusMedium,
            bottomStartRadius = MyAppTheme.appDimens.sizeRadiusMedium,
        )
    ) {
        AppSpacerStatusBar()
        AppBox(
            modifier = Modifier
                .padding(
                    top = MyAppTheme.appDimens.paddingTiny,
                    start = MyAppTheme.appDimens.padding,
                    end = MyAppTheme.appDimens.padding,
                    bottom = MyAppTheme.appDimens.padding,
                )
                .fillMaxWidth(),
        ) {
            if(showBackIcon){
//                Icon(painterResource(R.drawable.))
            }
            AppTextBold(
                text = title ?: stringResource(id = R.string.app_name),
                color = MyAppTheme.appColor.textNormal,
                fontSize = MyAppTheme.appDimens.fontSizeMedium,
                modifier = Modifier.align(Alignment.Center)
            )
            if (iconEnd != null) {
                AppBox(Modifier.align(Alignment.CenterEnd)) {
                    iconEnd()
                }
            }
        }
    }
}