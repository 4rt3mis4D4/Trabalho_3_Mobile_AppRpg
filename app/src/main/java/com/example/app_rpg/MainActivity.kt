package com.example.app_rpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.app_rpg.ui.theme.App_rpgTheme
import com.example.app_rpg.ui.theme.LocalModePalette
import com.example.app_rpg.ui.theme.MasterPalette
import com.example.app_rpg.ui.theme.ModePalette
import com.example.app_rpg.ui.theme.PlayerPalette
import com.example.app_rpg.ui.theme.corBrancoPuro
import com.example.app_rpg.ui.theme.corCinzaEscuro
import com.example.app_rpg.ui.theme.corCinzaMedio
import com.example.app_rpg.ui.theme.corPretoPuro

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App_rpgTheme {
                App()
            }
        }
    }
}

object Routes {
    const val DICE = "dice"
    const val CHARACTER_SHEET = "character_sheet"
    const val PLAYLIST = "playlist"
    const val ITEMS = "items"
    const val STORE_LIST = "stores"

    const val STORE_ID_ARG = "storeId"
    const val STORE = "store/{$STORE_ID_ARG}"
    const val STORE_REGISTRATION = "store_registration?$STORE_ID_ARG={$STORE_ID_ARG}"

    fun store(storeId: Int) = "store/$storeId"
    fun storeRegistration(storeId: Int? = null) =
        if (storeId == null) "store_registration" else "store_registration?$STORE_ID_ARG=$storeId"

    const val ITEM_ID_ARG = "itemId"
    const val ITEM_REGISTRATION = "item_registration?$ITEM_ID_ARG={$ITEM_ID_ARG}"

    fun itemRegistration(itemId: Int? = null) =
        if (itemId == null) "item_registration" else "item_registration?$ITEM_ID_ARG=$itemId"
}

enum class Destination(
    val label: String,
    @param:DrawableRes val iconRes: Int,
    val route: String,
    val internalRoutes: Set<String> = emptySet()
) {
    Dice("Dados", R.drawable.ic_dice, Routes.DICE),
    CharacterSheet("Ficha", R.drawable.ic_character_sheet, Routes.CHARACTER_SHEET),
    Playlist("Música", R.drawable.ic_playlist, Routes.PLAYLIST),
    Items("Itens", R.drawable.icon_tools, Routes.ITEMS, setOf(Routes.ITEM_REGISTRATION)),
    StoreList(
        "Lojas",
        R.drawable.ic_store,
        Routes.STORE_LIST,
        setOf(Routes.STORE, Routes.STORE_REGISTRATION)
    );

    fun isSelected(route: String?) = route == this.route || route in internalRoutes
}

enum class Mode(
    val label: String,
    val tabs: List<Destination>
) {
    Player("Jogador", listOf(Destination.Dice, Destination.CharacterSheet, Destination.StoreList)),
    Master("Mestre", listOf(Destination.Playlist, Destination.Items, Destination.StoreList));

    val palette: ModePalette
        get() = if (this == Player) PlayerPalette else MasterPalette

    val other: Mode
        get() = if (this == Player) Master else Player
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var user by rememberSaveable { mutableStateOf(User()) }
    val mode = user.mode
    val viewModel: MeuViewModel = viewModel()
    val lojas = viewModel.lojas
    val itens = viewModel.itens

    val routeStoreId = backStackEntry?.arguments?.getInt(Routes.STORE_ID_ARG)
    val routeStore = lojas.firstOrNull { it.id == routeStoreId }
    val storeItems = itens.filter { routeStore != null && it.id in routeStore.itemIds }
    val routeItemId = backStackEntry?.arguments?.getInt(Routes.ITEM_ID_ARG)
    val routeItem = itens.firstOrNull { it.id == routeItemId }
    val tab = Destination.entries.firstOrNull { it.route == currentRoute }

    val palette = mode.palette

    CompositionLocalProvider(LocalModePalette provides palette) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = corPretoPuro,
            topBar = {
                AppTopBar(
                    title = when (currentRoute) {
                        Routes.STORE -> routeStore?.nome ?: "Loja"
                        Routes.STORE_REGISTRATION -> if (routeStore == null) "Criar Loja" else "Editar Loja"
                        Routes.ITEM_REGISTRATION -> if (routeItem == null) "Criar Item" else "Editar Item"
                        else -> tab?.label.orEmpty()
                    },
                    subtitle = if (currentRoute == Routes.STORE) {
                        if (storeItems.size == 1) "1 item à venda" else "${storeItems.size} itens à venda"
                    } else {
                        null
                    },
                    onBack = if (tab == null) {
                        { navController.popBackStack() }
                    } else {
                        null
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = corCinzaEscuro) {
                    mode.tabs.forEach { destination ->
                        NavigationBarItem(
                            selected = destination.isSelected(currentRoute),
                            onClick = { navController.navigateToTab(destination.route) },
                            icon = {
                                Icon(
                                    painter = painterResource(destination.iconRes),
                                    contentDescription = destination.label
                                )
                            },
                            label = {
                                Text(
                                    text = destination.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = corBrancoPuro,
                                selectedTextColor = corBrancoPuro,
                                indicatorColor = palette.accent,
                                unselectedIconColor = corCinzaMedio,
                                unselectedTextColor = corCinzaMedio,
                            )
                        )
                    }

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            val newMode = mode.other
                            user = user.copy(mode = newMode)
                            val stillAvailable = newMode.tabs.any { it.isSelected(currentRoute) }
                            if (newMode == Mode.Player && currentRoute == Routes.STORE_REGISTRATION) {
                                navController.popBackStack(Routes.STORE_LIST, inclusive = false)
                            } else if (!stillAvailable) {
                                navController.navigateToTab(newMode.tabs.first().route)
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.icon_swap),
                                contentDescription = "Alternar para modo ${mode.other.label}"
                            )
                        },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = palette.accent,
                            unselectedTextColor = palette.accent,
                        )
                    )
                }
            }
        ) { innerPadding ->
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

            NavHost(
                navController = navController,
                startDestination = Routes.DICE
            ) {
                composable(Routes.DICE) { DiceScreen(contentModifier, viewModel) }
                composable(Routes.CHARACTER_SHEET) {
                    FichaPersonagemScreen(
                        modifier = contentModifier,
                        viewModel = viewModel
                    )
                }
                composable(Routes.PLAYLIST) { Playlist(contentModifier) }

                composable(Routes.ITEMS) {
                    ItemListScreen(
                        modifier = contentModifier,
                        viewModel = viewModel,
                        onItemClick = { item ->
                            navController.navigate(Routes.itemRegistration(item.id))
                        },
                        onAdd = {
                            navController.navigate(Routes.itemRegistration())
                        },
                        onEdit = { item ->
                            navController.navigate(Routes.itemRegistration(item.id))
                        }
                    )
                }

                composable(
                    route = Routes.ITEM_REGISTRATION,
                    arguments = listOf(
                        navArgument(Routes.ITEM_ID_ARG) {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { entry ->
                    val itemId = entry.arguments?.getInt(Routes.ITEM_ID_ARG)
                    val editingItem = itens.firstOrNull { it.id == itemId }

                    ItemRegistrationScreen(
                        modifier = contentModifier,
                        viewModel = viewModel,
                        formKey = entry.id,
                        initialItem = editingItem,
                        onSave = { item ->
                            viewModel.salvarItem(item)
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.STORE_LIST) {
                    StoreListScreen(
                        modifier = contentModifier,
                        viewModel = viewModel,
                        isMaster = mode == Mode.Master,
                        onLojaClick = { loja ->
                            navController.navigate(Routes.store(loja.id))
                        },
                        onAdicionar = {
                            navController.navigate(Routes.storeRegistration())
                        },
                        onEditar = { loja ->
                            navController.navigate(Routes.storeRegistration(loja.id))
                        }
                    )
                }

                composable(
                    route = Routes.STORE,
                    arguments = listOf(navArgument(Routes.STORE_ID_ARG) { type = NavType.IntType })
                ) {
                    StoreScreen(
                        modifier = contentModifier,
                        viewModel = viewModel,
                        lojaId = routeStore?.id,
                        isMaster = mode == Mode.Master,
                        onItemClick = { item ->
                            if (mode == Mode.Master) {
                                navController.navigate(Routes.itemRegistration(item.id))
                            }
                        }
                    )
                }

                composable(
                    route = Routes.STORE_REGISTRATION,
                    arguments = listOf(
                        navArgument(Routes.STORE_ID_ARG) {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { entry ->
                    val storeId = entry.arguments?.getInt(Routes.STORE_ID_ARG)
                    val editingStore = lojas.firstOrNull { it.id == storeId }

                    StoreRegistrationScreen(
                        modifier = contentModifier,
                        viewModel = viewModel,
                        formKey = entry.id,
                        lojaInicial = editingStore,
                        onCancelar = { navController.popBackStack() },
                        onSalvar = { loja ->
                            viewModel.salvarLoja(loja)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AppPreview() {
    App_rpgTheme {
        App()
    }
}
