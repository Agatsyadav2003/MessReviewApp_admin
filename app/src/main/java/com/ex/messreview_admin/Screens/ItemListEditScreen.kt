package com.ex.messreview_admin.Screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.CircularProgressIndicator
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ex.messreview_admin.R
import com.ex.messreview_admin.viewmodel.MenuViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
fun cropToSquare(bitmap: Bitmap): Bitmap {
    val size = minOf(bitmap.width, bitmap.height)
    val xOffset = (bitmap.width - size) / 2
    val yOffset = (bitmap.height - size) / 2
    return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun itemEditScreen(dayOfWeek: String, mealTime: String, itemName: String, mess:String, viewModel: MenuViewModel) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var itemNameState by remember { mutableStateOf(TextFieldValue(itemName)) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    fun saveImageUrlInFirebase(url: String, mess: String, dayOfWeek: String, mealTime: String, itemName: String) {
        val database = FirebaseDatabase.getInstance().reference
        val imageUriString = imageUri.toString()
        database.child("items").child("$mess-$dayOfWeek-$mealTime").child(itemName).child("Desription").setValue(url)
    }
    fun uploadImageToFirebaseStorage(uri: Uri, mess: String, dayOfWeek: String, mealTime: String, itemName: String) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/$mess-$dayOfWeek-$mealTime/$itemName.jpg")
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()

        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUrl = uri.toString()
                saveImageUrlInFirebase(imageUrl!!, mess, dayOfWeek, mealTime, itemName)
            }
        }.addOnFailureListener {
            // Handle any errors
        }
    }



    // Function to load image URI from Firebase
    fun loadImageUrlFromFirebase(mess: String, dayOfWeek: String, mealTime: String, itemName: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("items").child("$mess-$dayOfWeek-$mealTime").child(itemName).child("Desription").get().addOnSuccessListener { snapshot ->
            val url = snapshot.getValue(String::class.java)
            imageUrl = url ?: "https://firebasestorage.googleapis.com/v0/b/my-application-6b503.appspot.com/o/foodimg.jpg?alt=media&token=5039597d-d0c1-4dd3-88ce-bd31784f0a9d"

        }.addOnFailureListener {
            // Handle errors in fetching URL
            Log.e("ImageFetch", "Failed to fetch image URL", it)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                croppedBitmap = cropToSquare(bitmap)
                uploadImageToFirebaseStorage(uri, mess, dayOfWeek, mealTime, itemName)
            }
        }
    }
    LaunchedEffect(Unit) {
        loadImageUrlFromFirebase(mess, dayOfWeek, mealTime, itemName)
    }
    if (showSaveDialog) {
        ConfirmationDialog(
            title = "Confirm Save",
            message = "Are you sure you want to save the changes?",
            isLoading = isLoading,
            onConfirm = {
                isLoading = true
                viewModel.saveItem(mess, dayOfWeek, mealTime, itemName, itemNameState.text,
                    onSuccess = {
                        isLoading = false
                        showSaveDialog = false
                    },
                    onFailure = { e ->
                        isLoading = false
                        showSaveDialog = false
                    }
                )
                viewModel.fetchMenuData("$mess")
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            title = "Confirm Delete",
            message = "Are you sure you want to delete this item?",
            isLoading = isLoading,
            onConfirm = {
                isLoading = true
                viewModel.deleteItem(mess, dayOfWeek, mealTime, itemName,
                    onSuccess = {
                        isLoading = false
                        showDeleteDialog = false
                    },
                    onFailure = { e ->
                        isLoading = false
                        showDeleteDialog = false
                    }
                )
                viewModel.fetchMenuData("$mess")
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
                    .copy(alpha = 0.6f)
                    .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp) // Ensures equidistance between elements
    ) {
        Spacer(modifier = Modifier.height(32.dp)) // Increased top spacing

        Text(
            text = "$mess-$dayOfWeek - $mealTime",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                imageUrl?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } ?: run {
                    Image(
                        painter = painterResource(id = R.drawable.foodimg),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            CircleShape
                        ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            shape = MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp))
        ) {
            OutlinedTextField(
                value = itemNameState,
                onValueChange = { itemNameState = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), // Adjusted padding for better alignment
                shape = MaterialTheme.shapes.medium.copy(all = CornerSize(26.dp)) // Matching shape with card's shape
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { showSaveDialog = true},
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(50.dp))
            IconButton(
                onClick = { showDeleteDialog = true},
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.error, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                Text(text = message)
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            if (!isLoading) {
                Button(onClick = onConfirm) {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            if (!isLoading) {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewItemEditScreen() {

    val mockViewModel = MenuViewModel()
    itemEditScreen(
        dayOfWeek = "Monday",
        mealTime = "Lunch",
        itemName = "Paneer Butter Masala",
        mess = "Mess A",
        viewModel = mockViewModel
    )
}



