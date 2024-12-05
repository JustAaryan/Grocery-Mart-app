package com.example.usergrocerymart.viewmodels

import androidx.lifecycle.ViewModel
import com.example.usergrocerymart.models.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel: ViewModel() {
    fun fetchallprod(): Flow<List<Product>> = callbackFlow {
        val db =  FirebaseDatabase.getInstance().getReference("Admin").child("All Products")
        val eventlistener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)


                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventlistener)
        awaitClose{db.removeEventListener(eventlistener)}
    }
    fun getcatprod(category : String):Flow<List<Product>> = callbackFlow  {

        val db =  FirebaseDatabase.getInstance().getReference("Admin").child("ProductCategory/${category}")
        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)


                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        db.addValueEventListener(eventListener)
        awaitClose{
            db.removeEventListener(eventListener)
        }

    }
}