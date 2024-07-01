package com.ex.messreview_admin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

import java.util.Calendar

class MenuViewModel : ViewModel() {

    val menuData = MutableLiveData<Map<String, Map<String, List<String>>>>()
    val ratingData = MutableLiveData<Map<String, List<Float>>>()
    val monthlyAverages = MutableLiveData<Map<String, List<Float>>>()
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    fun addItem(mess: String, dayOfWeek: String, mealTime: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(mess).document(dayOfWeek)
        val newItemName = "Item"

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val items = document.get(mealTime) as? MutableList<String>
                if (items != null) {
                    items.add(newItemName)
                    docRef.update(mealTime, items).addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
                } else {
                    val newItems = mutableListOf(newItemName)
                    docRef.update(mealTime, newItems).addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
                }
            } else {
                val newItems = mutableListOf(newItemName)
                docRef.set(mapOf(mealTime to newItems)).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { e ->
                    onFailure(e)
                }
            }
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    fun saveItem(mess: String, dayOfWeek: String, mealTime: String, oldItemName: String, newItemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(mess).document(dayOfWeek)

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val items = document.get(mealTime) as? MutableList<String>
                if (items != null) {
                    val index = items.indexOf(oldItemName)
                    if (index != -1) {
                        items[index] = newItemName
                        docRef.update(mealTime, items).addOnSuccessListener {
                            onSuccess()
                        }.addOnFailureListener { e ->
                            onFailure(e)
                        }
                    }
                }
            }
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    fun deleteItem(mess: String, dayOfWeek: String, mealTime: String, itemName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection(mess).document(dayOfWeek)

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val items = document.get(mealTime) as? MutableList<String>
                if (items != null) {
                    items.remove(itemName)
                    docRef.update(mealTime, items).addOnSuccessListener {
                        onSuccess()
                    }.addOnFailureListener { e ->
                        onFailure(e)
                    }
                }
            }
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    fun fetchMenuData(messType: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val menuDataMap = mutableMapOf<String, MutableMap<String, List<String>>>()

            try {
                for (day in daysOfWeek) {
                    val documentSnapshot = db.collection(messType).document(day).get().await()
                    val dayMenuData = documentSnapshot.data?.mapValues { entry ->
                        entry.value as List<String>
                    } ?: emptyMap()
                    menuDataMap[day] = dayMenuData.toMutableMap()
                }
                menuData.postValue(menuDataMap)
            } catch (e: Exception) {
                e.printStackTrace()
                menuData.postValue(emptyMap())
            }
        }
    }
    data class Review(
        val breakfast: Float? = null,
        val lunch: Float? = null,
        val highTea: Float? = null,
        val dinner: Float? = null
    )


    fun calculateMonthlyAverages(
        itemPath: String

    ) {
        val database = FirebaseDatabase.getInstance().getReference("items")
        val pathComponents = itemPath.split("-")

        if (pathComponents.size != 5) {
            throw IllegalArgumentException("Invalid item path format")
        }


        val itemName = pathComponents[4]
        val itemPath1= pathComponents[0]+"-"+pathComponents[1]+"-"+pathComponents[2]+"-"+pathComponents[3]
        val itemRef = database.child(itemPath1).child(itemName)
        val ratingsMap = mutableMapOf<String, MutableList<Float>>()

        itemRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dateSnapshot in snapshot.children) {
                    val dateStr = dateSnapshot.key
                    val ratingsSnapshot = dateSnapshot.child("ratings")
                    if (dateStr != null && ratingsSnapshot.exists()) {
                        val month = getMonthFromDate(dateStr)
                        if (!ratingsMap.containsKey(month)) {
                            ratingsMap[month] = mutableListOf()
                        }
                        for (ratingSnapshot in ratingsSnapshot.children) {
                            val rating = ratingSnapshot.value.toString().toFloat()
                            ratingsMap[month]?.add(rating)
                        }
                    }
                }

                // Calculate the average for each month
                val averageRatingsMap = ratingsMap.mapValues { entry ->
                    val ratings = entry.value
                    ratings.sum() / ratings.size
                }

                monthlyAverages.postValue(mapOf(Pair(itemPath, averageRatingsMap.values.toList())))
            }

            override fun onCancelled(error: DatabaseError) {
                throw error.toException()
            }
        })
    }
    fun getMonthFromDate(dateStr: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = sdf.parse(dateStr)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault())!!
    }

    fun fetchRatingData(messType: String) {
        val database = Firebase.database
        val myRef = database.getReference("Reviews").child(messType)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingsMap = mutableMapOf<String, MutableMap<String, MutableList<Float>>>()

                for (dateSnapshot in snapshot.children) {
                    for (userSnapshot in dateSnapshot.children) {
                        val userId = userSnapshot.key ?: continue
                        val review = userSnapshot.getValue(Review::class.java) ?: continue

                        val date = dateSnapshot.key ?: continue
                        val dateParts = date.split("-")
                        if (dateParts.size != 3) continue

                        val year = dateParts[0]
                        val month = dateParts[1]
                        val dayOfWeek = getDayOfWeek(date)

                        review.breakfast?.let {
                            val key = "$dayOfWeek-Breakfast"
                            ratingsMap.getOrPut(key) { mutableMapOf() }
                                .getOrPut(month) { mutableListOf() }
                                .add(it)
                        }
                        review.lunch?.let {
                            val key = "$dayOfWeek-Lunch"
                            ratingsMap.getOrPut(key) { mutableMapOf() }
                                .getOrPut(month) { mutableListOf() }
                                .add(it)
                        }
                        review.highTea?.let {
                            val key = "$dayOfWeek-High Tea"
                            ratingsMap.getOrPut(key) { mutableMapOf() }
                                .getOrPut(month) { mutableListOf() }
                                .add(it)
                        }
                        review.dinner?.let {
                            val key = "$dayOfWeek-Dinner"
                            ratingsMap.getOrPut(key) { mutableMapOf() }
                                .getOrPut(month) { mutableListOf() }
                                .add(it)
                        }
                    }
                }

                val averagedRatingsMap = ratingsMap.mapValues { entry ->
                    entry.value.mapValues { monthEntry ->
                        monthEntry.value.average().toFloat()
                    }.toSortedMap(compareBy { it.toInt() }) // Sort by month for proper order
                }.mapValues { entry ->
                    entry.value.values.toList() // Convert month-wise map to list of averages
                }

                ratingData.postValue(averagedRatingsMap)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }


    private fun getDayOfWeek(date: String): String {
        // Assuming date is in the format "yyyy-MM-dd"
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = dateFormatter.parse(date)
        val dayOfWeekFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        return dayOfWeekFormatter.format(dateObj)
    }
}