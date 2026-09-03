package com.purered.pr1digitalad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold

import com.purered.pr1digitalad.screens.CartScreen
import com.purered.pr1digitalad.screens.HomeScreen
import com.purered.pr1digitalad.screens.OneAdScreen
import com.purered.pr1digitalad.screens.WeeklyAdScreen
import com.purered.pr1digitalad.ui.theme.PR1DigitalAdTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PR1DigitalAdTheme {
                PR1DigitalAdApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun PR1DigitalAdApp() {

    var currentDestination by rememberSaveable {
        mutableStateOf(AppDestinations.HOME)
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {

            AppDestinations.entries.forEach { destination ->

                item(
                    icon = {
                        Icon(
                            painter = painterResource(destination.icon),
                            contentDescription = destination.label,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(destination.label)
                    },
                    selected = destination == currentDestination,
                    onClick = {
                        currentDestination = destination
                    }
                )
            }
        }
    ) {

        when (currentDestination) {

            AppDestinations.HOME -> {
                HomeScreen()
            }

            AppDestinations.CLASSICAD -> {
                WeeklyAdScreen()
            }

            AppDestinations.ONEAD -> {
                OneAdScreen()
            }

            AppDestinations.CART -> {
                CartScreen()
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int
) {
    HOME(
        label = "Home",
        icon = R.drawable.ic_home
    ),

    CLASSICAD(
        label = "Weekly Ad",
        icon = R.drawable.ic_newspaper
    ),

    ONEAD(
        label = "One Ad",
        icon = R.drawable.ic_newspaper
    ),

    CART(
        label = "Cart",
        icon = R.drawable.ic_cart
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PR1DigitalAdTheme {
        HomeScreen()
    }
}