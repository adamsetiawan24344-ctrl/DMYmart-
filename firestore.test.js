const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const nowTs = () => new Date(Date.now() - 2000);

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function sampleProduct(sellerId) {
  return {
    name: "Indomie Goreng Spesial",
    description: "Mi goreng instan favorit keluarga Indonesia.",
    categories: ["Makanan & Minuman", "Bahan Pokok & Dapur", "Promo & Paket Hemat"],
    price: 3100,
    originalPrice: 3500,
    stock: 120,
    soldCount: 450,
    rating: 4.9,
    unit: "Bungkus 85g",
    imageKey: "food_noodle",
    sellerId,
    sellerName: "DMYmart Official",
    isFlashSale: true,
    isOfficialStore: true,
    isPublished: true,
    createdAt: nowTs(),
    updatedAt: nowTs(),
  };
}

function sampleCartItem(sellerId) {
  return {
    productId: "prod_indomie",
    productName: "Indomie Goreng Spesial",
    productImageKey: "food_noodle",
    categories: ["Makanan & Minuman", "Bahan Pokok & Dapur"],
    unit: "Bungkus 85g",
    sellerId,
    sellerName: "DMYmart Official",
    price: 3100,
    originalPrice: 3500,
    quantity: 2,
    stock: 120,
    isSelected: true,
    createdAt: nowTs(),
    updatedAt: nowTs(),
  };
}

test("Unauthenticated user: cannot read products, cart, or user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").where("isPublished", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
});

test("User profile: owner can create and update coins, cross-user access denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      coins: 500,
      walletBalance: 100000,
      lastCheckInDate: "2026-11-16",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      coins: 600,
      lastCheckInDate: "2026-11-17",
      updatedAt: nowTs(),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      coins: 999999,
      updatedAt: nowTs(),
    })
  );
});

test("Cart: owner can add, update quantity, toggle selection, and delete; cross-user denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceCartRef = aliceDb.collection("users").doc(ALICE_UID).collection("cart").doc("prod_indomie");

  await assertSucceeds(aliceCartRef.set(sampleCartItem(ALICE_UID)));
  await assertSucceeds(
    aliceCartRef.update({
      quantity: 5,
      updatedAt: nowTs(),
    })
  );
  await assertSucceeds(
    aliceCartRef.update({
      isSelected: false,
      updatedAt: nowTs(),
    })
  );

  // Reject ghost field shadow update
  await assertFails(
    aliceCartRef.update({
      quantity: 3,
      ghostAdminOverride: true,
      updatedAt: nowTs(),
    })
  );

  // Cross-user read/write/delete denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").doc("prod_indomie").delete());

  await assertSucceeds(aliceCartRef.delete());
});

test("Products: multi-category create, filtered list succeeds, unfiltered list fails, seller-only update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(aliceDb.collection("products").doc("prod_indomie").set(sampleProduct(ALICE_UID)));

  // Unfiltered list violates rules-are-not-filters
  await assertFails(bobDb.collection("products").get());

  // Filtered published query succeeds
  await assertSucceeds(bobDb.collection("products").where("isPublished", "==", true).get());

  // Bob cannot spoof sellerId to Alice
  await assertFails(bobDb.collection("products").doc("prod_spoof").set(sampleProduct(ALICE_UID)));

  // Bob cannot modify Alice's product
  await assertFails(
    bobDb.collection("products").doc("prod_indomie").update({
      price: 100,
      updatedAt: nowTs(),
    })
  );

  // Alice can update her own product categories & price
  await assertSucceeds(
    aliceDb.collection("products").doc("prod_indomie").update({
      categories: ["Makanan & Minuman", "Promo & Paket Hemat"],
      price: 2900,
      updatedAt: nowTs(),
    })
  );
});

test("Orders: create, status progression, and terminal state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_001");

  await assertSucceeds(
    orderRef.set({
      orderNumber: "DMY-20261116-001",
      status: "DIKEMAS",
      items: [{ productId: "prod_indomie", name: "Indomie Goreng", price: 3100, quantity: 2 }],
      itemSummary: "Indomie Goreng (2x)",
      shippingAddress: "Jl. Merdeka No. 10, Jakarta",
      recipientName: "Alice",
      recipientPhone: "081234567890",
      shippingMethod: "Reguler 1-2 Jam",
      shippingCost: 5000,
      voucherCode: "DMYHEMAT10",
      discountAmount: 620,
      coinsUsed: 0,
      subtotal: 6200,
      totalAmount: 10580,
      paymentMethod: "QRIS",
      createdAt: nowTs(),
      updatedAt: nowTs(),
    })
  );

  await assertSucceeds(orderRef.update({ status: "DIKIRIM", updatedAt: nowTs() }));
  await assertSucceeds(orderRef.update({ status: "SELESAI", updatedAt: nowTs() }));

  // Terminal state lock: cannot update after SELESAI
  await assertFails(orderRef.update({ status: "DIBATALKAN", updatedAt: nowTs() }));

  // Delete always denied
  await assertFails(orderRef.delete());
});
