package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketplaceCategories
import com.example.data.model.Product
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.ShopeeProductCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeSoft
import com.example.ui.viewmodel.SearchFilterState
import com.example.ui.viewmodel.SearchSuggestion
import com.example.ui.viewmodel.SortOption
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryScreen(
  productsState: UiState<List<Product>>,
  onOpenSearch: () -> Unit,
  onOpenSearchWithCategory: (String) -> Unit,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedCategories by remember { mutableStateOf<Set<String>>(emptySet()) }
  var matchAllCategories by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Surface(
      color = ShopeeOrange,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Kategori Produk",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          IconButton(onClick = onOpenSearch) {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = "Cari produk",
              tint = Color.White,
            )
          }
        }
        Text(
          text = "Pilih 1 atau lebih kategori sekaligus untuk melihat produk terkait.",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.9f),
        )
      }
    }

    when (productsState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          androidx.compose.material3.CircularProgressIndicator(color = ShopeeOrange)
        }
      }
      is UiState.Error -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(productsState.message, color = MaterialTheme.colorScheme.error)
        }
      }
      is UiState.Success -> {
        val products = productsState.data
        val filtered =
          remember(products, selectedCategories, matchAllCategories) {
            if (selectedCategories.isEmpty()) {
              products
            } else if (matchAllCategories) {
              products.filter { prod -> selectedCategories.all { it in prod.categories } }
            } else {
              products.filter { prod -> selectedCategories.any { it in prod.categories } }
            }
          }

        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 165.dp),
          contentPadding = PaddingValues(14.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize(),
        ) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "Filter Multi-Kategori (${selectedCategories.size} dipilih)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  if (selectedCategories.isNotEmpty()) {
                    TextButton(onClick = { selectedCategories = emptySet() }) {
                      Text("Reset", color = ShopeeOrange)
                    }
                  }
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  MarketplaceCategories.allSelectable.forEach { cat ->
                    val count = products.count { cat in it.categories }
                    val isSelected = cat in selectedCategories
                    FilterChip(
                      selected = isSelected,
                      onClick = {
                        selectedCategories =
                          if (isSelected) selectedCategories - cat else selectedCategories + cat
                      },
                      label = { Text("$cat ($count)") },
                      leadingIcon =
                        if (isSelected) {
                          {
                            Icon(
                              imageVector = Icons.Rounded.Check,
                              contentDescription = null,
                              modifier = Modifier.size(16.dp),
                            )
                          }
                        } else null,
                      colors =
                        FilterChipDefaults.filterChipColors(
                          selectedContainerColor = ShopeeOrangeSoft,
                          selectedLabelColor = ShopeeOrange,
                          selectedLeadingIconColor = ShopeeOrange,
                        ),
                    )
                  }
                }

                if (selectedCategories.size > 1) {
                  Spacer(Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = "Cocokkan SEMUA kategori terpilih",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Switch(
                      checked = matchAllCategories,
                      onCheckedChange = { matchAllCategories = it },
                    )
                  }
                }
              }
            }
          }

          item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Menampilkan ${filtered.size} produk",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
              )
              if (selectedCategories.size == 1) {
                TextButton(
                  onClick = { onOpenSearchWithCategory(selectedCategories.first()) }
                ) {
                  Text("Filter Lanjutan >", color = ShopeeOrange)
                }
              }
            }
          }

          if (filtered.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
              Box(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                contentAlignment = Alignment.Center,
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Rounded.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                  )
                  Spacer(Modifier.height(8.dp))
                  Text(
                    text = "Belum ada produk dengan kombinasi kategori tersebut",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          } else {
            items(filtered, key = { it.id }) { product ->
              ShopeeProductCard(
                product = product,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  query: String,
  submittedQuery: String,
  suggestions: List<SearchSuggestion>,
  results: List<Product>,
  recentSearches: List<String>,
  popularSearches: List<String>,
  filterState: SearchFilterState,
  onQueryChange: (String) -> Unit,
  onSubmitSearch: (String) -> Unit,
  onClearRecent: () -> Unit,
  onUpdateFilter: ((SearchFilterState) -> SearchFilterState) -> Unit,
  onResetFilter: () -> Unit,
  onProductClick: (Product) -> Unit,
  onAddToCart: (Product) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  var showSuggestions by remember(submittedQuery) {
    mutableStateOf(submittedQuery.isEmpty() && filterState.activeFilterCount == 0)
  }
  var showFilterSheet by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    // Search Top Bar
    Surface(color = ShopeeOrange, modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Kembali",
            tint = Color.White,
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          modifier = Modifier.weight(1f).height(44.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.Search,
              contentDescription = null,
              tint = ShopeeOrange,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
              if (query.isEmpty()) {
                Text(
                  text = "Cari snack, beras, sabun, promo...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color(0xFF8A8E99),
                  maxLines = 1,
                )
              }
              BasicTextField(
                value = query,
                onValueChange = {
                  onQueryChange(it)
                  showSuggestions = true
                },
                singleLine = true,
                textStyle =
                  MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1D1E24)),
                cursorBrush = SolidColor(ShopeeOrange),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions =
                  KeyboardActions(
                    onSearch = {
                      onSubmitSearch(query)
                      showSuggestions = false
                    }
                  ),
                modifier = Modifier.fillMaxWidth().testTag("search_input_field"),
              )
            }
            if (query.isNotEmpty()) {
              IconButton(
                onClick = {
                  onQueryChange("")
                  showSuggestions = true
                },
                modifier = Modifier.size(32.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Close,
                  contentDescription = "Hapus kata kunci",
                  tint = Color(0xFF777A85),
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }

        Spacer(Modifier.width(6.dp))

        TextButton(
          onClick = {
            onSubmitSearch(query)
            showSuggestions = false
          },
          modifier = Modifier.testTag("search_submit_button"),
        ) {
          Text("Cari", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }

    if (showSuggestions) {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
      ) {
        if (query.isBlank()) {
          if (recentSearches.isNotEmpty()) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Pencarian Terakhir",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onClearRecent) {
                  Icon(
                    imageVector = Icons.Rounded.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(Modifier.width(4.dp))
                  Text("Hapus", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Spacer(Modifier.height(4.dp))
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                recentSearches.forEach { term ->
                  Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier =
                      Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                          onSubmitSearch(term)
                          showSuggestions = false
                        },
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Rounded.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp),
                      )
                      Spacer(Modifier.width(6.dp))
                      Text(term, style = MaterialTheme.typography.bodySmall)
                    }
                  }
                }
              }
            }
          }

          item {
            Spacer(Modifier.height(6.dp))
            Text(
              text = "Pencarian Populer di DMYmart",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              popularSearches.forEach { term ->
                Surface(
                  shape = RoundedCornerShape(20.dp),
                  color = ShopeeOrangeSoft,
                  modifier =
                    Modifier
                      .clip(RoundedCornerShape(20.dp))
                      .clickable {
                        onSubmitSearch(term)
                        showSuggestions = false
                      },
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.TrendingUp,
                      contentDescription = null,
                      tint = ShopeeOrange,
                      modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                      term,
                      style = MaterialTheme.typography.bodySmall,
                      color = ShopeeOrange,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }
            }
          }
        } else {
          item {
            Text(
              text = "Saran Pencarian",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          items(suggestions) { suggestion ->
            when (suggestion) {
              is SearchSuggestion.CategorySuggestion -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable {
                        onUpdateFilter {
                          it.copy(selectedCategories = setOf(suggestion.categoryName))
                        }
                        onSubmitSearch("")
                        showSuggestions = false
                      },
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp),
                  ) {
                    Icon(
                      imageVector = Icons.Rounded.Category,
                      contentDescription = null,
                      tint = ShopeeOrange,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                      text = suggestion.label,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = ShopeeOrange,
                    )
                  }
                }
              }
              is SearchSuggestion.ProductSuggestion -> {
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = MaterialTheme.colorScheme.surface,
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .clickable {
                        onSubmitSearch(suggestion.product.name)
                        onProductClick(suggestion.product)
                      },
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp),
                  ) {
                    ProductThumbnail(
                      imageKey = suggestion.product.imageKey,
                      contentDescription = suggestion.product.name,
                      modifier = Modifier.size(44.dp),
                      iconSize = 20.dp,
                      cornerRadius = 8.dp,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = suggestion.product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                      Text(
                        text =
                          "${formatRupiah(suggestion.product.price)} • ${suggestion.product.categories.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShopeeOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }
              }
              is SearchSuggestion.HistorySuggestion,
              is SearchSuggestion.PopularSuggestion -> {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier =
                    Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(10.dp))
                      .clickable {
                        onSubmitSearch(suggestion.label)
                        showSuggestions = false
                      }
                      .padding(horizontal = 8.dp, vertical = 10.dp),
                ) {
                  Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(Modifier.width(10.dp))
                  Text(suggestion.label, style = MaterialTheme.typography.bodyMedium)
                }
              }
            }
          }
        }
      }
    } else {
      // Sort + Filter Bar
      Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            LazyRow(
              modifier = Modifier.weight(1f),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              items(SortOption.entries) { sortOpt ->
                val selected = filterState.sortOption == sortOpt
                FilterChip(
                  selected = selected,
                  onClick = { onUpdateFilter { it.copy(sortOption = sortOpt) } },
                  label = { Text(sortOpt.label) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = ShopeeOrangeSoft,
                      selectedLabelColor = ShopeeOrange,
                    ),
                )
              }
            }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(
              onClick = { showFilterSheet = true },
              shape = RoundedCornerShape(10.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
              modifier = Modifier.testTag("search_filter_button"),
            ) {
              BadgedBox(
                badge = {
                  if (filterState.activeFilterCount > 0) {
                    Badge(containerColor = ShopeeOrange, contentColor = Color.White) {
                      Text(filterState.activeFilterCount.toString())
                    }
                  }
                }
              ) {
                Icon(
                  imageVector = Icons.Rounded.FilterList,
                  contentDescription = "Filter hasil pencarian",
                  tint = ShopeeOrange,
                  modifier = Modifier.size(18.dp),
                )
              }
              Spacer(Modifier.width(6.dp))
              Text("Filter", color = ShopeeOrange, style = MaterialTheme.typography.labelMedium)
            }
          }

          if (filterState.activeFilterCount > 0) {
            LazyRow(
              contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              items(filterState.selectedCategories.toList()) { cat ->
                FilterChip(
                  selected = true,
                  onClick = {
                    onUpdateFilter { it.copy(selectedCategories = it.selectedCategories - cat) }
                  },
                  label = { Text(cat) },
                  trailingIcon = {
                    Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                  },
                )
              }
              if (filterState.flashSaleOnly) {
                item {
                  FilterChip(
                    selected = true,
                    onClick = { onUpdateFilter { it.copy(flashSaleOnly = false) } },
                    label = { Text("Flash Sale") },
                    trailingIcon = {
                      Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                  )
                }
              }
              if (filterState.minPrice != null || filterState.maxPrice != null) {
                item {
                  FilterChip(
                    selected = true,
                    onClick = { onUpdateFilter { it.copy(minPrice = null, maxPrice = null) } },
                    label = {
                      Text(
                        "Harga ${filterState.minPrice?.let(::formatRupiah) ?: "Rp0"} - ${filterState.maxPrice?.let(::formatRupiah) ?: "Max"}"
                      )
                    },
                    trailingIcon = {
                      Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                  )
                }
              }
            }
          }
          HorizontalDivider()
        }
      }

      if (results.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize().padding(32.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Rounded.SearchOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(56.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
              text = "Produk tidak ditemukan",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "Coba ubah kata kunci atau reset filter pencarian Anda.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Button(
              onClick = {
                onResetFilter()
                onSubmitSearch("")
              },
              colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
            ) {
              Text("Reset Pencarian & Filter")
            }
          }
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 165.dp),
          contentPadding = PaddingValues(14.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        ) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
              text = "Ditemukan ${results.size} produk relevan",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          items(results, key = { it.id }) { prod ->
            ShopeeProductCard(
              product = prod,
              onProductClick = onProductClick,
              onAddToCart = onAddToCart,
            )
          }
        }
      }
    }
  }

  if (showFilterSheet) {
    SearchFilterSheet(
      currentFilter = filterState,
      onApply = { newState ->
        onUpdateFilter { newState }
        showFilterSheet = false
      },
      onReset = {
        onResetFilter()
        showFilterSheet = false
      },
      onDismiss = { showFilterSheet = false },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterSheet(
  currentFilter: SearchFilterState,
  onApply: (SearchFilterState) -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  var selectedCats by remember { mutableStateOf(currentFilter.selectedCategories) }
  var minPriceText by remember { mutableStateOf(currentFilter.minPrice?.toString() ?: "") }
  var maxPriceText by remember { mutableStateOf(currentFilter.maxPrice?.toString() ?: "") }
  var minRating by remember { mutableStateOf(currentFilter.minRating) }
  var flashOnly by remember { mutableStateOf(currentFilter.flashSaleOnly) }
  var officialOnly by remember { mutableStateOf(currentFilter.officialOnly) }
  var inStockOnly by remember { mutableStateOf(currentFilter.inStockOnly) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp, vertical = 8.dp)
          .navigationBarsPadding()
    ) {
      Text(
        text = "Filter Pencarian Lanjutan",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
      Spacer(Modifier.height(14.dp))

      Text(
        text = "Kategori Produk (Pilih 1 atau Lebih)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(6.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        MarketplaceCategories.allSelectable.forEach { cat ->
          val active = cat in selectedCats
          FilterChip(
            selected = active,
            onClick = {
              selectedCats = if (active) selectedCats - cat else selectedCats + cat
            },
            label = { Text(cat) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = ShopeeOrangeSoft,
                selectedLabelColor = ShopeeOrange,
              ),
          )
        }
      }

      Spacer(Modifier.height(14.dp))
      Text(
        text = "Rentang Harga (Rp)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = minPriceText,
          onValueChange = { v -> minPriceText = v.filter { it.isDigit() } },
          label = { Text("Min Harga") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
          value = maxPriceText,
          onValueChange = { v -> maxPriceText = v.filter { it.isDigit() } },
          label = { Text("Max Harga") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.weight(1f),
        )
      }
      Spacer(Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = maxPriceText == "15000" && minPriceText.isEmpty(),
          onClick = {
            minPriceText = ""
            maxPriceText = "15000"
          },
          label = { Text("< Rp15rb") },
        )
        FilterChip(
          selected = minPriceText == "15000" && maxPriceText == "50000",
          onClick = {
            minPriceText = "15000"
            maxPriceText = "50000"
          },
          label = { Text("Rp15rb - 50rb") },
        )
        FilterChip(
          selected = minPriceText == "50000" && maxPriceText.isEmpty(),
          onClick = {
            minPriceText = "50000"
            maxPriceText = ""
          },
          label = { Text("> Rp50rb") },
        )
      }

      Spacer(Modifier.height(14.dp))
      Text(
        text = "Rating Minimum",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(0.0 to "Semua", 4.0 to "4.0+ ★", 4.5 to "4.5+ ★", 4.8 to "4.8+ ★").forEach {
          (ratingVal, label) ->
          FilterChip(
            selected = minRating == ratingVal,
            onClick = { minRating = ratingVal },
            label = { Text(label) },
          )
        }
      }

      Spacer(Modifier.height(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Hanya Promo Flash Sale", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = flashOnly, onCheckedChange = { flashOnly = it })
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Hanya DMYmart Official Mall", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = officialOnly, onCheckedChange = { officialOnly = it })
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text("Hanya Stok Tersedia", style = MaterialTheme.typography.bodyMedium)
        Switch(checked = inStockOnly, onCheckedChange = { inStockOnly = it })
      }

      Spacer(Modifier.height(18.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f).height(48.dp)) {
          Text("Reset")
        }
        Button(
          onClick = {
            onApply(
              currentFilter.copy(
                selectedCategories = selectedCats,
                minPrice = minPriceText.toLongOrNull(),
                maxPrice = maxPriceText.toLongOrNull(),
                minRating = minRating,
                flashSaleOnly = flashOnly,
                officialOnly = officialOnly,
                inStockOnly = inStockOnly,
              )
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = ShopeeOrange),
          modifier = Modifier.weight(1.4f).height(48.dp),
        ) {
          Text("Terapkan Filter", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
      Spacer(Modifier.height(16.dp))
    }
  }
}
