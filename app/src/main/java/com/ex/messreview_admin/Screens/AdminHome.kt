package com.ex.messreview.Screens

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.ex.messreview_admin.R

import com.ex.messreview_admin.data.ratingsMap
import com.ex.messreview_admin.viewmodel.MenuViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

val currentDayIndex = LocalDate.now().dayOfWeek.value % 7

val currentMealTime = when (LocalTime.now()) {
    in LocalTime.MIDNIGHT..LocalTime.of(8, 59) -> "Breakfast"
    in LocalTime.of(9, 0)..LocalTime.of(13, 59) -> "Lunch"
    in LocalTime.of(14, 0)..LocalTime.of(18, 59) -> "High Tea"
    else -> "Dinner"
}

var selectedDay by mutableStateOf(currentDayIndex)
val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
val mealTimes = listOf("Breakfast", "Lunch", "High Tea", "Dinner")
var selectedMealTime by mutableStateOf(currentMealTime)

@Composable
fun LoadingAnimation() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 100
                0.9f at 400
            },
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(color = Color.Gray, shape = CircleShape)
                .alpha(alpha)
        )
        Spacer(modifier = Modifier.width(15.dp))
        Box(
            modifier = Modifier
                .height(24.dp)
                .fillMaxWidth()
                .background(color = Color.Gray, shape = RoundedCornerShape(4.dp))
                .alpha(alpha)
        )
    }
}
@Composable
fun FoodItemList(day: String, mealTime: String, navController: NavHostController, mess: String, menuData: Map<String, Map<String, List<String>>>, viewModel: MenuViewModel) {
    val menuItems = menuData[day]?.get(mealTime) ?: listOf()
    val ratingData by viewModel.ratingData.observeAsState(initial = emptyMap())
    var imageUrls by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val isLoading = menuData.isEmpty() || ratingData.isEmpty()
    fun loadImageUrlFromFirebase(mess: String, dayOfWeek: String, mealTime: String, itemName: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("items").child("$mess-$dayOfWeek-$mealTime").child(itemName).child("Desription").get().addOnSuccessListener { snapshot ->
            val url = snapshot.getValue(String::class.java)
            imageUrls = imageUrls + (itemName to (url ?: "https://firebasestorage.googleapis.com/v0/b/my-application-6b503.appspot.com/o/foodimg.jpg?alt=media&token=5039597d-d0c1-4dd3-88ce-bd31784f0a9d"))
        }.addOnFailureListener {
            // Handle errors in fetching URL
            Log.e("ImageFetch", "Failed to fetch image URL", it)
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(0.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp)),
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
            ) {
                if (isLoading) {
                    LoadingAnimation()
                } else {
                    BarChart(data = generateBarData(selectedDay, selectedMealTime, ratingData))
                }
            }
            Text(
                text = "Menu for $day - $mealTime",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (isLoading) {
            items(5) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp))
                ) {
                    LoadingAnimation()
                }
            }
        } else {
            items(menuItems) { menuItem ->
                LaunchedEffect(menuItem) {
                    loadImageUrlFromFirebase(mess, day, mealTime, menuItem)
                }

                val imageUrl = imageUrls[menuItem]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            viewModel.calculateMonthlyAverages("$mess-$day-$mealTime-$menuItem")
                            navController.navigate("rating_screen/$menuItem/${R.drawable.foodimg}/$mess-$day-$mealTime")
                        },
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        imageUrl?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "Meal Image",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .padding(end=15.dp),
                                contentScale = ContentScale.Crop
                            )
                        } ?: run {
                            Image(
                                painter = painterResource(id = R.drawable.foodimg),
                                contentDescription = "Meal Image",
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .padding(end=15.dp),
                                    contentScale = ContentScale.Crop
                            )}
//                        Image(
//                            painter = painterResource(id = R.drawable.foodimg),
//                            contentDescription = "Meal Image",
//                            modifier = Modifier
//                                .size(90.dp)
//                                .clip(CircleShape)
//                                .padding(end = 15.dp),
//                            contentScale = ContentScale.Crop
//                        )
                        Column(
                            modifier = Modifier.weight(2f),
                        ) {
                            Text(
                                text = menuItem,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        IconButton(
                            onClick = {
                                navController.navigate("item_edit_screen/$day/$mealTime/$menuItem/$mess")
                            },
                            modifier = Modifier.padding(start = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Navigate to Edit screen",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavHostController, catererName: String, messType: String, viewModel: MenuViewModel) {
    val dayListState = rememberLazyListState()
    val mealListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val menuData by viewModel.menuData.observeAsState(initial = emptyMap())


    LaunchedEffect(Unit) {
        coroutineScope.launch {
            dayListState.scrollToItem(selectedDay)
            mealListState.scrollToItem(mealTimes.indexOf(selectedMealTime))

        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background
                    .copy(alpha = 0.6f)
                    .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    navController.popBackStack()
                    viewModel.menuData.value = emptyMap()
                    viewModel.ratingData.value = emptyMap()
                          },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "$catererName: $messType",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = { viewModel.addItem("$catererName-$messType", daysOfWeek[selectedDay], selectedMealTime,
                    onSuccess = {
                        // Handle success (e.g., show a success message)
                    },
                    onFailure = { e ->
                        // Handle failure (e.g., show an error message)
                    }
                )
                    viewModel.fetchMenuData("$catererName-$messType")          },
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            state = dayListState,
            modifier = Modifier.padding(5.dp)
        ) {
            items(daysOfWeek.size) { index ->
                val day = daysOfWeek[index]
                val isSelected = selectedDay == index
                val buttonColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
                        .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )

                Button(
                    onClick = { selectedDay = index },
                    modifier = Modifier
                        .padding(3.dp)
                        .background(buttonColor, CircleShape),
                    shape = CircleShape
                ) {
                    Text(
                        text = day,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            state = mealListState,
            modifier = Modifier.padding(5.dp)
        ) {
            items(mealTimes.size) { index ->
                val mealTime = mealTimes[index]
                val isSelected = selectedMealTime == mealTime
                val buttonColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
                        .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )

                Button(
                    onClick = { selectedMealTime = mealTime },
                    modifier = Modifier
                        .padding(3.dp)
                        .background(buttonColor, CircleShape),
                    shape = CircleShape
                ) {
                    Text(
                        text = mealTime,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                    )
                }
            }
        }




        if (selectedDay > -1) {
            FoodItemList(day = daysOfWeek[selectedDay], mealTime = selectedMealTime, navController = navController,mess="$catererName-$messType",menuData = menuData,viewModel)
        }
    }
}
@Composable
fun generateBarData(day: Int, mealTime: String, ratingData: Map<String, List<Float>>): BarData {
    val entries = mutableListOf<BarEntry>()
    val ratings = getRatingsForDayAndMeal(day, mealTime,ratingData)

    for ((index, rating) in ratings.withIndex()) {
        entries.add(BarEntry(index.toFloat(), rating))
    }

    val dataSet = BarDataSet(entries, "Ratings")
    dataSet.color = MaterialTheme.colorScheme.primary.toArgb()

    return BarData(dataSet)
}

fun getRatingsForDayAndMeal(day: Int, mealTime: String, ratingData: Map<String, List<Float>>): List<Float> {
    val daysOfWeek = listOf("Sun","Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val key = "${daysOfWeek[day]}-$mealTime"
    return ratingData[key] ?: listOf()
}

@Composable
fun BarChart(data: BarData) {
    val labelTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.BarChart(context).apply {
                this.data = data
                description.isEnabled = false
                legend.isEnabled = false

                // Enable horizontal scrolling
                setScaleEnabled(false)
                setScaleYEnabled(false)
                setPinchZoom(false)

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                xAxis.valueFormatter = object : ValueFormatter() {
                    private val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                    override fun getFormattedValue(value: Float): String {
                        return months.getOrNull(value.toInt() % months.size) ?: value.toString()
                    }
                }

                axisRight.isEnabled = false
                axisLeft.axisMinimum = 1f
                axisLeft.axisMaximum = 5f
                axisLeft.granularity = 1f

                // Add spacing between bars
                barData.barWidth = 0.4f
                xAxis.axisMinimum = -0.5f
                xAxis.axisMaximum = data.xMax + 0.5f

                invalidate()
            }
        },
        update = { chart ->
            chart.data = data
            chart.xAxis.textColor = labelTextColor
            chart.axisLeft.textColor = labelTextColor
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp) // Adjust height as needed
            .clip(MaterialTheme.shapes.medium.copy(all = CornerSize(36.dp))) // Add clipping to match card shape

            .padding(16.dp) // Add padding for better appearance
            .horizontalScroll(rememberScrollState()) // Enable horizontal scrolling
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme { // Use MaterialTheme to provide default styling
        HomeScreen(
            navController = rememberNavController(),
            catererName = "Zenith",
            messType = "Veg",
            viewModel = MenuViewModel() // You'll likely need a mock ViewModel for preview
        )


    }
}

@Preview(showBackground = true)
@Composable
fun chartPreview(){
    MaterialTheme { // Use MaterialTheme to provide default styling
        BarChart(data = generateBarData(selectedDay, selectedMealTime, ratingsMap))
    }
}





