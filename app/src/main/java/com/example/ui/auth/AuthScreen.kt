package com.example.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.theme.AmberFlash
import com.example.ui.theme.EmeraldVoucher
import com.example.ui.theme.ShopeeOrange
import com.example.ui.theme.ShopeeOrangeDark
import com.example.ui.theme.ShopeeOrangeLight

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var isCheckingSession by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isCheckingSession = false
        onAuthSuccess()
      },
      onUnauthenticated = { isCheckingSession = false },
      scope = coroutineScope,
    )
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(ShopeeOrange, ShopeeOrangeLight, MaterialTheme.colorScheme.background)
          )
        ),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .widthIn(max = 560.dp)
          .statusBarsPadding()
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(12.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          shadowElevation = 6.dp,
          modifier = Modifier.size(54.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Rounded.ShoppingBag,
              contentDescription = "Logo DMYmart",
              tint = ShopeeOrange,
              modifier = Modifier.size(30.dp),
            )
          }
        }
        Spacer(Modifier.width(12.dp))
        Column {
          Text(
            text = "DMYmart",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Minimarket Hemat • Jual & Beli Cepat",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.92f),
          )
        }
      }

      Spacer(Modifier.height(20.dp))

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_promo),
            contentDescription = "Promo Minimarket DMYmart",
            modifier =
              Modifier.fillMaxWidth().height(176.dp).clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
          )

          Spacer(Modifier.height(18.dp))

          Text(
            text = "Belanja Harian & Kelola Toko Dalam Satu Aplikasi",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Spacer(Modifier.height(6.dp))
          Text(
            text =
              "Nikmati Flash Sale minimarket, voucher gratis ongkir, keranjang tersinkron otomatis, dan jual produk Anda sendiri.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(Modifier.height(16.dp))

          FeatureRow(
            icon = Icons.Rounded.Bolt,
            tint = AmberFlash,
            title = "Flash Sale & Voucher Harian",
            subtitle = "Diskon sembako, snack, & minuman setiap hari",
          )
          Spacer(Modifier.height(10.dp))
          FeatureRow(
            icon = Icons.Rounded.Category,
            tint = ShopeeOrange,
            title = "Multi-Kategori & Pencarian Canggih",
            subtitle = "Saran pintar & filter harga hingga multi-kategori",
          )
          Spacer(Modifier.height(10.dp))
          FeatureRow(
            icon = Icons.Rounded.LocalShipping,
            tint = EmeraldVoucher,
            title = "Keranjang & Pesanan Tersimpan",
            subtitle = "Aman di cloud meski aplikasi ditutup",
          )
          Spacer(Modifier.height(10.dp))
          FeatureRow(
            icon = Icons.Rounded.Storefront,
            tint = ShopeeOrangeDark,
            title = "Buka Toko Minimarket Anda",
            subtitle = "Tambah produk & atur banyak kategori sekaligus",
          )

          Spacer(Modifier.height(20.dp))

          if (errorMessage != null) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.errorContainer,
              modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            ) {
              Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp),
              )
            }
          }

          Button(
            onClick = {
              errorMessage = null
              isLoading = true
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  errorMessage = msg
                },
                scope = coroutineScope,
                onAuthCancelled = { isLoading = false },
              )
            },
            enabled = !isLoading && !isCheckingSession,
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = ShopeeOrange,
                contentColor = Color.White,
              ),
            modifier =
              Modifier.fillMaxWidth().height(52.dp).testTag("google_sign_in_button"),
          ) {
            if (isLoading || isCheckingSession) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp,
              )
              Spacer(Modifier.width(10.dp))
              Text(
                text = if (isCheckingSession) "Memeriksa Sesi..." else "Menghubungkan...",
                style = MaterialTheme.typography.labelLarge,
              )
            } else {
              Text(
                text = "Masuk dengan Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }

          Spacer(Modifier.height(10.dp))
          Text(
            text = "Masuk aman menggunakan akun Google untuk menyimpan keranjang & pesanan Anda.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}

@Composable
private fun FeatureRow(
  icon: ImageVector,
  tint: Color,
  title: String,
  subtitle: String,
) {
  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
    Surface(
      shape = CircleShape,
      color = tint.copy(alpha = 0.14f),
      modifier = Modifier.size(38.dp),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(20.dp),
        )
      }
    }
    Spacer(Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
