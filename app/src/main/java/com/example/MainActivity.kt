package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.repository.CartRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOutUser
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoryScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.ProfileAndSellerScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.DMYmartTheme
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.DMYmartViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      DMYmartTheme {
        AppNavigation()
      }
    }
  }
}

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    key(user.uid) {
      DMYmartMainApp(
        currentUserId = user.uid,
        displayName = user.displayName ?: "Sahabat DMYmart",
        email = user.email ?: "",
        photoUrl = user.photoUrl?.toString(),
      )
    }
  }
}

private data class BottomTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val bottomTabs =
  listOf(
    BottomTab("home", "Beranda", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    BottomTab("categories", "Kategori", Icons.Rounded.Category, Icons.Outlined.Category, "nav_categories"),
    BottomTab("cart", "Keranjang", Icons.Rounded.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_cart"),
    BottomTab("orders", "Pesanan", Icons.Rounded.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_orders"),
    BottomTab("profile", "Saya", Icons.Rounded.Person, Icons.Outlined.Person, "nav_profile"),
  )

@Composable
fun DMYmartMainApp(
  currentUserId: String,
  displayName: String,
  email: String,
  photoUrl: String?,
) {
  val viewModel: DMYmartViewModel =
    viewModel(
      key = currentUserId,
      factory =
        viewModelFactory {
          initializer {
            val app =
              checkNotNull(this[APPLICATION_KEY]) {
                "APPLICATION_KEY missing from CreationExtras"
              }
            val databaseId = app.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(databaseId)
            DMYmartViewModel(
              productRepository = ProductRepository(db),
              cartRepository = CartRepository(db),
              orderRepository = OrderRepository(db),
              userRepository = UserRepository(db),
              currentUserId = currentUserId,
              currentDisplayName = displayName,
              currentEmail = email,
              currentPhotoUrl = photoUrl,
            )
          }
        },
    )

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }

  val productsState by viewModel.productsState.collectAsStateWithLifecycle()
  val sellerProductsState by viewModel.sellerProductsState.collectAsStateWithLifecycle()
  val cartState by viewModel.cartState.collectAsStateWithLifecycle()
  val ordersState by viewModel.ordersState.collectAsStateWithLifecycle()
  val profile by viewModel.profileState.collectAsStateWithLifecycle()
  val addresses by viewModel.addressesState.collectAsStateWithLifecycle()
  val favorites by viewModel.favoritesState.collectAsStateWithLifecycle()

  val selectedHomeCategory by viewModel.selectedHomeCategory.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val submittedQuery by viewModel.submittedQuery.collectAsStateWithLifecycle()
  val searchSuggestions by viewModel.searchSuggestions.collectAsStateWithLifecycle()
  val searchFilter by viewModel.searchFilter.collectAsStateWithLifecycle()
  val searchResults by viewModel.filteredSearchResults.collectAsStateWithLifecycle()
  val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
  val selectedVoucher by viewModel.selectedVoucher.collectAsStateWithLifecycle()
  val buyNowItems by viewModel.buyNowItems.collectAsStateWithLifecycle()
  val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

  val cartItems = (cartState as? UiState.Success)?.data ?: emptyList()
  val cartBadgeCount = cartItems.sumOf { it.quantity }.toInt()

  LaunchedEffect(toastMessage) {
    val msg = toastMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearToast()
    }
  }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val showBottomBar = currentRoute in bottomTabs.map { it.route }

  fun navigateTab(route: String) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useRail && showBottomBar) {
        NavigationRail(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxHeight(),
        ) {
          bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationRailItem(
              selected = selected,
              onClick = { navigateTab(tab.route) },
              icon = {
                TabIcon(
                  tab = tab,
                  selected = selected,
                  cartBadgeCount = cartBadgeCount,
                )
              },
              label = {
                Text(
                  text = tab.label,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
              },
              colors =
                NavigationRailItemDefaults.colors(
                  selectedIconColor = ShopeeOrange,
                  selectedTextColor = ShopeeOrange,
                  indicatorColor = ShopeeOrangeSoft,
                ),
              modifier = Modifier.testTag(tab.testTag),
            )
          }
        }
      }

      Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
          if (!useRail && showBottomBar) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 4.dp,
            ) {
              bottomTabs.forEach { tab ->
                val selected = currentRoute == tab.route
                NavigationBarItem(
                  selected = selected,
                  onClick = { navigateTab(tab.route) },
                  icon = {
                    TabIcon(
                      tab = tab,
                      selected = selected,
                      cartBadgeCount = cartBadgeCount,
                    )
                  },
                  label = {
                    Text(
                      text = tab.label,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  colors =
                    NavigationBarItemDefaults.colors(
                      selectedIconColor = ShopeeOrange,
                      selectedTextColor = ShopeeOrange,
                      indicatorColor = ShopeeOrangeSoft,
                    ),
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }
        },
      ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize(),
          ) {
            composable("home") {
              HomeScreen(
                productsState = productsState,
                profile = profile,
                cartItemCount = cartBadgeCount,
                selectedCategory = selectedHomeCategory,
                onSelectCategory = viewModel::selectHomeCategory,
                onOpenSearch = { navController.navigate("search") },
                onOpenCart = { navigateTab("cart") },
                onOpenAllCategories = { navigateTab("categories") },
                onProductClick = { prod -> navController.navigate("product/${prod.id}") },
                onAddToCart = { prod -> viewModel.addToCart(prod, 1) },
                onClaimCoins = viewModel::claimDailyCoins,
              )
            }

            composable("categories") {
              CategoryScreen(
                productsState = productsState,
                onOpenSearch = { navController.navigate("search") },
                onOpenSearchWithCategory = { category ->
                  viewModel.applySearchCategoryShortcut(category)
                  navController.navigate("search")
                },
                onProductClick = { prod -> navController.navigate("product/${prod.id}") },
                onAddToCart = { prod -> viewModel.addToCart(prod, 1) },
              )
            }

            composable("cart") {
              CartScreen(
                cartState = cartState,
                selectedVoucher = selectedVoucher,
                onToggleSelectItem = viewModel::toggleCartItemSelection,
                onToggleSelectAll = viewModel::toggleSelectAllCart,
                onIncrementItem = viewModel::incrementCartItem,
                onDecrementItem = viewModel::decrementCartItem,
                onRemoveItem = viewModel::removeCartItem,
                onRemoveSelected = viewModel::removeSelectedCartItems,
                onSelectVoucher = viewModel::selectVoucher,
                onProductClick = { prodId -> navController.navigate("product/$prodId") },
                onGoShopping = { navigateTab("home") },
                onCheckout = {
                  viewModel.clearBuyNow()
                  navController.navigate("checkout/true")
                },
              )
            }

            composable("orders") {
              OrdersScreen(
                ordersState = ordersState,
                onAdvanceStatus = viewModel::advanceOrderStatus,
                onReorder = { order ->
                  viewModel.reorderOrder(order)
                  navigateTab("cart")
                },
                onGoShopping = { navigateTab("home") },
              )
            }

            composable("profile") {
              ProfileAndSellerScreen(
                profile = profile,
                sellerProductsState = sellerProductsState,
                addresses = addresses,
                favorites = favorites,
                isBusy = isBusy,
                onClaimCoins = viewModel::claimDailyCoins,
                onSaveSellerProduct = viewModel::saveSellerProduct,
                onDeleteSellerProduct = viewModel::deleteSellerProduct,
                onAddAddress = viewModel::addAddress,
                onSetDefaultAddress = viewModel::setDefaultAddress,
                onDeleteAddress = viewModel::deleteAddress,
                onOpenProduct = { prodId -> navController.navigate("product/$prodId") },
                onSignOut = { signOutUser(credentialManager, coroutineScope) },
              )
            }

            composable("search") {
              SearchScreen(
                query = searchQuery,
                submittedQuery = submittedQuery,
                suggestions = searchSuggestions,
                results = searchResults,
                recentSearches = recentSearches,
                popularSearches = viewModel.popularSearches,
                filterState = searchFilter,
                onQueryChange = viewModel::updateSearchQuery,
                onSubmitSearch = viewModel::submitSearch,
                onClearRecent = viewModel::clearRecentSearches,
                onUpdateFilter = viewModel::updateSearchFilter,
                onResetFilter = viewModel::resetSearchFilter,
                onProductClick = { prod -> navController.navigate("product/${prod.id}") },
                onAddToCart = { prod -> viewModel.addToCart(prod, 1) },
                onBack = { navController.popBackStack() },
              )
            }

            composable(
              route = "product/{productId}",
              arguments = listOf(navArgument("productId") { type = NavType.StringType }),
            ) { backStackEntry ->
              val productId = backStackEntry.arguments?.getString("productId") ?: ""
              val allProducts = (productsState as? UiState.Success)?.data ?: emptyList()
              val sellerProducts = (sellerProductsState as? UiState.Success)?.data ?: emptyList()
              val product =
                allProducts.firstOrNull { it.id == productId }
                  ?: sellerProducts.firstOrNull { it.id == productId }

              if (product == null) {
                Box(modifier = Modifier.fillMaxSize()) {
                  Text(
                    text = "Produk tidak ditemukan",
                    modifier = Modifier.padding(24.dp),
                  )
                }
              } else {
                val isFav = favorites.any { it.productId == product.id }
                ProductDetailScreen(
                  product = product,
                  isFavorite = isFav,
                  cartCount = cartBadgeCount,
                  onToggleFavorite = { viewModel.toggleFavorite(product) },
                  onAddToCart = { qty -> viewModel.addToCart(product, qty) },
                  onBuyNow = { qty ->
                    viewModel.setBuyNowProduct(product, qty)
                    navController.navigate("checkout/false")
                  },
                  onCategoryClick = { category ->
                    viewModel.applySearchCategoryShortcut(category)
                    navController.navigate("search")
                  },
                  onOpenCart = {
                    navController.popBackStack("home", inclusive = false)
                    navigateTab("cart")
                  },
                  onBack = { navController.popBackStack() },
                )
              }
            }

            composable(
              route = "checkout/{isFromCart}",
              arguments = listOf(navArgument("isFromCart") { type = NavType.BoolType }),
            ) { backStackEntry ->
              val isFromCart = backStackEntry.arguments?.getBoolean("isFromCart") ?: true
              val checkoutItems =
                if (isFromCart) cartItems.filter { it.isSelected }
                else buyNowItems ?: emptyList()

              CheckoutScreen(
                items = checkoutItems,
                isFromCart = isFromCart,
                addresses = addresses,
                profile = profile,
                selectedVoucher = selectedVoucher,
                isBusy = isBusy,
                onAddAddress = viewModel::addAddress,
                onPlaceOrder = { items, fromCart, addr, shipMethod, shipCost, voucher, useCoins, payMethod ->
                  viewModel.placeOrder(
                    items = items,
                    isFromCart = fromCart,
                    address = addr,
                    shippingMethod = shipMethod,
                    shippingCost = shipCost,
                    voucher = voucher,
                    useCoins = useCoins,
                    paymentMethod = payMethod,
                  ) {
                    navController.popBackStack("home", inclusive = false)
                    navigateTab("orders")
                  }
                },
                onBack = { navController.popBackStack() },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TabIcon(
  tab: BottomTab,
  selected: Boolean,
  cartBadgeCount: Int,
) {
  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
  if (tab.route == "cart" && cartBadgeCount > 0) {
    BadgedBox(
      badge = {
        Badge(containerColor = ShopeeOrange, contentColor = Color.White) {
          Text(if (cartBadgeCount > 99) "99+" else cartBadgeCount.toString())
        }
      }
    ) {
      Icon(imageVector = icon, contentDescription = tab.label)
    }
  } else {
    Icon(imageVector = icon, contentDescription = tab.label)
  }
}
