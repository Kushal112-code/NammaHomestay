package com.example.nammahomestay

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }
}

// ---------------- GLOBAL ----------------
val auth = FirebaseAuth.getInstance()
val db = FirebaseFirestore.getInstance()

data class Home(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val price: String = "",
    val menu: String = "",
    val ownerId: String = "",
    val limit: Int = 2
)

var homesGlobal = listOf<Home>()

// ---------------- NAV ----------------
@Composable
fun App() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "role") {

        composable("role") { RoleScreen(nav) }
        composable("login/{role}") { LoginScreen(nav, it.arguments?.getString("role")!!) }
        composable("register/{role}") { RegisterScreen(nav, it.arguments?.getString("role")!!) }

        composable("home") { HomeScreen(nav) }
        composable("add") { AddHomeScreen(nav) }
        composable("edit/{id}") { EditHomeScreen(nav, it.arguments?.getString("id")!!) }

        composable("details/{index}") {
            val i = it.arguments?.getString("index")?.toIntOrNull() ?: -1
            if (i in homesGlobal.indices) {
                HomeDetailScreen(nav, homesGlobal[i])
            } else {
                LaunchedEffect(Unit) { nav.popBackStack() }
            }
        }

        composable("booking/{name}/{ownerId}") {
            val name = Uri.decode(it.arguments?.getString("name") ?: "")
            val ownerId = Uri.decode(it.arguments?.getString("ownerId") ?: "")
            BookingScreen(nav, name, ownerId)
        }

        composable("ownerBookings") { OwnerBookingsScreen() }
        composable("history") { UserHistoryScreen() }
    }
}

// ---------------- ROLE ----------------
@Composable
fun RoleScreen(nav: NavController) {
    LaunchedEffect(Unit) { auth.signOut() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Namma HomeStay", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text("Select your role to continue", fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(Modifier.height(64.dp))

        Button(
            onClick = { nav.navigate("login/user") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("I am a User", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { nav.navigate("login/owner") },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("I am an Owner", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------- LOGIN ----------------
@Composable
fun LoginScreen(nav: NavController, role: String) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Login ($role)", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                auth.signInWithEmailAndPassword(email.trim(), pass)
                    .addOnSuccessListener {
                        isLoading = false
                        nav.navigate("home") {
                            popUpTo("role") { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Login", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        TextButton({ nav.navigate("register/$role") }) {
            Text("New user? Register now", fontSize = 16.sp)
        }
    }
}

// ---------------- REGISTER ----------------
@Composable
fun RegisterScreen(nav: NavController, role: String) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register ($role)", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                auth.createUserWithEmailAndPassword(email.trim(), pass)
                    .addOnSuccessListener { result ->
                        val uid = result.user?.uid ?: return@addOnSuccessListener
                        db.collection("users").document(uid)
                            .set(mapOf("name" to name, "phone" to phone, "role" to role))
                            .addOnSuccessListener {
                                isLoading = false
                                nav.navigate("home") {
                                    popUpTo("role") { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                isLoading = false
                                Toast.makeText(context, "DB Error: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Register", fontSize = 18.sp)
        }
    }
}

// ---------------- HOME ----------------
@Composable
fun HomeScreen(nav: NavController) {

    var homes by remember { mutableStateOf(listOf<Home>()) }
    var role by remember { mutableStateOf("") }
    val uid = auth.currentUser?.uid

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    role = it.getString("role") ?: "user"
                }
                .addOnFailureListener { role = "user" }
        }
    }

    LaunchedEffect(Unit) {
        db.collection("homes").addSnapshotListener { v, _ ->
            homes = v?.documents?.map {
                Home(
                    id = it.id,
                    name = it.getString("name") ?: "",
                    location = it.getString("location") ?: "",
                    price = it.getString("price") ?: "",
                    menu = it.getString("menu") ?: "",
                    ownerId = it.getString("ownerId") ?: "",
                    limit = (it.getLong("limit") ?: 2).toInt()
                )
            } ?: listOf()

            homesGlobal = homes
        }
    }

    if (role.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Namma HomeStay", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            TextButton({
                auth.signOut()
                nav.navigate("role") {
                    popUpTo(0) { inclusive = true }
                }
            }) {
                Text("Logout", color = Color.Red, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // OWNER UI
        if (role == "owner") {
            Row(Modifier.fillMaxWidth()) {
                Button(
                    onClick = { nav.navigate("add") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Home", fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { nav.navigate("ownerBookings") },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("View Bookings", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // USER UI
        if (role == "user") {
            Button(
                onClick = { nav.navigate("history") },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text("📜 My Bookings History", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            itemsIndexed(homes) { i, home ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {

                        Text(home.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(home.location, color = MaterialTheme.colorScheme.secondary)
                        Text("₹${home.price}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { nav.navigate("details/$i") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Details")
                        }

                        // owner edit/delete
                        if (home.ownerId == uid) {
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { nav.navigate("edit/${home.id}") },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Edit")
                                }
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { db.collection("homes").document(home.id).delete() },
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- ADD HOME ----------------
@Composable
fun AddHomeScreen(nav: NavController) {

    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Add New HomeStay", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(price, { price = it }, label = { Text("Price per night") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(menu, { menu = it }, label = { Text("Food Menu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(limit, { limit = it }, label = { Text("Guest Limit") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isEmpty() || price.isEmpty()) {
                    Toast.makeText(context, "Please provide all details", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val user = auth.currentUser
                if (user == null) {
                    Toast.makeText(context, "Please login again", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true
                val homeData = mapOf(
                    "name" to name,
                    "location" to location,
                    "price" to price,
                    "menu" to menu,
                    "ownerId" to user.uid,
                    "limit" to (limit.toIntOrNull() ?: 2)
                )
                db.collection("homes").add(homeData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "HomeStay Added Successfully", Toast.LENGTH_SHORT).show()
                        isLoading = false
                        nav.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Save & List", fontSize = 18.sp)
        }
    }
}

// ---------------- EDIT ----------------
@Composable
fun EditHomeScreen(nav: NavController, id: String) {

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("homes").document(id).get()
            .addOnSuccessListener {
                name = it.getString("name") ?: ""
                location = it.getString("location") ?: ""
                price = it.getString("price") ?: ""
                menu = it.getString("menu") ?: ""
            }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Edit Details", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(price, { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(menu, { menu = it }, label = { Text("Menu") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(Modifier.height(48.dp))

        Button(
            onClick = {
                db.collection("homes").document(id).update(
                    mapOf(
                        "name" to name,
                        "location" to location,
                        "price" to price,
                        "menu" to menu
                    )
                )
                nav.popBackStack()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Update Details", fontSize = 18.sp)
        }
    }
}

// ---------------- DETAILS ----------------
@Composable
fun HomeDetailScreen(nav: NavController, home: Home) {
    val context = LocalContext.current
    val uid = auth.currentUser?.uid
    var role by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { role = it.getString("role") }
                .addOnFailureListener { role = "user" }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Spacer(Modifier.height(24.dp))
        Text(home.name, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(home.location, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = Color.LightGray)
        
        Spacer(Modifier.height(16.dp))
        Text("What we offer:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("🍽️ Menu: ${home.menu}", fontSize = 16.sp)
        Text("👥 Capacity: ${home.limit} Persons", fontSize = 16.sp)

        Spacer(Modifier.weight(1f))

        // Role-based visibility
        if (role == "user") {
            Button(
                onClick = {
                    if (home.ownerId.isNotEmpty()) {
                        val encodedName = Uri.encode(home.name)
                        val encodedOwnerId = Uri.encode(home.ownerId)
                        nav.navigate("booking/$encodedName/$encodedOwnerId")
                    } else {
                        Toast.makeText(context, "Owner information missing", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Book Now", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        } else if (role == null) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

// ---------------- BOOKING ----------------
@Composable
fun BookingScreen(nav: NavController, name: String, ownerId: String) {

    val context = LocalContext.current
    val user = auth.currentUser
    var isLoading by remember { mutableStateOf(false) }
    
    if (user == null) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        return
    }
    
    val uid = user.uid

    var date by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener {
                userName = it.getString("name") ?: ""
                phone = it.getString("phone") ?: ""
            }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Finalize Booking", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("HomeStay: $name", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Guest: $userName")
                Text("Contact: $phone")
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    date = "$d/${m + 1}/$y"
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (date.isEmpty()) "📅 Select Stay Date" else "Date: $date ✓", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                if (date.isEmpty()) {
                    Toast.makeText(context, "Please select a date", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                isLoading = true
                db.collection("bookings").add(
                    mapOf(
                        "homeName" to name,
                        "userName" to userName,
                        "phone" to phone,
                        "date" to date,
                        "userId" to uid,
                        "ownerId" to ownerId
                    )
                )
                .addOnSuccessListener {
                    isLoading = false
                    Toast.makeText(context, "Booking Successful!", Toast.LENGTH_LONG).show()
                    nav.popBackStack()
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Confirm Reservation", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- OWNER BOOKINGS ----------------
@Composable
fun OwnerBookingsScreen() {

    val uid = auth.currentUser?.uid ?: ""
    var list by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            db.collection("bookings")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener { v, _ ->
                    list = v?.documents?.map { it.data!! } ?: listOf()
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Incoming Bookings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${it["homeName"]}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text("👤 Guest: ${it["userName"]}")
                        Text("📞 Contact: ${it["phone"]}")
                        Text("📅 Date: ${it["date"]}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ---------------- USER HISTORY ----------------
@Composable
fun UserHistoryScreen() {

    val uid = auth.currentUser?.uid ?: ""
    var list by remember { mutableStateOf(listOf<Map<String, Any>>()) }

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            db.collection("bookings")
                .whereEqualTo("userId", uid)
                .addSnapshotListener { v, _ ->
                    list = v?.documents?.map { it.data!! } ?: listOf()
                }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Stays", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${it["homeName"]}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Date: ${it["date"]}", color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("Booked ✓", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
