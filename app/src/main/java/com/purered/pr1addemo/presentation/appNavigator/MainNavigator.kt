package com.purered.pr1addemo.presentation.appNavigator

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.purered.pr1addemo.presentation.shop.ShopScreen
import com.purered.pr1addemo.presentation.weeklyAd.WeeklyAdScreen
import com.purered.pr1addemo.presentation.weeklyAd.WeeklyAdsViewModel
import com.purered.pr1addemo.ui.theme.NewsAppTheme


data class BottomNavItem(val route: String,
                         val icon: ImageVector,
                         val title: String,
                         val badgeCount: Int = 0)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigator() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("home", Icons.Default.Home, "Home"),
        BottomNavItem("weeklyAds", Icons.Default.Description, "WeeklyAds"),
        BottomNavItem("savings", Icons.Default.Receipt, "Savings"),
        BottomNavItem("shop", Icons.Rounded.ShoppingBag , "Shop"),
        BottomNavItem("cart", Icons.Default.ShoppingCart, "Cart", badgeCount = 4)
    )
    var currentTitle by remember { mutableStateOf(items[0].title) }
    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text(text = currentTitle) }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController,items, onItemSelected =  {
                    selectedTitle ->
                currentTitle = selectedTitle

            })
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = "home",
            Modifier.padding(innerPadding)
        ) {
            composable("home") { ShopScreen() }
            composable("weeklyAds") {

                WeeklyAdScreen()
            }
            composable("savings") { SavingsScreen() }
            composable("shop") { ShopScreen() }
            composable("cart") { CartScreen() }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(navController: NavController, items:List<BottomNavItem>, onItemSelected: (String) -> Unit) {

    NavigationBar {
        val currentRoute = currentRoute(navController)
        items.forEach { item ->
            val isSelected =   currentRoute == item.route
            NavigationBarItem(
                icon = {
                    if (item.route == "cart" && item.badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(item.badgeCount.toString())
                                }
                            }
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Icon(
                            item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {   Text(
                    fontSize = 10.sp,
                    text = item.title,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        onItemSelected(item.title)
                    }
                }
            )
        }
    }
}




@Composable
fun SavingsScreen() {
    Text(text = "Savings Screen")
}


@Composable
fun CartScreen() {
    Text(text = "Cart Screen")
}

@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NewsAppTheme {
        MainNavigator()
    }
}