package com.example.colorwave.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class FirebasePaletteRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun palettesCollection() =
        firestore.collection("users")
            .document(auth.currentUser!!.uid)
            .collection("palettes")

    fun observePalettes(): Flow<List<FirebasePalette>> = callbackFlow {
        val listener = palettesCollection()
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val palettes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirebasePalette::class.java)
                        ?.copy(id = doc.id)
                } ?: emptyList()

                trySend(palettes)
            }

        awaitClose { listener.remove() }
    }

    fun savePalette(trackName: String, colorsHex: List<String>) {
        val palette = FirebasePalette(
            trackName = trackName,
            colorsHex = colorsHex
        )
        palettesCollection().add(palette)
    }

    fun deletePalette(id: String) {
        palettesCollection().document(id).delete()
    }
}