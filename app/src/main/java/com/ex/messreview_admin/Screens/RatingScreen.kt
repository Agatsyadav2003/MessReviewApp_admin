package com.ex.messreview.Screens

import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ex.messreview_admin.R
import com.ex.messreview_admin.data.ratingsMap
import com.ex.messreview_admin.viewmodel.MenuViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class Review(val username: String, val text: String, val rating: Int)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(itemName: String, imageResId: Int,itemInfo:String?, viewModel: MenuViewModel) {
    var currentRating by remember { mutableStateOf(3) }
    var userRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    val reviews = remember { mutableStateListOf<Review>() }
    val monthlyAverages  by viewModel.monthlyAverages.observeAsState(initial = emptyMap())

    // Fetch reviews from Firebase
    LaunchedEffect(itemName) {
        val database = FirebaseDatabase.getInstance().reference
        val itemRef = database.child("items").child(itemInfo ?: "").child(itemName)
        itemRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                reviews.clear()
                for (dateSnapshot in snapshot.children) {
                    val date = dateSnapshot.key ?: ""
                    val reviewsSnapshot = dateSnapshot.child("reviews")
                    for (reviewSnapshot in reviewsSnapshot.children) {
                        val username = reviewSnapshot.key ?: ""
                        val text = reviewSnapshot.getValue(String::class.java) ?: ""
                        val rating = reviewSnapshot.child("rating").getValue(Int::class.java) ?: 0
                        reviews.add(Review(username, text, rating))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background
                .copy(alpha = 0.6f)
                .compositeOver(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Item Review Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .shadow(4.dp, RoundedCornerShape(36.dp)),
                shape = RoundedCornerShape(36.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = itemName, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        RatingBar(rating = currentRating)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque viverra blandit diam vel dapibus. Duis at mi id nunc rhoncus luctus dapibus ac.",
                        fontSize = 14.sp
                    )
                }
            }

            // Item-chart Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(200.dp)  // Add height constraint
                    .shadow(4.dp, RoundedCornerShape(36.dp)),
                shape = RoundedCornerShape(36.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {

                BarChart(data = generateBarData(itemName,itemInfo,monthlyAverages))

            }

            // Reviews List
            LazyColumn {
                items(reviews) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}
@Composable
fun generateBarData(itemName:String,itemInfo:String?, monthlyAverages: Map<String, List<Float>>): BarData {
    val entries = mutableListOf<BarEntry>()
    val ratings = getRatingsForDayAndMeal(itemName,itemInfo,monthlyAverages)

    for ((index, rating) in ratings.withIndex()) {
        entries.add(BarEntry(index.toFloat(), rating))
    }

    val dataSet = BarDataSet(entries, "Ratings")
    dataSet.color = MaterialTheme.colorScheme.primary.toArgb()

    return BarData(dataSet)
}

fun getRatingsForDayAndMeal(itemName:String,itemInfo:String?,monthlyAverages: Map<String, List<Float>>): List<Float> {


            val key = "$itemInfo-$itemName"
            return monthlyAverages[key] ?: listOf()


}

@Composable
fun RatingBar(rating: Int, onRatingChanged: ((Int) -> Unit)? = null) {
    Row {
        for (i in 1..5) {
            Icon(
                painter = painterResource(
                    id = if (i <= rating) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                ),
                contentDescription = null,
                tint = if (i <= rating) Color.Yellow else Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .clickable(enabled = onRatingChanged != null) {
                        onRatingChanged?.invoke(i)
                    }
            )
        }
    }
}

@Composable
fun LineChart() {
    val entries = remember { mutableStateListOf<Entry>() }

    LaunchedEffect(Unit) {
        // Generate data for 12 months with y values from 1 to 5
        for (i in 0..11) {
            val yValue = (1..5).random().toFloat()
            entries.add(Entry(i.toFloat(), yValue))
        }
    }

    val lineDataSet = LineDataSet(entries, "Taste Analysis").apply {
        color = MaterialTheme.colorScheme.primary.toArgb()
        valueTextColor = Color.Black.toArgb()
        valueTextSize = 12f
        setDrawFilled(false)
    }

    AndroidView(
        factory = { context ->
            com.github.mikephil.charting.charts.LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                data = LineData(lineDataSet)
                description.isEnabled = false
                legend.isEnabled = true

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            return months[value.toInt()]
                        }
                    }
                    granularity = 1f
                    textColor = Color.Black.toArgb()
                    textSize = 12f
                    setDrawGridLines(true)
                }

                axisLeft.apply {
                    axisMinimum = 1f  // Set minimum value for y-axis to 1
                    axisMaximum = 5f  // Set maximum value for y-axis to 5
                    textColor = Color.Black.toArgb()
                    textSize = 12f
                    setDrawGridLines(true)
                }
                axisRight.isEnabled = false

                // Important: Call invalidate() after setting data and configuration
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth() // Ensure chart takes full available width
            .height(200.dp) // Set a fixed height for the chart
            .padding(8.dp) // Add padding for better visual separation
    )
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clickable { /* Handle click */ }
            .shadow(4.dp, RoundedCornerShape(36.dp)),
        shape = RoundedCornerShape(36.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.text,
                    fontSize = 14.sp
                )
            }
        }
    }
}


